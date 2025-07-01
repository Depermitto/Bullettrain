package io.github.depermitto.bullettrain.theme

import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@Composable
fun TextFieldDefaults.unlinedColors() =
  colors(
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    unfocusedContainerColor = BottomAppBarDefaults.containerColor,
    focusedContainerColor = BottomAppBarDefaults.containerColor,
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

fun originalLightColorScheme() =
  lightColorScheme(
    primary = Color(0xFF35618E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF174974),
    secondary = Color(0xFF535F70),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD6E4F7),
    onSecondaryContainer = Color(0xFF3B4858),
    tertiary = Color(0xFF6A5778),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF2DAFF),
    onTertiaryContainer = Color(0xFF524060),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFF8F9FF),
    onBackground = Color(0xFF191C20),
    surface = Color(0xFFF8F9FF),
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFDFE2EB),
    onSurfaceVariant = Color(0xFF42474E),
    outline = Color(0xFF73777F),
    outlineVariant = Color(0xFFC3C7CF),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF2E3135),
    inverseOnSurface = Color(0xFFEFF0F7),
    inversePrimary = Color(0xFF9FCAFC),
    surfaceDim = Color(0xFFD8DAE0),
    surfaceBright = Color(0xFFF8F9FF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF2F3F9),
    surfaceContainer = Color(0xFFECEEF4),
    surfaceContainerHigh = Color(0xFFE6E8EE),
    surfaceContainerHighest = Color(0xFFE1E2E8),
  )

// fun blueDarkColorScheme() =
//  darkColorScheme(
//    primary = Color(0xFF9FCAFC),
//    onPrimary = Color(0xFF003257),
//    primaryContainer = Color(0xFF174974),
//    onPrimaryContainer = Color(0xFFD1E4FF),
//    secondary = Color(0xFFA9CADB), // A blend of primary blue and greener tertiary
//    onSecondary = Color(0xFF1C313F), // Darker shade for contrast
//    secondaryContainer = Color(0xFF2C4A59), // A deeper cyan-green
//    onSecondaryContainer = Color(0xFFCFE5FA), // Light cyan for contrast
//    tertiary = Color(0xFF4DD0E1), // Cyan-green
//    onTertiary = Color(0xFF00363D), // Darker shade for contrast
//    tertiaryContainer = Color(0xFF004F56), // A deeper cyan-green
//    onTertiaryContainer = Color(0xFF97F0FF), // Light cyan for contrast
//    error = Color(0xFFFFB4AB),
//    onError = Color(0xFF690005),
//    errorContainer = Color(0xFF93000A),
//    onErrorContainer = Color(0xFFFFDAD6),
//    background = Color(0xFF0C0F12),
//    onBackground = Color(0xFFE1E2E8),
//    surface = Color(0xFF0D1114),
//    onSurface = Color(0xFFE1E2E8),
//    surfaceVariant = Color(0xFF383C42),
//    onSurfaceVariant = Color(0xFFC3C7CF),
//    outline = Color(0xFF777B82),
//    outlineVariant = Color(0xFF383C42),
//    scrim = Color(0xFF000000),
//    inverseSurface = Color(0xFFE1E2E8),
//    inverseOnSurface = Color(0xFF2E3135),
//    inversePrimary = Color(0xFF35618E),
//    surfaceDim = Color(0xFF0D1114),
//    surfaceBright = Color(0xFF2D3034),
//    surfaceContainerLowest = Color(0xFF090B0E),
//    surfaceContainerLow = Color(0xFF14161A),
//    surfaceContainer = Color(0xFF181A1E),
//    surfaceContainerHighest = Color(0xFF2B2D31),
//  )

fun originalDarkColorScheme() =
  darkColorScheme(
    primary = Color(0xFFFFD700),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFFFFD700),
    onPrimaryContainer = Color(0xFF121212),
    secondary = Color(0xFFffeb7f),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFFffeb7f),
    onSecondaryContainer = Color(0xFF000000),
    tertiary = Color(-1),
    onTertiary = Color(-1),
    tertiaryContainer = Color(-1),
    onTertiaryContainer = Color(-1),
    error = Color(0xFFFF4444),
    onError = Color(0xFF121212),
    errorContainer = Color(0xFF770000),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0C0F12),
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
