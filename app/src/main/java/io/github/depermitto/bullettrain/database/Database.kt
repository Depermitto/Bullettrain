package io.github.depermitto.bullettrain.database

import android.content.Context
import android.util.Log
import io.github.depermitto.bullettrain.R
import io.github.depermitto.bullettrain.database.entities.ExerciseDao
import io.github.depermitto.bullettrain.database.entities.ExerciseDescriptor
import io.github.depermitto.bullettrain.database.entities.HistoryDao
import io.github.depermitto.bullettrain.database.entities.HistoryRecord
import io.github.depermitto.bullettrain.database.entities.Program
import io.github.depermitto.bullettrain.database.entities.ProgramDao
import io.github.depermitto.bullettrain.database.entities.Settings
import io.github.depermitto.bullettrain.database.entities.SettingsDao
import io.github.depermitto.bullettrain.util.loadAndUncompressData
import io.github.depermitto.bullettrain.util.saveAndCompressData
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.pickFile
import java.io.FileOutputStream
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlin.io.path.absolutePathString
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class Database(private val dir: Path, private val context: Context) {

  private val settingsLoc = dir / "settings"
  private val historyLoc = dir / "history"
  private val programsLoc = dir / "programs"
  private val exercisesLoc = dir / "exercises"
  private val dataLocations = listOf(settingsLoc, historyLoc, programsLoc, exercisesLoc)

  init {
    if (!settingsLoc.exists()) {
      saveAndCompressData(settingsLoc, Settings())
      Log.i("db", "settings initialized.")
    }
    if (!historyLoc.exists()) {
      saveAndCompressData(historyLoc, emptyList<HistoryRecord>())
      Log.i("db", "history initialized.")
    }
    if (!programsLoc.exists()) {
      saveAndCompressData(historyLoc, listOf(Program.EmptyWorkout))
      Log.i("db", "programs initialized.")
    }
    if (!exercisesLoc.exists()) {
      saveAndCompressData(exercisesLoc, emptyList<ExerciseDescriptor>())
      Log.i("db", "exercises initialized.")
    }
  }

  val settingsDao = SettingsDao(settingsLoc)
  val historyDao = HistoryDao(historyLoc)
  val programDao = ProgramDao(programsLoc)
  val exerciseDao = ExerciseDao(exercisesLoc)

  /**
   * Launch a file picker, export the zipped database to the selected location, and return the name
   * of the exported file.
   *
   * @see [importDatabase]
   */
  suspend fun exportDatabase(): Result<String> {
    val now = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    val backup = dir / "bullettrain-$now.bk.zip"
    withContext(Dispatchers.IO) {
      ZipOutputStream(FileOutputStream(backup.absolutePathString())).use { zipOutputStream ->
        for (loc in dataLocations) {
          zipOutputStream.putNextEntry(ZipEntry(loc.name))
          zipOutputStream.write(loc.readBytes())
        }
      }
    }
    val file =
      FileKit.saveFile(
        bytes = backup.readBytes(),
        baseName = backup.name.substringBefore('.'),
        extension = backup.name.substringAfter('.'),
      ) ?: return failure(Throwable(message = "Creating a data backup cancelled"))
    return success(file.name)
  }

  private suspend fun importDatabaseFromBytes(bytes: ByteArray): Result<Unit> {
    val tmpFile = (dir / "tmp").toFile()
    tmpFile.writeBytes(bytes)
    try {
      val result: Result<Unit> =
        withContext(Dispatchers.IO) {
          ZipFile(tmpFile).use { zipFile ->
            val entries = zipFile.entries().toList()
            // we should probably check if file is not corrupt here
            if (!entries.zip(dataLocations).all { (entry, backup) -> entry.name == backup.name }) {
              return@withContext failure(Throwable(message = "data is in an incorrect format"))
            }

            entries.forEachIndexed { i, entry ->
              val loc = dataLocations[i]
              loc.writeBytes(zipFile.getInputStream(entry).readBytes())

              when (loc.nameWithoutExtension) {
                "settings" -> settingsDao.item.update { SettingsDao(loc).item.value }
                "history" -> historyDao.items.update { HistoryDao(loc).items.value }
                "programs" -> programDao.items.update { ProgramDao(loc).items.value }
                "exercises" -> exerciseDao.items.update { ExerciseDao(loc).items.value }
              }
            }
          }
          return@withContext success(Unit)
        }
      if (result.isFailure) return result
    } catch (_: ZipException) {
      return failure(Throwable(message = "not a data backup"))
    } catch (err: Throwable) {
      return failure(err)
    }
    return success(Unit)
  }

  /**
   * Launch a file picker, import a data backup and return the name of the selected file.
   *
   * @see [exportDatabase]
   */
  suspend fun importDatabase(): Result<String> {
    val pickedFile =
      FileKit.pickFile(title = "Pick a data backup", type = PickerType.File())
        ?: return failure(Throwable(message = "Restoring a data backup cancelled"))

    return importDatabaseFromBytes(pickedFile.readBytes())
      .fold(onFailure = { failure(it) }, onSuccess = { success(pickedFile.name) })
  }

  suspend fun factoryReset(): Boolean {
    val stream = context.resources.openRawResource(R.raw.fallback)
    return importDatabaseFromBytes(stream.readBytes()).isSuccess
  }

  init {
    if (
      loadAndUncompressData<List<Program>>(programsLoc).isEmpty() && runBlocking { factoryReset() }
    )
      Log.i("db", "polluted database with default data")
  }
}
