package io.github.depermitto.bullettrain.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.depermitto.bullettrain.database.entities.SettingsDao

@Composable
fun BullettrainTheme(
    settingsDao: SettingsDao,
    content: @Composable () -> Unit,
) {
    val settings by settingsDao.getSettings.collectAsStateWithLifecycle()

    MaterialTheme(
        colorScheme = if (settings.theme.isDarkMode()) settings.palette.darkScheme else settings.palette.lightScheme,
        typography = Typography,
        content = content
    )
}
