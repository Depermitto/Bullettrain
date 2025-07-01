package io.github.depermitto.bullettrain.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.depermitto.bullettrain.database.entities.SettingsDao

@Composable
fun BullettrainTheme(
    settingsDao: SettingsDao,
    content: @Composable () -> Unit,
) {
    val settings by settingsDao.getSettings.collectAsStateWithLifecycle()

    val colorScheme = when {
        settings.theme.isDarkMode() && settings.trueBlack -> settings.palette.darkScheme.copy(background = Color.Black)
        settings.theme.isDarkMode() -> settings.palette.darkScheme
        else -> settings.palette.lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme, typography = Typography, content = content
    )
}
