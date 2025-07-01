package io.github.depermitto.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.times
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.ItemSpacing

// TODO classic settings screen (better layout)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel,
) = Box(
    modifier = modifier
        .fillMaxSize()
        .padding(ItemPadding)
) {
    val context = LocalContext.current
    LaunchedEffect(settingsViewModel.toastMessage) {
        if (settingsViewModel.toastMessage.isNotEmpty()) {
            Toast.makeText(context, settingsViewModel.toastMessage, Toast.LENGTH_SHORT).show()
        }
    }

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
        Button(onClick = { settingsViewModel.importDatabase() }) { Text(text = "Import") }
        Button(onClick = { settingsViewModel.exportDatabase() }) { Text(text = "Export") }
    }

    Button(modifier = Modifier.align(Alignment.BottomCenter), onClick = { settingsViewModel.factoryReset() }) {
        Text(text = "Factory Reset")
    }
}
