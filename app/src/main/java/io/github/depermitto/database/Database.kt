package io.github.depermitto.database

import androidx.compose.runtime.*
import io.github.depermitto.util.set
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.pickFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class Database(private val databaseDirectory: File) {
    companion object {
        const val SETTINGS_FILENAME = "settings.json"
        const val HISTORY_FILENAME = "history.json"
        const val PROGRAMS_FILENAME = "programs.json"
        const val EXERCISES_FILENAME = "exercises.json"
    }

    private val settingsFile = File(databaseDirectory, SETTINGS_FILENAME)
    private val historyFile = File(databaseDirectory, HISTORY_FILENAME)
    private val programsFile = File(databaseDirectory, PROGRAMS_FILENAME)
    private val exercisesFile = File(databaseDirectory, EXERCISES_FILENAME)
    private val backupFile = File(databaseDirectory, "bullet-train.bk.zip")

    val backupFiles = listOf(settingsFile, historyFile, programsFile, exercisesFile)

    var exerciseDao by mutableStateOf(ExerciseDao(exercisesFile))
        private set

    init {
        if (!settingsFile.exists()) {
            settingsFile.writeText(Json.encodeToString(Settings()))
        }

        if (!historyFile.exists()) {
            historyFile.writeText(Json.encodeToString(listOf<HistoryRecord>()))
        }

        if (!programsFile.exists()) {
            programsFile.writeText(Json.encodeToString(listOf<Program>()))
        }

        if (!exercisesFile.exists()) {
            exercisesFile.writeText(Json.encodeToString(listOf<Exercise>()))
        }
    }

    /**
     * launch file picker and export the zipped database to it. Returns true if successful.
     */
    suspend fun exportDatabase(): Boolean {
        ZipOutputStream(FileOutputStream(backupFile)).use { zipOutputStream ->
            for (backup in backupFiles) {
                zipOutputStream.putNextEntry(ZipEntry(backup.name))
                zipOutputStream.write(backup.readBytes())
            }
        }
        val file = FileKit.saveFile(
            bytes = backupFile.readBytes(),
            baseName = backupFile.name.substringBefore('.'),
            extension = backupFile.name.substringAfter('.')
        )
        return file != null
    }

    /**
     * launch file picker and import database from the zipFile. Returns true if successful.
     */
    suspend fun importDatabase(): Boolean {
        val file = FileKit.pickFile(type = PickerType.File()) ?: return false
        val tmpFile = File(databaseDirectory, "tmp").apply { writeBytes(file.readBytes()) }

        ZipFile(tmpFile).use { zipFile ->
            val entries = zipFile.entries().toList()
            if (!entries.zip(backupFiles).all { (entry, backup) -> entry.name == backup.name }) return false

            entries.forEachIndexed { i, entry ->
                backupFiles[i].writeBytes(zipFile.getInputStream(entry).readBytes())

                when (entry.name) {
                    SETTINGS_FILENAME -> {}
                    HISTORY_FILENAME -> {}
                    PROGRAMS_FILENAME -> {}
                    EXERCISES_FILENAME -> exerciseDao = ExerciseDao(exercisesFile)
                }
            }
        }
        return true
    }
}

interface Dao<T> {
    val getAll: StateFlow<List<T>>

    /**
     * return index of the updated item. -1 if not in database.
     */
    fun update(item: T): Int

    /**
     * return index of the newly inserted item.
     */
    fun insert(item: T): Int
    fun delete(item: T)
}

class ExerciseDao(private val exerciseFile: File) : Dao<Exercise> {
    private val exercises = MutableStateFlow(Json.decodeFromString<List<Exercise>>(exerciseFile.readText()))
    private var id = exercises.value.maxOfOrNull { it.exerciseId } ?: 0

    override val getAll = exercises.asStateFlow()

    override fun update(item: Exercise): Int {
        var id = item.exerciseId
        write { state ->
            val existingIndex = state.indexOfFirst { it.exerciseId == item.exerciseId }
            if (existingIndex == -1) {
                id = -1
                state
            } else state.set(existingIndex, item)
        }
        return id
    }

    override fun insert(item: Exercise): Int {
        write { state ->
            id += 1
            state + item.copy(exerciseId = id)
        }
        return id
    }


    override fun delete(item: Exercise) = write { state -> state - item }

    private fun write(operation: (List<Exercise>) -> List<Exercise>) {
        exercises.update { state -> operation(state) }
        exerciseFile.writeText(Json.encodeToString(exercises.value))
    }
}
