package io.github.depermitto.screens.programs

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
import io.github.depermitto.data.Day
import io.github.depermitto.data.ExerciseDao
import io.github.depermitto.set
import io.github.depermitto.components.ExpandableOutlinedCard
import io.github.depermitto.screens.exercises.ExercisesScreen
import io.github.depermitto.theme.paddingDp
import io.github.depermitto.theme.spacingDp
import io.github.depermitto.theme.transparentTextFieldColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScreen(day: Day, onDayChange: (Day?) -> Unit, exerciseDao: ExerciseDao) {
    ExpandableOutlinedCard(title = {
        TextField(
            value = day.name,
            onValueChange = { onDayChange(day.copy(name = it)) },
            textStyle = MaterialTheme.typography.titleMedium,
            colors = transparentTextFieldColors()
        )
    }, dropdownItems = {
        DropdownMenuItem(text = { Text(text = "Delete") },
            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            onClick = { onDayChange(null) })
    }, startExpanded = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingDp), verticalArrangement = Arrangement.spacedBy(spacingDp)
        ) {
            day.exercises.forEachIndexed { i, exercises ->
                SetsScreen(title = { Text(text = "${i + 1}. ${exercises.first().name}") },
                    sets = exercises,
                    onSetsChange = {
                        if (it == null) onDayChange(day.copy(exercises = day.exercises - setOf(exercises)))
                        else onDayChange(day.copy(exercises = day.exercises.set(i, it)))
                    })
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
                    onDayChange(day.copy(exercises = day.exercises + setOf(listOf(it))))
                    showBottomSheet = false
                })
            }
        }
    }
}
