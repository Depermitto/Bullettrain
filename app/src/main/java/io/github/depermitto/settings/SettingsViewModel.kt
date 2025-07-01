package io.github.depermitto.settings

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import io.github.depermitto.data.GymDatabase
import io.github.depermitto.database.Settings
import io.github.depermitto.settings.UnitSystem.Imperial
import io.github.depermitto.settings.UnitSystem.Metric
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.pickFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class UnitSystem { Metric, Imperial }

const val SETTINGS_FILENAME = "settings.json"
const val DB_FILENAME = "firetent.sqlite"

class SettingsViewModel(private val data: PersistentData) : ViewModel() {
    var settings by mutableStateOf(Json.decodeFromString<Settings>(data.settingsFile.readText()))
        private set

    suspend fun importDatabase(context: Context): String? {
        data.db.checkpoint()
        Log.i("DB IMPORT OLD", data.db.getProgramDao().getAll().toList().toString())
        val pickedFile = FileKit.pickFile(type = PickerType.File())
        if (pickedFile != null) {
            val importDb = Room.databaseBuilder<GymDatabase>(context = context, name = pickedFile.name).build()
            data.db.getProgramDao().deleteAll()
            Log.i("DB IMPORT NEW", importDb.getProgramDao().getAll().toList().toString())
            data.db.getProgramDao().upsert(*importDb.getProgramDao().getAll().toTypedArray())

            return pickedFile.name
        }
        return null
    }

    suspend fun exportDatabase(): String? {
        data.db.checkpoint()
        Log.i("DB EXPORT", data.db.getProgramDao().getAll().toList().toString())
        val pickedFile = FileKit.saveFile(
            bytes = data.dbFile.readBytes(),
            baseName = DB_FILENAME.substringBeforeLast('.'),
            extension = DB_FILENAME.substringAfterLast('.')
        )
        return pickedFile?.name
    }

    fun factoryReset() {
        data.db.checkpoint()
        data.dbFile.writeBytes(data.fallbackBytes)
    }

    fun setWeightUnit(unit: UnitSystem) {
        settings = settings.copy(unitSystem = unit)
        viewModelScope.launch(Dispatchers.IO) {
            data.settingsFile.writeBytes(
                Json.encodeToString<Settings>(this@SettingsViewModel.settings).encodeToByteArray()
            )
        }
    }

    fun weightUnit() = when (settings.unitSystem) {
        Metric -> "kg"
        Imperial -> "lbs"
    }

    companion object {
        fun Factory(data: PersistentData) = viewModelFactory { initializer { SettingsViewModel(data) } }
    }
}