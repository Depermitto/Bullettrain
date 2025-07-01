package io.github.depermitto.bullettrain.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.depermitto.bullettrain.components.ExtendedListItem
import io.github.depermitto.bullettrain.components.ListAlertDialog
import io.github.depermitto.bullettrain.components.RadioTile
import io.github.depermitto.bullettrain.db.Db
import io.github.depermitto.bullettrain.protos.SettingsProto.*
import io.github.depermitto.bullettrain.util.isDarkMode
import io.github.depermitto.bullettrain.util.splitOnUppercase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
  modifier: Modifier = Modifier,
  db: Db,
  scope: CoroutineScope = rememberCoroutineScope(),
  snackbarHostState: SnackbarHostState,
) {
  val settings by db.settingsDao.get.collectAsStateWithLifecycle()
  Column(modifier) {
    SettingGroup(headline = "Appearance") {
      SettingList(
        headline = "Theme",
        supporting = settings.theme.name.splitOnUppercase(),
        list = listOf(Theme.FollowSystem, Theme.Light, Theme.Dark),
        onClick = { db.settingsDao.update(settings.toBuilder().setTheme(it)) },
      ) { theme ->
        RadioTile(
          headlineContent = { Text(theme.name.splitOnUppercase()) },
          selected = theme == settings.theme,
        )
      }

      SettingSwitch(
        headline = "True Black",
        supporting = "Recommended for OLED screens",
        checked = settings.trueBlack,
        onChecked = { db.settingsDao.update(settings.toBuilder().setTrueBlack(it)) },
        enabled = settings.theme.isDarkMode(),
      )
    }

    SettingGroup(headline = "Preferences") {
      SettingList(
        headline = "Unit System",
        supporting = settings.unitSystem.name,
        list = listOf(UnitSystem.Metric, UnitSystem.Imperial),
        onClick = { db.settingsDao.update(settings.toBuilder().setUnitSystem(it)) },
      ) { unitSystem ->
        RadioTile(
          headlineContent = { Text(unitSystem.name) },
          selected = unitSystem == settings.unitSystem,
        )
      }
    }

    SettingGroup(headline = "Data Management") {
      Setting(
        headline = "Export",
        supporting = "Create a backup and store all your data in a file",
        onClick = {
          scope.launch(Dispatchers.IO) {
            val msg =
              db
                .exportDatabaseInteractively()
                .fold(
                  onSuccess = { filename -> "Successfully saved to $filename" },
                  onFailure = { err ->
                    err.message ?: "Unknown error occurred while exporting to file"
                  },
                )
            snackbarHostState.showSnackbar(msg, withDismissAction = true)
          }
        },
      )
      Setting(
        headline = "Import",
        supporting = "Restore a previously created data backup",
        onClick = {
          scope.launch(Dispatchers.IO) {
            val msg =
              db
                .importDatabaseInteractively()
                .fold(
                  onSuccess = { filename -> "Successfully imported $filename" },
                  onFailure = { err ->
                    err.message ?: "Unknown error occurred while restoring from file"
                  },
                )
            snackbarHostState.showSnackbar(msg, withDismissAction = true)
          }
        },
      )
      var showConfirmFactoryResetDialog by rememberSaveable { mutableStateOf(false) }
      Setting(
        headline = "Factory Reset",
        supporting = "Clear all your data and reset to default settings",
        onClick = { showConfirmFactoryResetDialog = true },
      )

      if (showConfirmFactoryResetDialog)
        AlertDialog(
          onDismissRequest = { showConfirmFactoryResetDialog = false },
          text = {
            Text(
              "Do you definitely want to reset to factory? You will be prompted to save existing data nonetheless."
            )
          },
          dismissButton = {
            TextButton(onClick = { showConfirmFactoryResetDialog = false }) { Text("Cancel") }
          },
          confirmButton = {
            TextButton(
              onClick = {
                showConfirmFactoryResetDialog = false
                scope.launch(Dispatchers.IO) {
                  if (db.exportDatabaseInteractively().isFailure)
                    snackbarHostState.showSnackbar(
                      "You are required to save your data if you want to reset to factory",
                      withDismissAction = true,
                    )
                  else if (db.factoryReset())
                    snackbarHostState.showSnackbar(
                      "Factory reset complete",
                      withDismissAction = true,
                    )
                  else
                    snackbarHostState.showSnackbar("Factory reset failed", withDismissAction = true)
                }
              }
            ) {
              Text("Yes, Reset To Factory")
            }
          },
        )
    }
  }
}

@Composable
fun SettingGroup(
  modifier: Modifier = Modifier,
  headline: String,
  content: @Composable ColumnScope.() -> Unit,
) {
  Column(modifier) {
    Text(
      headline,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
      color = MaterialTheme.colorScheme.primary,
      style = MaterialTheme.typography.bodyMedium,
    )
    content()
  }
}

@Composable
fun <T> SettingList(
  modifier: Modifier = Modifier,
  headline: String,
  supporting: String,
  list: List<T>,
  onClick: (T) -> Unit,
  content: @Composable (T) -> Unit,
) {
  var showDialog by rememberSaveable { mutableStateOf(false) }
  ExtendedListItem(
    modifier = modifier,
    headlineContent = { Text(headline) },
    supportingContent = { Text(supporting) },
    onClick = { showDialog = true },
    supportingTextStyle = MaterialTheme.typography.bodySmall,
  )

  if (showDialog)
    ListAlertDialog(
      title = headline,
      list = list,
      onClick = {
        showDialog = false
        onClick(it)
      },
      onDismissRequest = { showDialog = false },
      dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
      content = { content(it) },
    )
}

@Composable
fun Setting(
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  headline: String,
  supporting: String,
) {
  ExtendedListItem(
    modifier = modifier,
    headlineContent = { Text(headline) },
    supportingContent = { Text(supporting) },
    onClick = onClick,
    supportingTextStyle = MaterialTheme.typography.bodySmall,
  )
}

@Composable
fun SettingSwitch(
  modifier: Modifier = Modifier,
  headline: String,
  supporting: String,
  onChecked: (Boolean) -> Unit,
  checked: Boolean,
  enabled: Boolean = true,
) {
  ExtendedListItem(
    modifier = modifier,
    headlineContent = { Text(headline) },
    supportingContent = { Text(supporting) },
    onClick = { onChecked(!checked) }.takeIf { enabled },
    trailingContent = { Switch(checked = checked, onCheckedChange = null, enabled = enabled) },
    supportingTextStyle = MaterialTheme.typography.bodySmall,
  )
}
