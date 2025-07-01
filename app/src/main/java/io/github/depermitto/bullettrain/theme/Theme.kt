package io.github.depermitto.bullettrain.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.depermitto.bullettrain.database.entities.Settings

@Composable
fun BullettrainTheme(settings: Settings, content: @Composable () -> Unit) {
  val colorScheme =
    when {
      settings.theme.isDarkMode() && settings.trueBlack ->
        settings.palette.darkScheme.copy(background = Color.Black)
      settings.theme.isDarkMode() -> settings.palette.darkScheme
      else -> settings.palette.lightScheme
    }

  MaterialTheme(
    colorScheme = colorScheme,
    content = content,
    typography =
      Typography(
        titleLarge =
          TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp,
          )
      ),
  )
}
