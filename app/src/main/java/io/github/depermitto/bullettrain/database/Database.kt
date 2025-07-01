package io.github.depermitto.bullettrain.database

import android.content.Context
import android.util.Log
import io.github.depermitto.bullettrain.R
import io.github.depermitto.bullettrain.database.entities.*
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.pickFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

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
            settingsFile.writeLog(Settings())
            historyFile.writeLog(emptyList())
            programsFile.writeLog(listOf(Program.EmptyWorkout))
            exercisesFile.writeLog(emptyList())
        }
    }

    private val backupFile = File(databaseDirectory, "bullet-train.bk.zip")

    val settingsDao = SettingsDao(settingsFile)
    val historyDao = HistoryDao(historyFile)
    val programDao = ProgramDao(programsFile)
    val exerciseDao = ExerciseDao(exercisesFile)

    /**
     * Launch file picker and export the zipped database to it. Returns name the file the database was exported to  if successful.
     * @see [importDatabase]
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
     * Launch file picker and import database from the zipFile. Returns true if successful.
     * @see [exportDatabase]
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
            // we should probably check if file is not corrupt here
            if (!entries.zip(backupFiles).all { (entry, backup) -> entry.name == backup.file.name }) return null

            entries.forEachIndexed { i, entry ->
                when (val backupFile = backupFiles[i].apply { file.writeBytes(zipFile.getInputStream(entry).readBytes()) }) {
                    is SettingsFile -> settingsDao.item.update { SettingsDao(backupFile).item.value }
                    is HistoryFile -> historyDao.items.update { HistoryDao(backupFile).items.value }
                    is ProgramsFile -> programDao.items.update { ProgramDao(backupFile).items.value }
                    is ExerciseFile -> exerciseDao.items.update { ExerciseDao(backupFile).items.value }
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
        BackgroundSlave.enqueue(Dispatchers.Main) {
            // this could be any StorageFile really
            if (exercisesFile.read().isEmpty() && factoryReset()) Log.i("db-global", "polluted database with default data")
        }
    }
}