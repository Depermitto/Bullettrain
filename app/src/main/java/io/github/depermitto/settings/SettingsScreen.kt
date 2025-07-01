package io.github.depermitto.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.times
import io.github.depermitto.database.Database
import io.github.depermitto.database.UnitSystem
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.ItemSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TODO classic settings screen (better layout)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    db: Database,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(modifier = modifier.padding(ItemPadding), snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) {
        Box(modifier = modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2 * ItemSpacing)
            ) {
                Text(text = db.settingsDao.unitSystem.name)
                Switch(checked = db.settingsDao.unitSystem == UnitSystem.Metric, onCheckedChange = {
                    db.settingsDao.unitSystem = when (db.settingsDao.unitSystem) {
                        UnitSystem.Metric -> UnitSystem.Imperial
                        UnitSystem.Imperial -> UnitSystem.Metric
                    }
                })
            }

            Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = {
                    scope.launch(Dispatchers.IO) {
                        val filename: String? = db.importDatabase()
                        val msg = if (filename != null) "Successfully Imported \"$filename\"" else "Could Not Import"
                        snackbarHostState.showSnackbar(msg)
                    }
                }) { Text(text = "Import") }
                Button(onClick = {
                    scope.launch(Dispatchers.IO) {
                        val filename: String? = db.exportDatabase()
                        val msg = if (filename != null) "Successfully Saved To \"$filename\"" else "Could Not Export"
                        snackbarHostState.showSnackbar(msg)
                    }
                }) { Text(text = "Export") }
            }

//            Button(modifier = Modifier.align(Alignment.BottomCenter), onClick = {
//                db.factoryReset()
//                settingsViewModel.viewModelScope.launch { snackbarHostState.showSnackbar("Factory Reset Complete") }
//            }) { Text(text = "Factory Reset") }
        }
    }
} 