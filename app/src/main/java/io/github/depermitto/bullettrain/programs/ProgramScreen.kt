package io.github.depermitto.bullettrain.programs

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destination
import io.github.depermitto.bullettrain.components.AnchoredFloatingActionButton
import io.github.depermitto.bullettrain.components.DiscardConfirmationAlertDialog
import io.github.depermitto.bullettrain.components.DragButton
import io.github.depermitto.bullettrain.components.HoldToShowOptionsBox
import io.github.depermitto.bullettrain.components.NumberField
import io.github.depermitto.bullettrain.components.TextFieldAlertDialog
import io.github.depermitto.bullettrain.database.entities.PerfVar
import io.github.depermitto.bullettrain.theme.DuplicateIcon
import io.github.depermitto.bullettrain.theme.EmptyScrollSpace
import io.github.depermitto.bullettrain.theme.Medium
import io.github.depermitto.bullettrain.theme.Small
import sh.calvin.reorderable.ReorderableColumn

@Composable
fun ProgramScreen(
  modifier: Modifier = Modifier,
  programViewModel: ProgramViewModel,
  navController: NavController,
) =
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    val view = LocalView.current
    val days = programViewModel.getDays()
    ReorderableColumn(
      list = days,
      modifier =
        Modifier.padding(horizontal = Dp.Medium)
          .verticalScroll(rememberScrollState(0))
          .padding(bottom = Dp.EmptyScrollSpace),
      verticalArrangement = Arrangement.spacedBy(Dp.Small),
      onMove = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
          view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
        }
      },
      onSettle = { fromIndex, toIndex -> programViewModel.reorderDays(fromIndex, toIndex) },
    ) { dayIndex, day, isDragging ->
      val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)

      key(day) {
        Surface(shadowElevation = elevation, shape = MaterialTheme.shapes.medium) {
          var showRenameDialog by rememberSaveable { mutableStateOf(false) }
          var showDayDeleteDialog by rememberSaveable { mutableStateOf(false) }
          HoldToShowOptionsBox(
            onClick = { navController.navigate(Destination.Day(dayIndex)) },
            holdOptions = { closeDropdown ->
              DropdownMenuItem(
                text = { Text(text = "Rename") },
                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                onClick = {
                  closeDropdown()
                  showRenameDialog = true
                },
              )
              DropdownMenuItem(
                text = { Text(text = "Duplicate") },
                leadingIcon = { DuplicateIcon() },
                onClick = {
                  closeDropdown()
                  programViewModel.addDay(day)
                },
              )
              DropdownMenuItem(
                text = { Text(text = "Delete") },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                onClick = {
                  closeDropdown()
                  showDayDeleteDialog = true
                },
              )
            },
          ) {
            OutlinedCard {
              ListItem(
                headlineContent = { Text(text = day.name, maxLines = 1) },
                supportingContent = {
                  Text(text = "${day.entries.sumOf { it.sets.size }} sets", maxLines = 1)
                },
                trailingContent = { DragButton(this@ReorderableColumn, view) },
              )
            }

            if (showDayDeleteDialog)
              DiscardConfirmationAlertDialog(
                text = "Do you definitely want to discard ${day.name}?",
                onDismissRequest = { showDayDeleteDialog = false },
                onConfirm = { programViewModel.removeDayAt(dayIndex) },
              )

            if (showRenameDialog)
              TextFieldAlertDialog(
                label = { Text("Day Name") },
                onDismissRequest = { showRenameDialog = false },
                dismissButton = {
                  TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
                },
                confirmButton = {
                  TextButton(
                    onClick = {
                      programViewModel.setDay(dayIndex, day.copy(name = it))
                      showRenameDialog = false
                    }
                  ) {
                    Text("Confirm")
                  }
                },
              )
          }
        }
      }
    }

    if (programViewModel.getDays().size < 14)
      AnchoredFloatingActionButton(
        onClick = { programViewModel.addDay() },
        text = { Text("Add Day") },
        icon = { Icon(Icons.Filled.Add, contentDescription = "Add New Day") },
      )
  }

@Composable
fun ExerciseTargetField(
  modifier: Modifier = Modifier,
  value: PerfVar,
  onValueChange: (PerfVar) -> Unit,
  readOnly: Boolean = false,
) =
  Box(modifier = modifier) {
    when (value) {
      is PerfVar.Reps ->
        NumberField(
          modifier = Modifier.fillMaxWidth(),
          value = value.reps,
          onValueChange = { onValueChange(value.copy(it)) },
          readOnly = readOnly,
        )

      is PerfVar.Time ->
        Row(verticalAlignment = Alignment.CenterVertically) {
          NumberField(
            modifier = Modifier.weight(1f),
            value = value.time,
            onValueChange = { onValueChange(value.copy(it)) },
            readOnly = readOnly,
          )
          Text(text = "min")
        }

      is PerfVar.RepRange ->
        Row(verticalAlignment = Alignment.CenterVertically) {
          NumberField(
            modifier = Modifier.weight(0.5f),
            value = value.min,
            onValueChange = { onValueChange(value.copy(min = it)) },
            readOnly = readOnly,
          )
          Text(text = "-", modifier = Modifier.padding(horizontal = 2.dp))
          NumberField(
            modifier = Modifier.weight(0.5f),
            value = value.max,
            onValueChange = { onValueChange(value.copy(max = it)) },
            readOnly = readOnly,
          )
        }

      is PerfVar.TimeRange ->
        Row(verticalAlignment = Alignment.CenterVertically) {
          NumberField(
            modifier = Modifier.weight(0.5f),
            value = value.min,
            onValueChange = { onValueChange(value.copy(min = it)) },
            readOnly = readOnly,
          )
          Text(text = "-", modifier = Modifier.padding(horizontal = 2.dp))
          NumberField(
            modifier = Modifier.weight(0.5f),
            value = value.max,
            onValueChange = { onValueChange(value.copy(max = it)) },
            readOnly = readOnly,
          )
          Text(text = "min")
        }
    }
  }
