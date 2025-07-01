package io.github.depermitto.database

import androidx.compose.runtime.*
import io.github.depermitto.settings.UnitSystem
import io.github.depermitto.settings.UnitSystem.Imperial
import io.github.depermitto.settings.UnitSystem.Metric
import io.github.depermitto.util.set
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.pickFile
import kotlinx.coroutines.flow.MutableStateFlow
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

    var settingsDao by mutableStateOf(SettingsDao(settingsFile))
        private set
    var historyDao by mutableStateOf(HistoryDao(historyFile))
        private set
    var programDao by mutableStateOf(ProgramDao(programsFile))
        private set
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
                    SETTINGS_FILENAME -> settingsDao = SettingsDao(settingsFile)
                    HISTORY_FILENAME -> historyDao = HistoryDao(historyFile)
                    PROGRAMS_FILENAME -> programDao = ProgramDao(programsFile)
                    EXERCISES_FILENAME -> exerciseDao = ExerciseDao(exercisesFile)
                }
            }
        }
        return true
    }
}

abstract class Dao<T : Entity>(protected val daoFile: File, startingItems: List<T>) {
    private val items = MutableStateFlow(startingItems)
    private var newId = items.value.maxOfOrNull { it.id } ?: 0

    val getAll = items.asStateFlow()

    fun update(item: T): Int {
        var id = item.id
        items.update { state ->
            val existingIndex = state.indexOfFirst { it.id == item.id }
            if (existingIndex == -1) {
                id = -1
                state
            } else state.set(existingIndex, item)
        }
        write(items.value)
        return id
    }

    fun insert(item: T): Int {
        items.update { state ->
            newId += 1
            state + item.clone(id = newId) as T
        }
        return newId
    }

    fun delete(item: T) {
        items.update { state -> state - item }
        write(items.value)
    }

    abstract fun write(state: List<T>)
}

class SettingsDao(private val settingsFile: File) {
    private var settings by mutableStateOf(Json.decodeFromString<Settings>(settingsFile.readText()))

    var unitSystem: UnitSystem = settings.unitSystem
        set(value) {
            settings = settings.copy(unitSystem = value)
            settingsFile.writeBytes(Json.encodeToString<Settings>(settings).encodeToByteArray())
        }

    fun weightUnit() = when (settings.unitSystem) {
        Metric -> "kg"
        Imperial -> "lbs"
    }
}

class HistoryDao(historyFile: File) : Dao<HistoryRecord>(historyFile, Json.decodeFromString(historyFile.readText())) {
    override fun write(state: List<HistoryRecord>) {
        daoFile.writeText(Json.encodeToString(state))
    }
}

class ProgramDao(programsFile: File) : Dao<Program>(programsFile, Json.decodeFromString(programsFile.readText())) {
    override fun write(state: List<Program>) {
        daoFile.writeText(Json.encodeToString(state))
    }
}

class ExerciseDao(exerciseFile: File) : Dao<Exercise>(exerciseFile, Json.decodeFromString(exerciseFile.readText())) {
    override fun write(state: List<Exercise>) {
        daoFile.writeText(Json.encodeToString(state))
    }
}