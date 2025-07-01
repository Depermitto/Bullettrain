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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.times
import androidx.lifecycle.viewModelScope
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.ItemSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TODO classic settings screen (better layout)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    Scaffold(modifier = modifier.padding(ItemPadding), snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) {
        Box(modifier = modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2 * ItemSpacing)
            ) {
                Text(text = settingsViewModel.settings.unitSystem.name)
                Switch(checked = settingsViewModel.settings.unitSystem == UnitSystem.Metric, onCheckedChange = {
                    settingsViewModel.setWeightUnit(
                        when (settingsViewModel.settings.unitSystem) {
                            UnitSystem.Metric -> UnitSystem.Imperial
                            UnitSystem.Imperial -> UnitSystem.Metric
                        }
                    )
                })
            }

            Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = {
                    settingsViewModel.viewModelScope.launch(Dispatchers.IO) {
                        val filename: String? = settingsViewModel.importDatabase(context)
                        val msg = if (filename != null) "Successfully Imported \"$filename\"" else "Could Not Import"
                        snackbarHostState.showSnackbar(msg)
                    }
                }) { Text(text = "Import") }
                Button(onClick = {
                    settingsViewModel.viewModelScope.launch(Dispatchers.IO) {
                        val filename: String? = settingsViewModel.exportDatabase()
                        val msg = if (filename != null) "Successfully Saved To \"$filename\"" else "Could Not Export"
                        snackbarHostState.showSnackbar(msg)
                    }
                }) { Text(text = "Export") }
            }

            Button(modifier = Modifier.align(Alignment.BottomCenter), onClick = {
                settingsViewModel.factoryReset()
                settingsViewModel.viewModelScope.launch { snackbarHostState.showSnackbar("Factory Reset Complete") }
            }) { Text(text = "Factory Reset") }
        }
    }
} 