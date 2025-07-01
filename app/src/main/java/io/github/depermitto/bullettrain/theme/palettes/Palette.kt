package io.github.depermitto.bullettrain.theme.palettes

import androidx.compose.material3.ColorScheme
import io.github.depermitto.bullettrain.database.serializers.ColorSchemeSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Palette(
    @Serializable(with = ColorSchemeSerializer::class) val lightScheme: ColorScheme,
    @Serializable(with = ColorSchemeSerializer::class) val darkScheme: ColorScheme,
    val name: String
)
