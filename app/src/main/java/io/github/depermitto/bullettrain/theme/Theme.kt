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

    var colorScheme = if (settings.theme.isDarkMode()) {
        settings.palette.darkScheme
    } else {
        settings.palette.lightScheme
    }

    if (settings.trueBlack) colorScheme = colorScheme.copy(background = Color.Black)

    MaterialTheme(
        colorScheme = colorScheme, typography = Typography, content = content
    )
}
