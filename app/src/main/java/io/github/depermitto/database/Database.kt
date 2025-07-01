package io.github.depermitto.database

import androidx.compose.runtime.*
import io.github.depermitto.train.WorkoutPhase
import io.github.depermitto.util.bigListSet
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.pickFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.updateAndGet
import java.io.File
import java.io.FileOutputStream
import java.time.Month
import java.time.ZoneId
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

private const val SETTINGS_FILENAME = "settings"
private const val HISTORY_FILENAME = "history"
private const val PROGRAMS_FILENAME = "programs"
private const val EXERCISES_FILENAME = "exercises"

class Database(private val databaseDirectory: File) {

    private val settingsFile = SettingsFile(File(databaseDirectory, SETTINGS_FILENAME))
    private val historyFile = HistoryFile(File(databaseDirectory, HISTORY_FILENAME))
    private val programsFile = ProgramsFile(File(databaseDirectory, PROGRAMS_FILENAME))
    private val exercisesFile = ExerciseFile(File(databaseDirectory, EXERCISES_FILENAME))

    init {
        if (!settingsFile.file.exists()) settingsFile.write(Settings())
        if (!historyFile.file.exists()) historyFile.write(emptyList())
        if (!programsFile.file.exists()) programsFile.write(listOf(ProgramDao.EmptyWorkout))
        if (!exercisesFile.file.exists()) exercisesFile.write(emptyList())
    }

    private val backupFile = File(databaseDirectory, "bullet-train.bk.zip")
    private val backupFiles = listOf(settingsFile, historyFile, programsFile, exercisesFile)

    var settingsDao by mutableStateOf(SettingsDao(settingsFile))
        private set
    var historyDao by mutableStateOf(HistoryDao(historyFile))
        private set
    var programDao by mutableStateOf(ProgramDao(programsFile))
        private set
    var exerciseDao by mutableStateOf(ExerciseDao(exercisesFile))
        private set


    /**
     * launch file picker and export the zipped database to it. Returns true if successful.
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

    /**
     * launch file picker and import database from the zipFile. Returns true if successful.
     */
    suspend fun importDatabase(): String? {
        val file = FileKit.pickFile(type = PickerType.File()) ?: return null
        val tmpFile = File(databaseDirectory, "tmp").apply { writeBytes(file.readBytes()) }

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
        return file.name
    }
}

abstract class Dao<T : Entity>(protected val storageFile: StorageFile<List<T>>) {
    private val items = MutableStateFlow(storageFile.read())
    private var newId = items.value.maxOfOrNull { it.id } ?: 0

    val getAll: StateFlow<List<T>> = items.asStateFlow()

    /**
     * Update the item and return a boolean indicating if the operation was successful.
     * i.e. return true if item is present in the database.
     */
    fun update(item: T): Boolean {
        val existingIndex = items.value.indexOfFirst { it.id == item.id }
        if (existingIndex == -1) return false

        val state = items.updateAndGet { state -> state.bigListSet(existingIndex, item) }
        BackgroundSlave.enqueue { storageFile.write(state) }
        return true
    }

    /**
     * return id of the inserted item.
     */
    @Suppress("UNCHECKED_CAST")
    fun insert(item: T): Int {
        val state = items.updateAndGet { state ->
            newId += 1
            state + item.clone(id = newId) as T
        }
        BackgroundSlave.enqueue { storageFile.write(state) }
        return newId
    }

    /**
     * return id of the inserted item or -1 if it existed in the database.
     */
    fun upsert(item: T): Int = if (update(item)) -1 else insert(item)

    fun delete(item: T) {
        val state = items.updateAndGet { state -> state - item }
        BackgroundSlave.enqueue { storageFile.write(state) }
    }

    fun where(id: Int): Flow<T?> = items.map { it.filter { it.id == id }.firstOrNull() }
}

class SettingsDao(private val file: SettingsFile) {
    private var settings by mutableStateOf(file.read())

    var unitSystem: UnitSystem = settings.unitSystem
        set(value) {
            settings = settings.copy(unitSystem = value)
            file.write(settings)
        }

    fun weightUnit() = when (settings.unitSystem) {
        UnitSystem.Metric -> "kg"
        UnitSystem.Imperial -> "lbs"
    }
}

class HistoryDao(file: HistoryFile) : Dao<HistoryRecord>(file) {
    suspend fun getUnfinishedBusiness(): HistoryRecord? =
        getAll.map { records -> records.filter { record -> record.workoutPhase == WorkoutPhase.During } }.firstOrNull()
            ?.firstOrNull()

    fun where(month: Month, year: Int): Flow<List<HistoryRecord>> = getAll.map { records ->
        records.filter { record ->
            val date = record.date.atZone(ZoneId.systemDefault())
            date.month == month && date.year == year
        }
    }
}

class ProgramDao(file: ProgramsFile) : Dao<Program>(file) {
    companion object {
        val EmptyWorkout = Program(id = -1, name = "Empty Workout")
    }

    val getAlmostAll = getAll.map { it.filterNot { it.id == EmptyWorkout.id && it.name == EmptyWorkout.name } }
}

class ExerciseDao(file: ExerciseFile) : Dao<Exercise>(file)
