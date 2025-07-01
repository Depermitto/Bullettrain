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
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.Result
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

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

    val settingsDao = SettingsDao(settingsFile)
    val historyDao = HistoryDao(historyFile)
    val programDao = ProgramDao(programsFile)
    val exerciseDao = ExerciseDao(exercisesFile)

    /**
     * Launch a file picker, export the zipped database to the selected location, and return the name of the exported file.
     * @see [importDatabase]
     */
    suspend fun exportDatabase(): Result<String> {
        val backupFile = File(databaseDirectory, "bullet-train.bk.zip")
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
        ) ?: return failure(Throwable(message = "Creating a data backup cancelled"))
        return success(file.name)
    }

    sealed class ImportType {
        data object Interactive : ImportType()
        data class FromStream(val stream: InputStream) : ImportType()
    }

    /**
     * Launch a file picker, import a data backup and return the name of the selected file.
     * @see [exportDatabase]
     */
    suspend fun importDatabase(importType: ImportType): Result<String> {
        val (bytes, filename) = when (importType) {
            is ImportType.FromStream -> importType.stream.readBytes() to ""
            is ImportType.Interactive -> {
                val file = FileKit.pickFile(title = "Pick a data backup", type = PickerType.File())
                    ?: return failure(Throwable(message = "Restoring a data backup cancelled"))
                file.readBytes() to file.name
            }
        }

        val tmpFile = File(databaseDirectory, "tmp").apply { writeBytes(bytes) }
        try {
            ZipFile(tmpFile).use { zipFile ->
                val entries = zipFile.entries().toList()
                // we should probably check if file is not corrupt here
                if (!entries.zip(backupFiles).all { (entry, backup) -> entry.name == backup.file.name }) {
                    return failure(Throwable(message = "$filename is in an incorrect format"))
                }

                entries.forEachIndexed { i, entry ->
                    when (val backupFile = backupFiles[i].apply { file.writeBytes(zipFile.getInputStream(entry).readBytes()) }) {
                        is SettingsFile -> settingsDao.item.update { SettingsDao(backupFile).item.value }
                        is HistoryFile -> historyDao.items.update { HistoryDao(backupFile).items.value }
                        is ProgramsFile -> programDao.items.update { ProgramDao(backupFile).items.value }
                        is ExerciseFile -> exerciseDao.items.update { ExerciseDao(backupFile).items.value }
                    }
                }
            }
        } catch (_: ZipException) {
            return failure(Throwable(message = "$filename is not a data backup"))
        } catch (err: Throwable) {
            return failure(err)
        }
        return success(filename)
    }

    suspend fun factoryReset(): Boolean {
        val inputStream = context.resources.openRawResource(R.raw.fallback)
        return importDatabase(ImportType.FromStream(inputStream)).isSuccess
    }

    init {
        BackgroundSlave.enqueue(Dispatchers.Main) {
            // this could be any StorageFile really
            if (exercisesFile.read().isEmpty() && factoryReset()) Log.i("db-global", "polluted database with default data")
        }
    }
}