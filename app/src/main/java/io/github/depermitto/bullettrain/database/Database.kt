package io.github.depermitto.bullettrain.database

import android.content.Context
import android.util.Log
import io.github.depermitto.bullettrain.R
import io.github.depermitto.bullettrain.database.daos.ExerciseDao
import io.github.depermitto.bullettrain.database.daos.HistoryDao
import io.github.depermitto.bullettrain.database.daos.ProgramDao
import io.github.depermitto.bullettrain.database.daos.SettingsDao
import io.github.depermitto.bullettrain.database.entities.ExerciseDescriptor
import io.github.depermitto.bullettrain.database.entities.HistoryRecord
import io.github.depermitto.bullettrain.database.entities.Program
import io.github.depermitto.bullettrain.database.entities.Settings
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
import kotlin.io.path.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Database(private val dir: Path, private val context: Context) {
  private val _sloc = dir / "settings"
  private val _hloc = dir / "history"
  private val _ploc = dir / "programs"
  private val _eloc = dir / "exercises"
  private val _locs = listOf(_sloc, _hloc, _ploc, _eloc)

  val settingsDao: SettingsDao
  val historyDao: HistoryDao
  val programDao: ProgramDao
  val exerciseDao: ExerciseDao

  private inline fun <reified T> decodeAndUncompress(filepath: Path) =
    Json.decodeFromString<T>(Compressor.uncompress(filepath.readText()))

  private inline fun <reified T> compressAndWrite(filepath: Path, data: T) =
    filepath.writeText(Compressor.compress(Json.encodeToString(data)))

  fun saveAppDataToPersistentStorage() {
    compressAndWrite(_sloc, settingsDao.item.value)
    compressAndWrite(_hloc, historyDao.items.value)
    compressAndWrite(_ploc, programDao.items.value)
    compressAndWrite(_eloc, exerciseDao.items.value)
  }

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
        for (loc in _locs) {
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
            if (!entries.zip(_locs).all { (entry, backup) -> entry.name == backup.name }) {
              return@withContext failure(Throwable(message = "data is in incorrect format"))
            }

            entries.forEachIndexed { i, entry ->
              val loc = _locs[i]
              loc.writeBytes(zipFile.getInputStream(entry).readBytes())

              when (loc.nameWithoutExtension) {
                "settings" -> settingsDao.item.update { decodeAndUncompress<Settings>(loc) }
                "history" ->
                  historyDao.items.update { decodeAndUncompress<List<HistoryRecord>>(loc) }
                "programs" -> programDao.items.update { decodeAndUncompress<List<Program>>(loc) }
                "exercises" ->
                  exerciseDao.items.update { decodeAndUncompress<List<ExerciseDescriptor>>(loc) }
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
    if (_locs.any { !it.exists() }) {
      settingsDao = SettingsDao(Settings())
      historyDao = HistoryDao(emptyList())
      programDao = ProgramDao(emptyList())
      exerciseDao = ExerciseDao(emptyList())
      saveAppDataToPersistentStorage()

      runBlocking { factoryReset() }
      Log.i("DB", "Database initialized.")
    } else {
      settingsDao = SettingsDao(decodeAndUncompress<Settings>(_sloc))
      historyDao = HistoryDao(decodeAndUncompress<List<HistoryRecord>>(_hloc))
      programDao = ProgramDao(decodeAndUncompress<List<Program>>(_ploc))
      exerciseDao = ExerciseDao(decodeAndUncompress<List<ExerciseDescriptor>>(_eloc))
    }
  }
}
