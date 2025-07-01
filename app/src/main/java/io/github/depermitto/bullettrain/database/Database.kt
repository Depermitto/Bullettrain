package io.github.depermitto.bullettrain.database

import android.content.Context
import android.util.Log
import io.github.depermitto.bullettrain.R
import io.github.depermitto.bullettrain.database.entities.*
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.pickFile
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.Result
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking

class Database(private val dir: File, private val context: Context) {

  private val settingsDepot = SettingsDepot(File(dir, "settings"))
  private val historyDepot = HistoryDepot(File(dir, "history"))
  private val programsDepot = ProgramsDepot(File(dir, "programs"))
  private val exercisesDepot = ExerciseDepot(File(dir, "exercises"))

  private val depots = listOf(settingsDepot, historyDepot, programsDepot, exercisesDepot)

  init {
    if (depots.any { !it.file.exists() }) {
      settingsDepot.stash(Settings())
      historyDepot.stash(emptyList())
      programsDepot.stash(listOf(Program.EmptyWorkout))
      exercisesDepot.stash(emptyList())

      Log.i("db", "init completed.")
    }
  }

  val settingsDao = SettingsDao(settingsDepot)
  val historyDao = HistoryDao(historyDepot)
  val programDao = ProgramDao(programsDepot)
  val exerciseDao = ExerciseDao(exercisesDepot)

  /**
   * Launch a file picker, export the zipped database to the selected location, and return the name
   * of the exported file.
   *
   * @see [importDatabase]
   */
  suspend fun exportDatabase(): Result<String> {
    val backupFile =
      File(
        dir,
        "bullettrain-${LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))}.bk.zip",
      )
    ZipOutputStream(FileOutputStream(backupFile)).use { zipOutputStream ->
      for (backup in depots) {
        zipOutputStream.putNextEntry(ZipEntry(backup.file.name))
        zipOutputStream.write(backup.file.readBytes())
      }
    }
    val file =
      FileKit.saveFile(
        bytes = backupFile.readBytes(),
        baseName = backupFile.name.substringBefore('.'),
        extension = backupFile.name.substringAfter('.'),
      ) ?: return failure(Throwable(message = "Creating a data backup cancelled"))
    return success(file.name)
  }

  sealed class ImportType {
    data object Interactive : ImportType()

    data class FromStream(val stream: InputStream) : ImportType()
  }

  /**
   * Launch a file picker, import a data backup and return the name of the selected file.
   *
   * @see [exportDatabase]
   */
  suspend fun importDatabase(importType: ImportType): Result<String> {
    val (bytes, filename) =
      when (importType) {
        is ImportType.FromStream -> importType.stream.readBytes() to ""
        is ImportType.Interactive -> {
          val file =
            FileKit.pickFile(title = "Pick a data backup", type = PickerType.File())
              ?: return failure(Throwable(message = "Restoring a data backup cancelled"))
          file.readBytes() to file.name
        }
      }

    val tmpFile = File(dir, "tmp").apply { writeBytes(bytes) }
    try {
      ZipFile(tmpFile).use { zipFile ->
        val entries = zipFile.entries().toList()
        // we should probably check if file is not corrupt here
        if (!entries.zip(depots).all { (entry, backup) -> entry.name == backup.file.name }) {
          return failure(Throwable(message = "$filename is in an incorrect format"))
        }

        entries.forEachIndexed { i, entry ->
          when (
            val backupFile =
              depots[i].apply { file.writeBytes(zipFile.getInputStream(entry).readBytes()) }
          ) {
            is SettingsDepot -> settingsDao.item.update { SettingsDao(backupFile).item.value }
            is HistoryDepot -> historyDao.items.update { HistoryDao(backupFile).items.value }
            is ProgramsDepot -> programDao.items.update { ProgramDao(backupFile).items.value }
            is ExerciseDepot -> exerciseDao.items.update { ExerciseDao(backupFile).items.value }
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
    // this could be any DepotFile really
    if (exercisesDepot.retrieve().isEmpty() && runBlocking { factoryReset() })
      Log.i("db", "polluted database with default data")
  }
}
