package io.github.depermitto.bullettrain.theme

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import io.github.depermitto.bullettrain.protos.SettingsProto.Settings
import io.github.depermitto.bullettrain.util.isDarkMode

@Composable
fun TextFieldDefaults.unlinedColors() =
  colors(
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
  )

@Composable
fun TextStyle.Companion.numeric(
  color: Color = LocalContentColor.current,
  fontWeight: FontWeight = FontWeight.Normal,
) =
  TextStyle(color)
    .copy(
      textAlign = TextAlign.Center,
      fontStyle = MaterialTheme.typography.bodyLarge.fontStyle,
      fontWeight = fontWeight,
    )

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun dynamicColorScheme(context: Context, settings: Settings): ColorScheme {
  if (!settings.theme.isDarkMode()) {
    return dynamicLightColorScheme(context)
      .copy(
        error = Color(0xFFD93025),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFFCFCFF),
        onBackground = Color(0xFF1A1C1E),
        surface = Color(0xFFFCFCFF),
        onSurface = Color(0xFF1A1C1E),
        surfaceVariant = Color(0xFFE1E2EC),
        onSurfaceVariant = Color(0xFF44474F),
        outline = Color(0xFF74777F),
        outlineVariant = Color(0xFFC4C6D0),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFF2E3135),
        inverseOnSurface = Color(0xFFF1F0F4),
        surfaceDim = Color(0xFFDCDDE0),
        surfaceBright = Color(0xFFFCFCFF),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF0F1F5),
        surfaceContainer = Color(0xFFE7E8EC),
        surfaceContainerHighest = Color(0xFFE4E5E9),
        inversePrimary = Color(0xFFC4C6D0),
      )
  }

  return dynamicDarkColorScheme(context)
    .copy(
      error = Color(0xFFFF4444),
      onError = Color(0xFF121212),
      errorContainer = Color(0xFF770000),
      onErrorContainer = Color(0xFFFFDAD6),
      background = if (settings.trueBlack) Color.Black else Color(0xFF0C0F12),
      onBackground = Color(0xFFE1E2E8),
      surface = Color(0xFF0D1114),
      onSurface = Color(0xFFE1E2E8),
      surfaceVariant = Color(0xFF383C42),
      onSurfaceVariant = Color(0xFFC3C7CF),
      outline = Color(0xFF777B82),
      outlineVariant = Color(0xFF383C42),
      scrim = Color(0xFF000000),
      inverseSurface = Color(0xFFE1E2E8),
      inverseOnSurface = Color(0xFF2E3135),
      surfaceDim = Color(0xFF0D1114),
      surfaceBright = Color(0xFF2D3034),
      surfaceContainerLowest = Color(0xFF090B0E),
      surfaceContainerLow = Color(0xFF14161A),
      surfaceContainer = Color(0xFF181A1E),
      surfaceContainerHighest = Color(0xFF2B2D31),
      inversePrimary = Color(0xFF444444),
    )
}
