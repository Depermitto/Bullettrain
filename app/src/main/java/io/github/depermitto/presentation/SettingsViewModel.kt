package io.github.depermitto.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.depermitto.data.GymDatabase
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.pickFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

enum class UnitSystem {
    Metric, Imperial;

    fun weightUnit() = when (this) {
        Metric -> "kg"
        Imperial -> "lbs"
    }
}

const val SETTINGS_FILENAME = "settings.json"
const val DB_FILENAME = "firetent.sqlite"

@Serializable
data class Settings(val unitSystem: UnitSystem = UnitSystem.Metric)

class SettingsViewModel(
    private val db: GymDatabase,
    private val dbFile: File,
    private val fallbackBytes: ByteArray,
    private val settingsFile: File,
) : ViewModel() {
    var toastMessage by mutableStateOf("")
        private set
    var settings by mutableStateOf(Json.decodeFromString<Settings>(settingsFile.readText()))
        private set

    fun importDatabase() = viewModelScope.launch(Dispatchers.IO) {
        val pickedFile = FileKit.pickFile(type = PickerType.File())
        if (pickedFile != null) {
            db.checkpoint()
            dbFile.writeBytes(pickedFile.readBytes())
            toastMessage = "Successfully Imported \"${pickedFile.name}\""
        }
    }

    fun exportDatabase() = viewModelScope.launch(Dispatchers.IO) {
        db.checkpoint()
        val pickedFile = FileKit.saveFile(
            bytes = dbFile.readBytes(),
            baseName = DB_FILENAME.substringBeforeLast('.'),
            extension = DB_FILENAME.substringAfterLast('.')
        )
        if (pickedFile != null) {
            toastMessage = "Successfully Saved To \"${pickedFile.name}\""
        }
    }

    fun factoryReset() = viewModelScope.launch(Dispatchers.IO) {
        db.checkpoint()
        dbFile.writeBytes(fallbackBytes)
        toastMessage = "Factory Reset Complete"
    }

    fun setWeightUnit(unit: UnitSystem) {
        settings = settings.copy(unitSystem = unit)
        viewModelScope.launch(Dispatchers.IO) {
            settingsFile.writeBytes(Json.encodeToString<Settings>(this@SettingsViewModel.settings).encodeToByteArray())
        }
    }

    companion object {
        fun Factory(db: GymDatabase, dbFile: File, fallbackBytes: ByteArray, settingsFile: File) =
            viewModelFactory { initializer { SettingsViewModel(db, dbFile, fallbackBytes, settingsFile) } }
    }
}