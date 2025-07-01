package io.github.depermitto.bullettrain.programs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destinations
import io.github.depermitto.bullettrain.components.HoldToShowOptionsBox
import io.github.depermitto.bullettrain.components.NumberField
import io.github.depermitto.bullettrain.components.TextFieldAlertDialog
import io.github.depermitto.bullettrain.database.PerfVar
import io.github.depermitto.bullettrain.theme.ItemPadding
import io.github.depermitto.bullettrain.theme.ItemSpacing
import io.github.depermitto.bullettrain.util.DuplicateIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramScreen(
    programViewModel: ProgramViewModel, navController: NavController
) = LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(ItemPadding),
    verticalArrangement = Arrangement.spacedBy(ItemSpacing),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    itemsIndexed(programViewModel.getDays()) { dayIndex, day ->
        var showRenameDialog by rememberSaveable { mutableStateOf(false) }
        HoldToShowOptionsBox(onClick = { navController.navigate(Destinations.Day(dayIndex)) }, holdOptions = {
            DropdownMenuItem(text = { Text(text = "Rename") },
                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                onClick = { showRenameDialog = true })
            DropdownMenuItem(text = { Text(text = "Duplicate") },
                leadingIcon = { DuplicateIcon() },
                onClick = { programViewModel.addDay(day) })
            DropdownMenuItem(text = { Text(text = "Delete") },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                onClick = { programViewModel.removeDayAt(dayIndex) })
        }) {
            OutlinedCard {
                ListItem(headlineContent = { Text(text = day.name, maxLines = 1) },
                    supportingContent = { Text(text = "${day.exercises.sumOf { it.sets.size }} sets", maxLines = 1) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowForward, null) })
            }

            AnimatedVisibility(visible = showRenameDialog, enter = scaleIn(), exit = scaleOut()) {
                TextFieldAlertDialog(
                    label = { Text("Day Name") },
                    onDismissRequest = { showRenameDialog = false },
                    cancelButton = { TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") } },
                    confirmButton = {
                        TextButton(onClick = {
                            programViewModel.setDay(dayIndex, day.copy(name = it))
                            showRenameDialog = false
                        }) {
                            Text("Confirm")
                        }
                    },
                )
            }
        }
    }

    item {
        Button(
            onClick = { programViewModel.addDay() },
            enabled = programViewModel.getDays().size < 7,
        ) {
            Text("Add Day")
        }
    }
}

@Composable
fun ExerciseTargetField(
    modifier: Modifier = Modifier,
    value: PerfVar,
    onValueChange: (PerfVar) -> Unit,
    readOnly: Boolean = false,
) = when (value) {
    is PerfVar.Reps -> NumberField(
        modifier, value = value.reps, onValueChange = { onValueChange(value.copy(it)) }, readOnly = readOnly
    )

    is PerfVar.Time -> Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        NumberField(
            modifier = modifier, value = value.time, onValueChange = { onValueChange(value.copy(it)) }, readOnly = readOnly
        )
        Text(text = "min")
    }

    is PerfVar.RepRange -> Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        NumberField(
            modifier = modifier, value = value.min, onValueChange = { onValueChange(value.copy(min = it)) }, readOnly = readOnly
        )
        Text(text = "-")
        NumberField(
            modifier = modifier, value = value.max, onValueChange = { onValueChange(value.copy(max = it)) }, readOnly = readOnly
        )
    }
}
