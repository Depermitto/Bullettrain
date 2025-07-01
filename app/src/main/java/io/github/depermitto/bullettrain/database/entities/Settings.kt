package io.github.depermitto.bullettrain.database.entities

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import io.github.depermitto.bullettrain.theme.palettes.Palette
import io.github.depermitto.bullettrain.theme.palettes.RhinoButtercupPalette
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
