package io.github.depermitto.bullettrain.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.times
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.depermitto.bullettrain.database.BackgroundSlave
import io.github.depermitto.bullettrain.database.Database
import io.github.depermitto.bullettrain.database.UnitSystem
import io.github.depermitto.bullettrain.theme.ItemPadding
import io.github.depermitto.bullettrain.theme.ItemSpacing

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    db: Database,
    snackbarHostState: SnackbarHostState,
) {
    val settings by db.settingsDao.settings.collectAsStateWithLifecycle()
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(ItemPadding)
    ) {
        Row(
            modifier = Modifier.align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2 * ItemSpacing)
        ) {
            Text(text = settings.unitSystem.name)
            Switch(checked = settings.unitSystem == UnitSystem.Metric, onCheckedChange = {
                db.settingsDao.setUnitSystem(
                    when (settings.unitSystem) {
                        UnitSystem.Metric -> UnitSystem.Imperial
                        UnitSystem.Imperial -> UnitSystem.Metric
                    }
                )
            })
        }

        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                BackgroundSlave.enqueue {
                    val filename: String? = db.importDatabase(importType = Database.ImportType.Interactive)
                    val msg = if (filename != null) "Successfully Imported \"$filename\"" else "Could Not Import"
                    snackbarHostState.showSnackbar(msg, withDismissAction = true)
                }
            }) {
                Text(text = "Import")
            }
            Button(onClick = {
                BackgroundSlave.enqueue {
                    val filename: String? = db.exportDatabase()
                    val msg = if (filename != null) "Successfully Saved To \"$filename\"" else "Could Not Export"
                    snackbarHostState.showSnackbar(msg, withDismissAction = true)
                }
            }) {
                Text(text = "Export")
            }
        }
        var showConfirmFactoryResetDialog by remember { mutableStateOf(false) }
        Button(modifier = Modifier.align(Alignment.BottomCenter), onClick = { showConfirmFactoryResetDialog = true }) {
            Text(text = "Factory Reset")
        }

        if (showConfirmFactoryResetDialog) AlertDialog(
            onDismissRequest = { showConfirmFactoryResetDialog = false },
            text = { Text("Do you definitely want to reset to factory? You will be prompted to save existing data nonetheless.") },
            dismissButton = { TextButton(onClick = { showConfirmFactoryResetDialog = false }) { Text("Cancel") } },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmFactoryResetDialog = false
                    BackgroundSlave.enqueue {
                        if (db.exportDatabase() == null) snackbarHostState.showSnackbar(
                            "You are required to save your data if you want to reset to factory", withDismissAction = true
                        ) else if (db.factoryReset()) snackbarHostState.showSnackbar(
                            "Factory reset complete", withDismissAction = true
                        ) else snackbarHostState.showSnackbar(
                            "Factory reset failed", withDismissAction = true
                        )
                    }
                }) {
                    Text("Factory Reset")
                }
            },
        )
    }
} 