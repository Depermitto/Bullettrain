package io.github.depermitto.bullettrain.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import io.github.depermitto.bullettrain.database.entities.Settings

@Composable
fun BullettrainTheme(
    settings: Settings,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        settings.theme.isDarkMode() && settings.trueBlack -> settings.palette.darkScheme.copy(background = Color.Black)
        settings.theme.isDarkMode() -> settings.palette.darkScheme
        else -> settings.palette.lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme, typography = Typography, content = content
    )
}
