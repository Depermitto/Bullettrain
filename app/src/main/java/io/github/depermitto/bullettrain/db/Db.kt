package io.github.depermitto.bullettrain.db

import android.content.Context
import android.util.Log
import io.github.depermitto.bullettrain.R
import io.github.depermitto.bullettrain.protos.DbProto
import io.github.depermitto.bullettrain.util.DateFormatters
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.pickFile
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.time.LocalDate
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipException
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlinx.coroutines.flow.update

class Db(dir: File, private val context: Context) {
  private val dbFile = dir.resolve("db")
  val settingsDao: SettingsDao
  val historyDao: HistoryDao
  val programDao: ProgramDao
  val exerciseDao: ExerciseDao

  /**
   * Launch a file picker, export the zipped database to the selected location, and return the name
   * of the exported file.
   *
   * @see [importDatabaseInteractively]
   */
  suspend fun exportDatabaseInteractively(): Result<String> {
    val now = LocalDate.now().format(DateFormatters.yyyy_MM_dd)
    val bytes =
      ByteArrayOutputStream().use { out ->
        GZIPOutputStream(out).use { gzip -> FileInputStream(dbFile).use { it.copyTo(gzip) } }
        out.toByteArray()
      }
    val file =
      FileKit.saveFile(bytes = bytes, baseName = now, extension = "bk.gz")
        ?: return failure(Throwable(message = "Creating a data backup cancelled"))
    return success(file.name)
  }

  fun getDb(): DbProto.Db =
    DbProto.Db.newBuilder()
      .addAllDescriptors(exerciseDao.items.value)
      .addAllPrograms(programDao.items.value)
      .addAllRecords(historyDao.items.value)
      .setSettings(settingsDao.item.value)
      .build()

  fun exportDatabase() {
    val db = getDb()
    FileOutputStream(dbFile).use { db.writeTo(it) }
  }

  private fun importDatabase(inputStream: InputStream): Result<Unit> {
    try {
      val db = GZIPInputStream(inputStream).use { DbProto.Db.parseFrom(it) }
      exerciseDao.items.update { db.descriptorsList }
      programDao.items.update { db.programsList }
      historyDao.items.update { db.recordsList }
      settingsDao.item.update { db.settings }
      FileOutputStream(dbFile).use { db.writeTo(it) }
    } catch (_: ZipException) {
      return failure(Throwable(message = "Not a data backup."))
    } catch (err: Throwable) {
      return failure(err)
    }
    return success(Unit)
  }

  /**
   * Launch a file picker, import a data backup and return the name of the selected file.
   *
   * @see [exportDatabaseInteractively]
   */
  suspend fun importDatabaseInteractively(): Result<String> {
    val pickedFile =
      FileKit.pickFile(title = "Pick a data backup")
        ?: return failure(Throwable(message = "User cancelled."))
    val inputStream =
      context.contentResolver.openInputStream(pickedFile.uri)
        ?: return failure(Throwable(message = "Could not open file."))
    return inputStream.use {
      importDatabase(it)
        .fold(
          onFailure = { throwable -> failure(throwable) },
          onSuccess = { success(pickedFile.name) },
        )
    }
  }

  fun factoryReset(): Boolean {
    return context.resources.openRawResource(R.raw.fallback).use { inputStream ->
      importDatabase(inputStream).isSuccess
    }
  }

  init {
    if (!dbFile.exists()) {
      val db = DbProto.Db.getDefaultInstance()
      settingsDao = SettingsDao(db.settings)
      historyDao = HistoryDao(db.recordsList)
      programDao = ProgramDao(db.programsList)
      exerciseDao = ExerciseDao(db.descriptorsList)
      this.factoryReset()
      Log.i("DB", "Database initialized.")
    } else {
      val db = FileInputStream(dbFile).use { DbProto.Db.parseFrom(it) }
      settingsDao = SettingsDao(db.settings)
      historyDao = HistoryDao(db.recordsList)
      programDao = ProgramDao(db.programsList)
      exerciseDao = ExerciseDao(db.descriptorsList)
    }
  }
}
