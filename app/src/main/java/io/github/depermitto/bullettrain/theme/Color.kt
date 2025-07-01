package io.github.depermitto.bullettrain.theme

import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.graphics.ColorUtils
import io.github.depermitto.bullettrain.protos.SettingsProto.*
import io.github.depermitto.bullettrain.util.isDarkMode

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
private fun ground(theme: Theme, value: Float): Color {
  require(value in 0.0..1.0)

  return if (theme.isDarkMode())
    Color(
      ColorUtils.blendARGB(
        MaterialTheme.colorScheme.background.toArgb(),
        MaterialTheme.colorScheme.surfaceBright.toArgb(),
        value,
      )
    )
  else
    Color(
      ColorUtils.blendARGB(
        MaterialTheme.colorScheme.background.toArgb(),
        MaterialTheme.colorScheme.surfaceDim.toArgb(),
        1F - value,
      )
    )
}

@Composable
fun focalGround(theme: Theme) =
  Color(
    ColorUtils.blendARGB(
      ground(theme, 0.3F).toArgb(),
      MaterialTheme.colorScheme.tertiaryContainer.toArgb(),
      0.1F,
    )
  )

@Composable fun secondaryGround(theme: Theme) = ground(theme, 0.8F)

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
