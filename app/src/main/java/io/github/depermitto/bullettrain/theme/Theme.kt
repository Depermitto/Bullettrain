package io.github.depermitto.bullettrain.theme

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.depermitto.bullettrain.protos.SettingsProto.*

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun BullettrainTheme(context: Context, settings: Settings, content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = dynamicColorScheme(context, settings),
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
