package io.github.depermitto.bullettrain.database.entities

import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import io.github.depermitto.bullettrain.database.Compressor
import io.github.depermitto.bullettrain.database.Depot
import io.github.depermitto.bullettrain.database.entities.Theme.Dark
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
    fun isDarkMode(): Boolean = this == Dark || (this == FollowSystem && isSystemInDarkTheme())
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

class SettingsDao(private val depot: SettingsDepot) {
    internal val item = MutableStateFlow(depot.retrieve())
    val getSettings = item.asStateFlow()

    fun update(function: (Settings) -> Settings) {
        val state = item.updateAndGet { state -> function(state) }
        depot.stash(state)
        Log.i("db-${depot.file.name}", state.toString())
    }
}

class SettingsDepot(file: File) : Depot<Settings>(file) {
    override fun retrieve(): Settings = Json.decodeFromString(Compressor.uncompress(file.readText()))
    override fun stash(obj: Settings) = file.writeText(Compressor.compress(Json.encodeToString(obj)))
}
