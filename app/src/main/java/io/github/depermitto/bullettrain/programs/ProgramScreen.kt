package io.github.depermitto.bullettrain.programs

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import io.github.depermitto.bullettrain.components.ConfirmationAlertDialog
import io.github.depermitto.bullettrain.components.ExtendedListItem
import io.github.depermitto.bullettrain.components.HoldToShowOptionsBox
import io.github.depermitto.bullettrain.components.TextFieldAlertDialog
import io.github.depermitto.bullettrain.protos.SettingsProto.*
import io.github.depermitto.bullettrain.theme.DragHandleIcon
import io.github.depermitto.bullettrain.theme.DuplicateIcon
import io.github.depermitto.bullettrain.theme.EmptyScrollSpace
import io.github.depermitto.bullettrain.theme.Medium
import io.github.depermitto.bullettrain.theme.Small
import io.github.depermitto.bullettrain.theme.focalGround
import io.github.depermitto.bullettrain.util.capwords
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun ProgramScreen(
  modifier: Modifier = Modifier,
  programViewModel: ProgramViewModel,
  settings: Settings,
  navController: NavController,
) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    val days = programViewModel.getDays()
    val view = LocalView.current
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState =
      rememberReorderableLazyListState(lazyListState) { from, to ->
        programViewModel.reorderDays(from.index, to.index)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
          view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
        }
      }
    LazyColumn(
      contentPadding =
        PaddingValues(start = Dp.Medium, end = Dp.Medium, bottom = Dp.EmptyScrollSpace),
      verticalArrangement = Arrangement.spacedBy(Dp.Small),
      state = lazyListState,
    ) {
      itemsIndexed(days, key = { _, it -> it.name }) { dayIndex, day ->
        ReorderableItem(reorderableLazyListState, key = day.name) { isDragging ->
          val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
          Surface(shadowElevation = elevation, shape = MaterialTheme.shapes.medium) {
            var showRenameDialog by rememberSaveable { mutableStateOf(false) }
            if (showRenameDialog) {
              var errorMessage by rememberSaveable { mutableStateOf("") }
              TextFieldAlertDialog(
                startingText = day.name,
                label = { Text("Day Name") },
                onDismissRequest = { showRenameDialog = false },
                dismissButton = {
                  TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
                },
                confirmButton = { dayName ->
                  TextButton(
                    onClick = {
                      if (dayName.isBlank()) {
                        errorMessage = "Blank day name"
                        return@TextButton
                      }

                      if (programViewModel.getDays().any { it.name == dayName.capwords() }) {
                        errorMessage = "A day with the same name already exists"
                        return@TextButton
                      }

                      programViewModel.setDay(
                        dayIndex,
                        day.toBuilder().setName(dayName.capwords()).build(),
                      )
                      showRenameDialog = false
                    }
                  ) {
                    Text("Confirm")
                  }
                },
                errorMessage = errorMessage,
                isError = errorMessage.isNotBlank(),
              )
            }

            var showDayDeleteDialog by rememberSaveable { mutableStateOf(false) }
            if (showDayDeleteDialog)
              ConfirmationAlertDialog(
                text = "Do you definitely want to delete ${day.name}?",
                onDismissRequest = { showDayDeleteDialog = false },
                onConfirm = { programViewModel.removeDayAt(dayIndex) },
              )

            HoldToShowOptionsBox(
              onClick = { navController.navigate(Destination.Day(dayIndex)) },
              holdOptions = { closeDropdown ->
                DropdownMenuItem(
                  text = { Text("Rename") },
                  leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = "Rename Day") },
                  onClick = {
                    closeDropdown()
                    showRenameDialog = true
                  },
                )
                DropdownMenuItem(
                  text = { Text("Duplicate") },
                  leadingIcon = DuplicateIcon,
                  onClick = {
                    closeDropdown()
                    programViewModel.addDay(
                      day.toBuilder().setName(programViewModel.generateUniqueDayName()).build()
                    )
                  },
                )
                DropdownMenuItem(
                  text = { Text("Delete") },
                  leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = "Delete Day") },
                  onClick = {
                    closeDropdown()
                    showDayDeleteDialog = true
                  },
                )
              },
            ) {
              Card(colors = CardDefaults.cardColors(containerColor = focalGround(settings.theme))) {
                ExtendedListItem(
                  headlineContent = { Text(day.name, maxLines = 1) },
                  supportingContent = {
                    Text("${day.exercisesList.sumOf { it.setsCount }} sets", maxLines = 1)
                  },
                  trailingContent = {
                    IconButton(
                      modifier =
                        Modifier.draggableHandle(
                          onDragStarted = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                              view.performHapticFeedback(HapticFeedbackConstants.DRAG_START)
                            }
                          },
                          onDragStopped = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                              view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                            }
                          },
                        ),
                      onClick = {},
                      content = DragHandleIcon,
                    )
                  },
                )
              }
            }
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
}
