package io.github.depermitto.bullettrain.database.entities

import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import io.github.depermitto.bullettrain.theme.palettes.Palette
import io.github.depermitto.bullettrain.theme.palettes.RhinoButtercupPalette
import io.github.depermitto.bullettrain.util.loadAndUncompressData
import io.github.depermitto.bullettrain.util.saveAndCompressData
import java.nio.file.Path
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.serialization.Serializable

enum class UnitSystem {
  Metric,
  Imperial;

  fun weightUnit(): String =
    when (this) {
      Metric -> "kg"
      Imperial -> "lbs"
    }
}

enum class Theme {
  FollowSystem,
  Light,
  Dark;

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
  val trueBlack: Boolean = false,
)

class SettingsDao(private val filepath: Path) {
  internal val item = MutableStateFlow<Settings>(loadAndUncompressData(filepath))
  val getSettings = item.asStateFlow()

  fun update(function: (Settings) -> Settings) {
    val state = item.updateAndGet { state -> function(state) }
    saveAndCompressData(filepath, state)
    Log.i("db-${filepath}", state.toString())
  }
}
