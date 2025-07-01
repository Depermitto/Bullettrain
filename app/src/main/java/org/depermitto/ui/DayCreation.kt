package org.depermitto.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.depermitto.database.Day
import org.depermitto.database.ExerciseDao
import org.depermitto.ui.screens.ExercisesScreen
import org.depermitto.ui.theme.horizontalDp
import org.depermitto.ui.theme.spacingDp
import org.depermitto.ui.theme.transparentTextFieldColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayCreation(day: Day, onDayChanged: (Day?) -> Unit, exerciseDao: ExerciseDao) {
    ExpandableOutlinedCard(title = {
        TextField(
            value = day.name,
            onValueChange = { onDayChanged(day.copy(name = it)) },
            textStyle = MaterialTheme.typography.titleMedium,
            colors = transparentTextFieldColors()
        )
    }, dropdownItems = {
        DropdownMenuItem(text = { Text(text = "Delete") },
            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            onClick = { onDayChanged(null) })
    }) {
        Column(
            modifier = Modifier.padding(horizontal = horizontalDp),
            verticalArrangement = Arrangement.spacedBy(spacingDp)
        ) {
            day.exercises.forEachIndexed { i, workoutEntry ->
                // TODO Set reps, sets and supersets, maybe extract to some composable
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Text(modifier = Modifier.padding(horizontalDp), text = "${i + 1}. ${workoutEntry.exercise.name}")
                }
            }

            var showBottomSheet by rememberSaveable { mutableStateOf(false) }
            val sheetState = rememberModalBottomSheetState(true)
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { showBottomSheet = true }) {
                Text(text = "Add Exercise")
            }

            if (showBottomSheet) ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false }, sheetState = sheetState
            ) {
                ExercisesScreen(exerciseDao = exerciseDao, onSelection = {
                    onDayChanged(day.copy(exercises = day.exercises + it))
                    showBottomSheet = false
                })
            }
        }
    }
}
