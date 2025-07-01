package io.github.depermitto.bullettrain.settings

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.depermitto.bullettrain.components.Tile
import io.github.depermitto.bullettrain.components.ListAlertDialog
import io.github.depermitto.bullettrain.components.RadioTile
import io.github.depermitto.bullettrain.database.Database
import io.github.depermitto.bullettrain.database.entities.Theme
import io.github.depermitto.bullettrain.database.entities.UnitSystem
import io.github.depermitto.bullettrain.theme.palettes.*
import io.github.depermitto.bullettrain.util.splitOnUppercase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    db: Database,
    scope: CoroutineScope = rememberCoroutineScope(),
    snackbarHostState: SnackbarHostState,
) = Column(modifier) {
    val context = LocalContext.current
    val dynamicPalette = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) null else Palette(
        lightScheme = dynamicLightColorScheme(context), darkScheme = dynamicDarkColorScheme(context), name = "Dynamic"
    )
    val settings by db.settingsDao.getSettings.collectAsStateWithLifecycle()

    SettingGroup(headline = "Appearance") {
        SettingList(headline = "Palette",
            supporting = settings.palette.name,
            list = listOfNotNull(dynamicPalette, RhinoButtercupPalette, FlamePeaPalette, BonemashPalette),
            onClick = { db.settingsDao.update { state -> state.copy(palette = it) } }) { palette ->
            RadioTile(headlineContent = { Text(palette.name) }, selected = palette corresponds settings.palette)
        }

        SettingList(headline = "Theme",
            supporting = settings.theme.name.splitOnUppercase(),
            list = Theme.entries,
            onClick = { db.settingsDao.update { state -> state.copy(theme = it) } }) { theme ->
            RadioTile(headlineContent = { Text(theme.name.splitOnUppercase()) }, selected = theme == settings.theme)
        }

        SettingSwitch(
            headline = "True Black",
            supporting = "Recommended for OLED screens",
            checked = settings.trueBlack,
            onChecked = { db.settingsDao.update { state -> state.copy(trueBlack = it) } },
            enabled = settings.theme.isDarkMode()
        )
    }

    SettingGroup(headline = "Preferences") {
        SettingList(headline = "Unit System",
            supporting = settings.unitSystem.name,
            list = UnitSystem.entries,
            onClick = { db.settingsDao.update { state -> state.copy(unitSystem = it) } }) { unitSystem ->
            RadioTile(headlineContent = { Text(unitSystem.name) }, selected = unitSystem == settings.unitSystem)
        }
    }

    SettingGroup(headline = "Data Management") {
        Setting(headline = "Export", supporting = "Create a backup and store all your data in a file", onClick = {
            scope.launch(Dispatchers.IO) {
                val msg = db.exportDatabase().fold(
                    onSuccess = { filename -> "Successfully saved to $filename" },
                    onFailure = { err -> err.message ?: "Unknown error occurred while exporting to file" },
                )
                snackbarHostState.showSnackbar(msg, withDismissAction = true)
            }
        })
        Setting(headline = "Import", supporting = "Restore a previously created data backup", onClick = {
            scope.launch(Dispatchers.IO) {
                val msg = db.importDatabase(importType = Database.ImportType.Interactive).fold(
                    onSuccess = { filename -> "Successfully Imported $filename" },
                    onFailure = { err -> err.message ?: "Unknown error occurred while restoring from file" },
                )
                snackbarHostState.showSnackbar(msg, withDismissAction = true)
            }
        })
        var showConfirmFactoryResetDialog by rememberSaveable { mutableStateOf(false) }
        Setting(headline = "Factory Reset", supporting = "Clear all your data and reset to default settings", onClick = {
            showConfirmFactoryResetDialog = true
        })

        if (showConfirmFactoryResetDialog) AlertDialog(onDismissRequest = { showConfirmFactoryResetDialog = false },
            text = { Text("Do you definitely want to reset to factory? You will be prompted to save existing data nonetheless.") },
            dismissButton = { TextButton(onClick = { showConfirmFactoryResetDialog = false }) { Text("Cancel") } },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmFactoryResetDialog = false
                    scope.launch(Dispatchers.IO) {
                        if (db.exportDatabase().isFailure) snackbarHostState.showSnackbar(
                            "You are required to save your data if you want to reset to factory", withDismissAction = true
                        ) else if (db.factoryReset()) snackbarHostState.showSnackbar(
                            "Factory reset complete", withDismissAction = true
                        ) else snackbarHostState.showSnackbar(
                            "Factory reset failed", withDismissAction = true
                        )
                    }
                }) {
                    Text("Yes, Reset To Factory")
                }
            })
    }
}

@Composable
fun SettingGroup(
    modifier: Modifier = Modifier, headline: String, content: @Composable ColumnScope.() -> Unit
) = Column(modifier) {
    Text(
        text = headline,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.bodyMedium
    )
    content()
}

@Composable
fun <T> SettingList(
    modifier: Modifier = Modifier,
    headline: String,
    supporting: String,
    list: List<T>,
    onClick: (T) -> Unit,
    content: @Composable (T) -> Unit
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    Tile(
        modifier = modifier,
        headlineContent = { Text(headline) },
        supportingContent = { Text(supporting) },
        onClick = { showDialog = true },
        supportingTextStyle = MaterialTheme.typography.bodySmall
    )

    if (showDialog) ListAlertDialog(title = headline,
        list = list,
        onClick = { showDialog = false; onClick(it) },
        onDismissRequest = { showDialog = false },
        dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
        content = { content(it) })
}

@Composable
fun Setting(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    headline: String,
    supporting: String,
) = Tile(
    modifier = modifier,
    headlineContent = { Text(headline) },
    supportingContent = { Text(supporting) },
    onClick = onClick,
    supportingTextStyle = MaterialTheme.typography.bodySmall
)

@Composable
fun SettingSwitch(
    modifier: Modifier = Modifier,
    headline: String,
    supporting: String,
    onChecked: (Boolean) -> Unit,
    checked: Boolean,
    enabled: Boolean = true
) = Tile(modifier = modifier,
    headlineContent = { Text(headline) },
    supportingContent = { Text(supporting) },
    onClick = { onChecked(!checked) }.takeIf { enabled },
    trailingContent = { Switch(checked = checked, onCheckedChange = null, enabled = enabled) },
    supportingTextStyle = MaterialTheme.typography.bodySmall
)
