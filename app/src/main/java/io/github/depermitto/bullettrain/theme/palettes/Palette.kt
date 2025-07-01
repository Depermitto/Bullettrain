package io.github.depermitto.bullettrain.theme.palettes

import androidx.compose.material3.ColorScheme
import io.github.depermitto.bullettrain.database.serializers.ColorSchemeSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Palette(
  @Serializable(with = ColorSchemeSerializer::class) val lightScheme: ColorScheme,
  @Serializable(with = ColorSchemeSerializer::class) val darkScheme: ColorScheme,
  val name: String,
) {
  infix fun corresponds(other: Palette) =
    this.lightScheme.primary == other.lightScheme.primary &&
      this.lightScheme.secondary == other.lightScheme.secondary &&
      this.lightScheme.tertiary == other.lightScheme.tertiary &&
      this.darkScheme.primary == other.darkScheme.primary &&
      this.darkScheme.secondary == other.darkScheme.secondary &&
      this.darkScheme.tertiary == other.darkScheme.tertiary

  infix fun correspondsNot(other: Palette) = !this.corresponds(other)
}
