package io.github.depermitto.bullettrain.programs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destination
import io.github.depermitto.bullettrain.components.AnchoredFloatingActionButton
import io.github.depermitto.bullettrain.components.ConfirmationAlertDialog
import io.github.depermitto.bullettrain.components.DropdownButton
import io.github.depermitto.bullettrain.components.EmptyScreen
import io.github.depermitto.bullettrain.components.ExtendedListItem
import io.github.depermitto.bullettrain.components.TextFieldAlertDialog
import io.github.depermitto.bullettrain.db.ProgramDao
import io.github.depermitto.bullettrain.protos.SettingsProto.*
import io.github.depermitto.bullettrain.theme.EmptyScrollSpace
import io.github.depermitto.bullettrain.theme.Medium
import io.github.depermitto.bullettrain.theme.Small
import io.github.depermitto.bullettrain.theme.focalGround
import io.github.depermitto.bullettrain.theme.secondaryGround

@Composable
fun ProgramsTab(
  modifier: Modifier = Modifier,
  programViewModel: ProgramViewModel,
  programDao: ProgramDao,
  settings: Settings,
  navController: NavController,
) {
  Box(modifier = Modifier.fillMaxHeight() then modifier) {
    val programs by programDao.getUserPrograms.collectAsStateWithLifecycle(emptyList())

    if (programs.isEmpty()) {
      EmptyScreen(
        "No programs found. After creating a program it will appear here.",
        modifier = modifier,
      )
      return
    }

    Column(
      modifier =
        Modifier.padding(start = Dp.Medium, end = Dp.Medium)
          .verticalScroll(rememberScrollState())
          .padding(bottom = Dp.EmptyScrollSpace),
      verticalArrangement = Arrangement.spacedBy(Dp.Medium),
    ) {
      programs.forEach { program ->
        var showRenameDialog by rememberSaveable { mutableStateOf(false) }
        if (showRenameDialog) {
          var errorMessage by rememberSaveable { mutableStateOf("") }
          TextFieldAlertDialog(
            startingText = program.name,
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

        var showProgramDeleteDialog by rememberSaveable { mutableStateOf(false) }
        if (showProgramDeleteDialog)
          ConfirmationAlertDialog(
            text = "Do you definitely want to delete ${program.name}?",
            onDismissRequest = { showProgramDeleteDialog = false },
            onConfirm = { programDao.delete(program) },
          )

        Card(
          onClick = { navController.navigate(Destination.Program(program.id)) },
          colors = CardDefaults.cardColors(containerColor = focalGround(settings.theme)),
        ) {
          Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(Dp.Small),
          ) {
            ExtendedListItem(
              contentPadding = PaddingValues(0.dp),
              headlineContent = { Text(program.name, style = MaterialTheme.typography.titleLarge) },
              trailingContent = {
                var showDropdown by remember { mutableStateOf(false) }
                DropdownButton(showDropdown, onShowChange = { showDropdown = it }) {
                  DropdownMenuItem(
                    text = { Text("Rename") },
                    leadingIcon = { Icon(Icons.Filled.Edit, "Rename program") },
                    onClick = {
                      showDropdown = false
                      showRenameDialog = true
                    },
                  )
                  DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = { Icon(Icons.Filled.Delete, "Delete program") },
                    onClick = {
                      showDropdown = false
                      showProgramDeleteDialog = true
                    },
                  )
                }
              },
              supportingContent = {
                Column {
                  Text("${program.workoutsCount} day program")
                  Text(
                    "${program.workoutsList.sumOf { day -> day.exercisesList.sumOf { e -> e.setsCount } }} total sets"
                  )
                }
              },
            )

            Spacer(Modifier.height(20.dp))

            Text("Next up...")
            for (i in 0..<minOf(2, program.workoutsCount)) {
              val day = program.workoutsList[(program.nextDayIndex + i) % program.workoutsCount]
              val dayIndex = program.workoutsList.indexOfFirst { d -> d.name == day.name }
              Card(
                colors = CardDefaults.cardColors(containerColor = secondaryGround(settings.theme)),
                onClick = { navController.navigate(Destination.DirectDay(program.id, dayIndex)) },
              ) {
                DayItem(day) {
                  IconButton(
                    onClick = {
                      navController.navigate(Destination.DirectDay(program.id, dayIndex))
                    }
                  ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Go to ${day.name}")
                  }
                }
              }
            }
          }
        }
      }
    }

    AnchoredFloatingActionButton(
      icon = { Icon(Icons.Filled.Add, "Create program") },
      text = { Text("Create") },
      onClick = {
        programViewModel.revertToDefault()
        navController.navigate(Destination.ProgramCreation)
      },
    )
  }
}
