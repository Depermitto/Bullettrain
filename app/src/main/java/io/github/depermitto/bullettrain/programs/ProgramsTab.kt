package io.github.depermitto.bullettrain.programs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destination
import io.github.depermitto.bullettrain.components.AnchoredFloatingActionButton
import io.github.depermitto.bullettrain.components.ConfirmationAlertDialog
import io.github.depermitto.bullettrain.components.ExtendedListItem
import io.github.depermitto.bullettrain.components.HoldToShowOptionsBox
import io.github.depermitto.bullettrain.components.TextFieldAlertDialog
import io.github.depermitto.bullettrain.db.ProgramDao
import io.github.depermitto.bullettrain.protos.SettingsProto.*
import io.github.depermitto.bullettrain.theme.EmptyScrollSpace
import io.github.depermitto.bullettrain.theme.Medium
import io.github.depermitto.bullettrain.theme.Small
import io.github.depermitto.bullettrain.theme.focalGround

@Composable
fun ProgramsTab(
  modifier: Modifier = Modifier,
  programViewModel: ProgramViewModel,
  programDao: ProgramDao,
  settings: Settings,
  navController: NavController,
) {
  Box(modifier = modifier.fillMaxSize()) {
    val programs by
      programDao.getUserPrograms.collectAsStateWithLifecycle(initialValue = emptyList())

    LazyColumn(
      contentPadding =
        PaddingValues(start = Dp.Medium, end = Dp.Medium, bottom = Dp.EmptyScrollSpace),
      verticalArrangement = Arrangement.spacedBy(Dp.Small),
    ) {
      items(programs) { program ->
        var showRenameDialog by rememberSaveable { mutableStateOf(false) }
        var showProgramDeleteDialog by rememberSaveable { mutableStateOf(false) }
        HoldToShowOptionsBox(
          onClick = { navController.navigate(Destination.Program(program.id)) },
          holdOptions = { closeDropdown ->
            DropdownMenuItem(
              text = { Text("Rename") },
              leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = "Rename Program") },
              onClick = {
                closeDropdown()
                showRenameDialog = true
              },
            )
            DropdownMenuItem(
              text = { Text("Delete") },
              leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = "Delete Program") },
              onClick = {
                closeDropdown()
                showProgramDeleteDialog = true
              },
            )
          },
        ) {
          Card(
            modifier = Modifier.align(Alignment.Center),
            colors = CardDefaults.cardColors(containerColor = focalGround(settings.theme)),
          ) {
            ExtendedListItem(
              headlineContent = { Text(program.name, style = MaterialTheme.typography.titleLarge) },
              supportingContent = {
                Column {
                  Text("${program.workoutsCount} day program")
                  Text(
                    "${program.workoutsList.sumOf { day -> day.exercisesList.sumOf { it.setsCount } }} total sets"
                  )
                }
              },
            )
          }

          if (showProgramDeleteDialog)
            ConfirmationAlertDialog(
              text = "Do you definitely want to delete ${program.name}?",
              onDismissRequest = { showProgramDeleteDialog = false },
              onConfirm = { programDao.delete(program) },
            )

          if (showRenameDialog) {
            var errorMessage by rememberSaveable { mutableStateOf("") }
            TextFieldAlertDialog(
              label = { Text("Program Name") },
              onDismissRequest = { showRenameDialog = false },
              dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
              },
              confirmButton = { name ->
                TextButton(
                  onClick = {
                    if (name.isBlank()) {
                      errorMessage = "Blank program name"
                      return@TextButton
                    }

                    programDao.update(program.toBuilder().setName(name).build())
                    showRenameDialog = false
                  }
                ) {
                  Text("Confirm")
                }
              },
              errorMessage = errorMessage,
              isError = errorMessage.isNotEmpty(),
            )
          }
        }
      }
    }

    AnchoredFloatingActionButton(
      icon = { Icon(Icons.Filled.Add, contentDescription = "Create Program") },
      text = { Text("Create") },
      onClick = {
        programViewModel.revertToDefault()
        navController.navigate(Destination.ProgramCreation)
      },
    )
  }
}
