package io.github.depermitto.bullettrain.settings

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.depermitto.bullettrain.components.ListAlertDialog
import io.github.depermitto.bullettrain.components.ListItem
import io.github.depermitto.bullettrain.database.Database
import io.github.depermitto.bullettrain.database.entities.Theme
import io.github.depermitto.bullettrain.database.entities.UnitSystem
import io.github.depermitto.bullettrain.theme.palettes.CreamCanFlamePeaPalette
import io.github.depermitto.bullettrain.theme.palettes.Palette
import io.github.depermitto.bullettrain.theme.palettes.RhinoButtercupPalette
import io.github.depermitto.bullettrain.util.splitOnUppercase

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    db: Database,
    snackbarHostState: SnackbarHostState,
) = Column(modifier) {
    val context = LocalContext.current
    val dynamicPalette = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) null else Palette(
        lightScheme = dynamicLightColorScheme(context), darkScheme = dynamicDarkColorScheme(context), name = "Dynamic"
    )
    val settings by db.settingsDao.getSettings.collectAsStateWithLifecycle()

    SettingList(headline = "Palette",
        supporting = settings.palette.name,
        list = listOfNotNull(dynamicPalette, RhinoButtercupPalette, CreamCanFlamePeaPalette),
        onClick = { db.settingsDao.update { state -> state.copy(palette = it) } }) { palette ->
        ListItem(headlineContent = { Text(palette.name) }, selected = palette.name == settings.palette.name)
    }

    SettingList(headline = "Unit System",
        supporting = settings.unitSystem.name,
        list = UnitSystem.entries,
        onClick = { db.settingsDao.update { state -> state.copy(unitSystem = it) } }) { unitSystem ->
        ListItem(headlineContent = { Text(unitSystem.name) }, selected = unitSystem == settings.unitSystem)
    }

    SettingList(headline = "Theme",
        supporting = settings.theme.name.splitOnUppercase(),
        list = Theme.entries,
        onClick = { db.settingsDao.update { state -> state.copy(theme = it) } }) { theme ->
        ListItem(headlineContent = { Text(theme.name.splitOnUppercase()) }, selected = theme == settings.theme)
    }

//        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
//            Button(onClick = {
//                BackgroundSlave.enqueue {
//                    val filename: String? = db.importDatabase(importType = Database.ImportType.Interactive)
//                    val msg = if (filename != null) "Successfully Imported \"$filename\"" else "Could Not Import"
//                    snackbarHostState.showSnackbar(msg, withDismissAction = true)
//                }
//            }) {
//                Text(text = "Import")
//            }
//            Button(onClick = {
//                BackgroundSlave.enqueue {
//                    val filename: String? = db.exportDatabase()
//                    val msg = if (filename != null) "Successfully Saved To \"$filename\"" else "Could Not Export"
//                    snackbarHostState.showSnackbar(msg, withDismissAction = true)
//                }
//            }) {
//                Text(text = "Export")
//            }
//        }
//        var showConfirmFactoryResetDialog by remember { mutableStateOf(false) }
//        Button(modifier = Modifier.align(Alignment.BottomCenter), onClick = { showConfirmFactoryResetDialog = true }) {
//            Text(text = "Factory Reset")
//        }
//
//        if (showConfirmFactoryResetDialog) AlertDialog(
//            onDismissRequest = { showConfirmFactoryResetDialog = false },
//            text = { Text("Do you definitely want to reset to factory? You will be prompted to save existing data nonetheless.") },
//            dismissButton = { TextButton(onClick = { showConfirmFactoryResetDialog = false }) { Text("Cancel") } },
//            confirmButton = {
//                TextButton(onClick = {
//                    showConfirmFactoryResetDialog = false
//                    BackgroundSlave.enqueue {
//                        if (db.exportDatabase() == null) snackbarHostState.showSnackbar(
//                            "You are required to save your data if you want to reset to factory", withDismissAction = true
//                        ) else if (db.factoryReset()) snackbarHostState.showSnackbar(
//                            "Factory reset complete", withDismissAction = true
//                        ) else snackbarHostState.showSnackbar(
//                            "Factory reset failed", withDismissAction = true
//                        )
//                    }
//                }) {
//                    Text("Factory Reset")
//                }
//            },
//        )
}

@Composable
fun <T> SettingList(headline: String, supporting: String, list: List<T>, onClick: (T) -> Unit, content: @Composable (T) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    ListItem(headlineContent = { Text(headline) }, supportingContent = { Text(supporting) }, onClick = { showDialog = true })

    if (showDialog) ListAlertDialog(title = "Unit System",
        list = list,
        onClick = { showDialog = false; onClick(it) },
        onDismissRequest = { showDialog = false },
        dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
        content = { content(it) })
}