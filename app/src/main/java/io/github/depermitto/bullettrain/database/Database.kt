package io.github.depermitto.bullettrain.database

import android.content.Context
import androidx.compose.runtime.*
import io.github.depermitto.bullettrain.R
import io.github.depermitto.bullettrain.train.WorkoutPhase
import io.github.depermitto.bullettrain.util.BKTree
import io.github.depermitto.bullettrain.util.bigListSet
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.pickFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.updateAndGet
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.time.Month
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.text.contains

private const val SETTINGS_FILENAME = "settings"
private const val HISTORY_FILENAME = "history"
private const val PROGRAMS_FILENAME = "programs"
private const val EXERCISES_FILENAME = "exercises"

class Database(private val databaseDirectory: File, private val context: Context) {

    private val settingsFile = SettingsFile(File(databaseDirectory, SETTINGS_FILENAME))
    private val historyFile = HistoryFile(File(databaseDirectory, HISTORY_FILENAME))
    private val programsFile = ProgramsFile(File(databaseDirectory, PROGRAMS_FILENAME))
    private val exercisesFile = ExerciseFile(File(databaseDirectory, EXERCISES_FILENAME))

    private val backupFiles = listOf(settingsFile, historyFile, programsFile, exercisesFile)

    init {
        if (backupFiles.any { !it.file.exists() }) {
            settingsFile.write(Settings())
            historyFile.write(emptyList())
            programsFile.write(listOf(Program.EmptyWorkout))
            exercisesFile.write(emptyList())
        }
    }

    private val backupFile = File(databaseDirectory, "bullet-train.bk.zip")

    var settingsDao by mutableStateOf(SettingsDao(settingsFile))
        private set
    var historyDao by mutableStateOf(HistoryDao(historyFile))
        private set
    var programDao by mutableStateOf(ProgramDao(programsFile))
        private set
    var exerciseDao by mutableStateOf(ExerciseDao(exercisesFile))
        private set


    /**
     * launch file picker and export the zipped database to it. Returns name the file the database was exported to  if successful.
     */
    suspend fun exportDatabase(): String? {
        ZipOutputStream(FileOutputStream(backupFile)).use { zipOutputStream ->
            for (backup in backupFiles) {
                zipOutputStream.putNextEntry(ZipEntry(backup.file.name))
                zipOutputStream.write(backup.file.readBytes())
            }
        }
        val file = FileKit.saveFile(
            bytes = backupFile.readBytes(),
            baseName = backupFile.name.substringBefore('.'),
            extension = backupFile.name.substringAfter('.')
        )
        return file?.name
    }

    sealed class ImportType {
        data object Interactive : ImportType()
        data class FromStream(val stream: InputStream) : ImportType()
    }

    /**
     * launch file picker and import database from the zipFile. Returns true if successful.
     */
    suspend fun importDatabase(importType: ImportType): String? {
        val (bytes, filename) = when (importType) {
            is ImportType.FromStream -> importType.stream.readBytes() to "Fallback"
            ImportType.Interactive -> {
                val file = FileKit.pickFile(type = PickerType.File()) ?: return null
                file.readBytes() to file.name
            }
        }

        val tmpFile = File(databaseDirectory, "tmp").apply { writeBytes(bytes) }

        ZipFile(tmpFile).use { zipFile ->
            val entries = zipFile.entries().toList()
            if (!entries.zip(backupFiles).all { (entry, backup) -> entry.name == backup.file.name }) return null

            entries.forEachIndexed { i, entry ->
                backupFiles[i].file.writeBytes(zipFile.getInputStream(entry).readBytes())

                when (entry.name) {
                    SETTINGS_FILENAME -> settingsDao = SettingsDao(settingsFile)
                    HISTORY_FILENAME -> historyDao = HistoryDao(historyFile)
                    PROGRAMS_FILENAME -> programDao = ProgramDao(programsFile)
                    EXERCISES_FILENAME -> exerciseDao = ExerciseDao(exercisesFile)
                }
            }
        }
        return filename
    }

    suspend fun factoryReset(): Boolean {
        val inputStream = context.resources.openRawResource(R.raw.fallback)
        return importDatabase(ImportType.FromStream(inputStream)) != null
    }

    init {
        BackgroundSlave.enqueue(Dispatchers.Main) { exercisesFile.read().ifEmpty { factoryReset() } }
    }
}

abstract class Dao<T : Entity>(protected val storageFile: StorageFile<List<T>>) {
    private val items = MutableStateFlow(storageFile.read())
    private var newId = items.value.maxOfOrNull { it.id } ?: 0

    val getAll: StateFlow<List<T>> = items.asStateFlow()

    /**
     * Update the item and return a boolean indicating if the operation was successful.
     */
    open fun update(item: T): Boolean {
        val existingIndex = items.value.indexOfFirst { it.id == item.id }
        if (existingIndex == -1) return false

        val state = items.updateAndGet { state -> state.bigListSet(existingIndex, item) }
        BackgroundSlave.enqueue { storageFile.write(state, log = true) }
        return true
    }

    /**
     * return id of the inserted item.
     */
    @Suppress("UNCHECKED_CAST")
    open fun insert(item: T): Int {
        val state = items.updateAndGet { state ->
            newId += 1
            state + item.clone(id = newId) as T
        }
        BackgroundSlave.enqueue { storageFile.write(state, log = true) }
        return newId
    }

    /**
     * return id of the inserted item or -1 if it was updated.
     */
    open fun upsert(item: T): Int = if (update(item)) -1 else insert(item)

    open fun delete(item: T) {
        val state = items.updateAndGet { state -> state - item }
        BackgroundSlave.enqueue { storageFile.write(state, log = true) }
    }

    open fun where(id: Int): Flow<T?> = items.map { it.filter { it.id == id }.firstOrNull() }
}

class SettingsDao(private val file: SettingsFile) {
    private val item = MutableStateFlow(file.read())
    val getSettings = item.asStateFlow()

    fun update(function: (Settings) -> Settings) {
        val state = item.updateAndGet { state -> function(state) }
        BackgroundSlave.enqueue { file.write(state, log = true) }
    }
}

class HistoryDao(file: HistoryFile) : Dao<HistoryRecord>(file) {
    suspend fun getUnfinishedBusiness(): HistoryRecord? =
        getAll.map { records -> records.filter { record -> record.workoutPhase != WorkoutPhase.Completed } }.firstOrNull()
            ?.firstOrNull()

    fun where(month: Month, year: Int): Flow<List<HistoryRecord>> = getAll.map { records ->
        records.filter { record -> record.date.month == month && record.date.year == year }
    }

    fun where(exercise: Exercise): Flow<List<Exercise>> = getAll.map { records ->
        records.flatMap { record -> record.workout.exercises.filter { it.id == exercise.id } }
            .sortedByDescending { exercise -> exercise.lastPerformedSet()?.doneTs }
    }
}

class ProgramDao(file: ProgramsFile) : Dao<Program>(file) {
    val getUserPrograms = getAll.map {
        it.filter { it correspondsNot Program.EmptyWorkout && !it.obsolete }.sortedByDescending { it.mostRecentWorkoutDate }
    }
    val getPerformable = getAll.map {
        it.filterNot { it.obsolete }.sortedByDescending { it.mostRecentWorkoutDate }
    }

    override fun delete(item: Program) {
        super.update(item.copy(obsolete = true))
    }
}

class ExerciseDao(file: ExerciseFile) : Dao<Exercise>(file) {
    private val bkTree = BKTree("Press") // This is the most frequent word in our database, followed by "Dumbbell" and "Barbell"

    val getSortedAlphabetically = getAll.map { it.sortedBy { it.name } }

    /**
     * Filter out exercises by name. This function provides an autocorrect/typo correcting algorithm that is
     * controlled with the [errorTolerance] and [ignoreCase] parameters.
     */
    fun where(name: String, errorTolerance: Int = 0, ignoreCase: Boolean = false) = getSortedAlphabetically.map { exercises ->
        val words = name.trim().split(' ')
        val predictedWords = words.mapNotNull { bkTree.search(it, errorTolerance, ignoreCase) }

        exercises.filter { exercise ->
            words.all { exercise.name.contains(it, ignoreCase) } || // not checking for empty string will show all exercises
                    (predictedWords.isNotEmpty() && predictedWords.all { exercise.name.contains(it, ignoreCase) })
        }
    }

    init {
        BackgroundSlave.enqueue {
            getSortedAlphabetically.first().forEach { exercise ->
                exercise.name.trim().split(' ').filter { word -> word.all { char -> char.isLetter() } }.forEach(bkTree::insert)
            }
        }
    }

    override fun insert(item: Exercise): Int {
        val item = item.copy(name = item.name.prep())
        bkTree.insert(item.name)
        return super.insert(item)
    }

    override fun update(item: Exercise): Boolean {
        return super.update(item.copy(name = item.name.prep()))
    }

    override fun upsert(item: Exercise): Int {
        return super.upsert(item.copy(name = item.name.prep()))
    }

    /**
     * Check [name] for duplicates and emptiness.
     *
     * @return Error message if [name] is bad and null if successfully validated.
     */
    fun validateName(name: String): String? {
        val name = name.prep()
        if (name.isBlank()) {
            return "Empty Exercise Name"
        }

        if (getAll.value.any { it.name == name }) {
            return "Duplicate Exercise Name"
        }

        return null
    }

    private fun String.prep() = this.trim().split(' ').joinToString(" ") { it.replaceFirstChar { it.uppercaseChar() } }
}
