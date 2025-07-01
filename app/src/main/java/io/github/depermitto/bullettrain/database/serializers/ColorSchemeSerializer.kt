package io.github.depermitto.bullettrain.database.serializers

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
private data class SerializableColorScheme(
  @Serializable(with = ColorSerializer::class) val primary: Color,
  @Serializable(with = ColorSerializer::class) val onPrimary: Color,
  @Serializable(with = ColorSerializer::class) val primaryContainer: Color,
  @Serializable(with = ColorSerializer::class) val onPrimaryContainer: Color,
  @Serializable(with = ColorSerializer::class) val inversePrimary: Color,
  @Serializable(with = ColorSerializer::class) val secondary: Color,
  @Serializable(with = ColorSerializer::class) val onSecondary: Color,
  @Serializable(with = ColorSerializer::class) val secondaryContainer: Color,
  @Serializable(with = ColorSerializer::class) val onSecondaryContainer: Color,
  @Serializable(with = ColorSerializer::class) val tertiary: Color,
  @Serializable(with = ColorSerializer::class) val onTertiary: Color,
  @Serializable(with = ColorSerializer::class) val tertiaryContainer: Color,
  @Serializable(with = ColorSerializer::class) val onTertiaryContainer: Color,
  @Serializable(with = ColorSerializer::class) val background: Color,
  @Serializable(with = ColorSerializer::class) val onBackground: Color,
  @Serializable(with = ColorSerializer::class) val surface: Color,
  @Serializable(with = ColorSerializer::class) val onSurface: Color,
  @Serializable(with = ColorSerializer::class) val surfaceVariant: Color,
  @Serializable(with = ColorSerializer::class) val onSurfaceVariant: Color,
  @Serializable(with = ColorSerializer::class) val surfaceTint: Color,
  @Serializable(with = ColorSerializer::class) val inverseSurface: Color,
  @Serializable(with = ColorSerializer::class) val inverseOnSurface: Color,
  @Serializable(with = ColorSerializer::class) val error: Color,
  @Serializable(with = ColorSerializer::class) val onError: Color,
  @Serializable(with = ColorSerializer::class) val errorContainer: Color,
  @Serializable(with = ColorSerializer::class) val onErrorContainer: Color,
  @Serializable(with = ColorSerializer::class) val outline: Color,
  @Serializable(with = ColorSerializer::class) val outlineVariant: Color,
  @Serializable(with = ColorSerializer::class) val scrim: Color,
  @Serializable(with = ColorSerializer::class) val surfaceBright: Color,
  @Serializable(with = ColorSerializer::class) val surfaceDim: Color,
  @Serializable(with = ColorSerializer::class) val surfaceContainer: Color,
  @Serializable(with = ColorSerializer::class) val surfaceContainerHigh: Color,
  @Serializable(with = ColorSerializer::class) val surfaceContainerHighest: Color,
  @Serializable(with = ColorSerializer::class) val surfaceContainerLow: Color,
  @Serializable(with = ColorSerializer::class) val surfaceContainerLowest: Color,
)

object ColorSchemeSerializer : KSerializer<ColorScheme> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("androidx.compose.material3.ColorScheme", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: ColorScheme) =
    encoder.encodeSerializableValue(
      SerializableColorScheme.serializer(),
      SerializableColorScheme(
        primary = value.primary,
        onPrimary = value.onPrimary,
        primaryContainer = value.primaryContainer,
        onPrimaryContainer = value.onPrimaryContainer,
        inversePrimary = value.inversePrimary,
        secondary = value.secondary,
        onSecondary = value.onSecondary,
        secondaryContainer = value.secondaryContainer,
        onSecondaryContainer = value.onSecondaryContainer,
        tertiary = value.tertiary,
        onTertiary = value.onTertiary,
        tertiaryContainer = value.tertiaryContainer,
        onTertiaryContainer = value.onTertiaryContainer,
        background = value.background,
        onBackground = value.onBackground,
        surface = value.surface,
        onSurface = value.onSurface,
        surfaceVariant = value.surfaceVariant,
        onSurfaceVariant = value.onSurfaceVariant,
        surfaceTint = value.surfaceTint,
        inverseSurface = value.inverseSurface,
        inverseOnSurface = value.inverseOnSurface,
        error = value.error,
        onError = value.onError,
        errorContainer = value.errorContainer,
        onErrorContainer = value.onErrorContainer,
        outline = value.outline,
        outlineVariant = value.outlineVariant,
        scrim = value.scrim,
        surfaceBright = value.surfaceBright,
        surfaceDim = value.surfaceDim,
        surfaceContainer = value.surfaceContainer,
        surfaceContainerHigh = value.surfaceContainerHigh,
        surfaceContainerHighest = value.surfaceContainerHighest,
        surfaceContainerLow = value.surfaceContainerLow,
        surfaceContainerLowest = value.surfaceContainerLowest,
      ),
    )

  override fun deserialize(decoder: Decoder): ColorScheme {
    val serializableColorScheme =
      decoder.decodeSerializableValue(SerializableColorScheme.serializer())
    return ColorScheme(
      primary = serializableColorScheme.primary,
      onPrimary = serializableColorScheme.onPrimary,
      primaryContainer = serializableColorScheme.primaryContainer,
      onPrimaryContainer = serializableColorScheme.onPrimaryContainer,
      inversePrimary = serializableColorScheme.inversePrimary,
      secondary = serializableColorScheme.secondary,
      onSecondary = serializableColorScheme.onSecondary,
      secondaryContainer = serializableColorScheme.secondaryContainer,
      onSecondaryContainer = serializableColorScheme.onSecondaryContainer,
      tertiary = serializableColorScheme.tertiary,
      onTertiary = serializableColorScheme.onTertiary,
      tertiaryContainer = serializableColorScheme.tertiaryContainer,
      onTertiaryContainer = serializableColorScheme.onTertiaryContainer,
      background = serializableColorScheme.background,
      onBackground = serializableColorScheme.onBackground,
      surface = serializableColorScheme.surface,
      onSurface = serializableColorScheme.onSurface,
      surfaceVariant = serializableColorScheme.surfaceVariant,
      onSurfaceVariant = serializableColorScheme.onSurfaceVariant,
      surfaceTint = serializableColorScheme.surfaceTint,
      inverseSurface = serializableColorScheme.inverseSurface,
      inverseOnSurface = serializableColorScheme.inverseOnSurface,
      error = serializableColorScheme.error,
      onError = serializableColorScheme.onError,
      errorContainer = serializableColorScheme.errorContainer,
      onErrorContainer = serializableColorScheme.onErrorContainer,
      outline = serializableColorScheme.outline,
      outlineVariant = serializableColorScheme.outlineVariant,
      scrim = serializableColorScheme.scrim,
      surfaceBright = serializableColorScheme.surfaceBright,
      surfaceDim = serializableColorScheme.surfaceDim,
      surfaceContainer = serializableColorScheme.surfaceContainer,
      surfaceContainerHigh = serializableColorScheme.surfaceContainerHigh,
      surfaceContainerHighest = serializableColorScheme.surfaceContainerHighest,
      surfaceContainerLow = serializableColorScheme.surfaceContainerLow,
      surfaceContainerLowest = serializableColorScheme.surfaceContainerLowest,
    )
  }
}
