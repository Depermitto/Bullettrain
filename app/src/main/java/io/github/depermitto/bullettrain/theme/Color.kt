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
import io.github.depermitto.bullettrain.database.entities.Theme

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
fun focalGround(theme: Theme) =
  if (theme.isDarkMode())
    Color(
      ColorUtils.blendARGB(
        MaterialTheme.colorScheme.background.toArgb(),
        MaterialTheme.colorScheme.surfaceBright.toArgb(),
        0.4F,
      )
    )
  else
    Color(
      ColorUtils.blendARGB(
        MaterialTheme.colorScheme.background.toArgb(),
        MaterialTheme.colorScheme.surfaceDim.toArgb(),
        0.6F,
      )
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
