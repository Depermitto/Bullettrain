package io.github.depermitto.bullettrain.theme

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.depermitto.bullettrain.protos.SettingsProto.*
import io.github.depermitto.bullettrain.util.isDarkMode

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun BullettrainTheme(context: Context, settings: Settings, content: @Composable () -> Unit) {
  val colorScheme =
    when {
      settings.theme.isDarkMode() && settings.trueBlack ->
        dynamicDarkColorScheme(context).copy(background = Color.Black)
      settings.theme.isDarkMode() -> dynamicDarkColorScheme(context)
      else -> dynamicLightColorScheme(context)
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
