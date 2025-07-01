package io.github.depermitto.bullettrain.database.entities

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import io.github.depermitto.bullettrain.database.BackgroundSlave
import io.github.depermitto.bullettrain.database.Compressor
import io.github.depermitto.bullettrain.database.StorageFile
import io.github.depermitto.bullettrain.theme.palettes.Palette
import io.github.depermitto.bullettrain.theme.palettes.RhinoButtercupPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

enum class UnitSystem {
    Metric, Imperial;

    fun weightUnit(): String = when (this) {
        Metric -> "kg"
        Imperial -> "lbs"
    }
}

enum class Theme {
    FollowSystem, Light, Dark;

    @Composable
    fun isDarkMode(): Boolean = (this == FollowSystem && (isSystemInDarkTheme())) || this == Dark
}

@Immutable
@Serializable
data class Settings(
    val unitSystem: UnitSystem = UnitSystem.Metric,
    val theme: Theme = Theme.FollowSystem,
    val palette: Palette = RhinoButtercupPalette,
    val dynamicColor: Boolean = false,
    val trueBlack: Boolean = false
)

class SettingsDao(private val file: SettingsFile) {
    internal val item = MutableStateFlow(file.read())
    val getSettings = item.asStateFlow()

    fun update(function: (Settings) -> Settings) {
        val state = item.updateAndGet { state -> function(state) }
        BackgroundSlave.enqueue { file.writeLog(state) }
    }
}

class SettingsFile(file: File) : StorageFile<Settings>(file) {
    override fun read(): Settings = Json.decodeFromString(Compressor.uncompress(file.readText()))
    override fun writeNoLog(obj: Settings) = file.writeText(Compressor.compress(Json.encodeToString(obj)))
}
