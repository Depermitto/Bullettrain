package io.github.depermitto.programs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.depermitto.components.ExpandableOutlinedCard
import io.github.depermitto.data.ExerciseDao
import io.github.depermitto.exercises.AddExerciseButton
import io.github.depermitto.exercises.Exercise
import io.github.depermitto.misc.set
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.ItemSpacing
import io.github.depermitto.theme.transparentTextFieldColors

@Composable
fun Program(
    programViewModel: ProgramViewModel,
    exerciseDao: ExerciseDao,
) = LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(ItemPadding),
    verticalArrangement = Arrangement.spacedBy(ItemSpacing),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    itemsIndexed(programViewModel.days) { dayIndex, day ->
        ExpandableOutlinedCard(title = {
            TextField(
                value = day.name,
                onValueChange = { programViewModel.setDay(dayIndex, day.copy(name = it)) },
                textStyle = MaterialTheme.typography.titleMedium,
                colors = transparentTextFieldColors()
            )
        }, dropdownItems = {
            DropdownMenuItem(text = { Text(text = "Delete") },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                onClick = { programViewModel.removeDayAt(dayIndex) })
        }, startExpanded = true) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ItemPadding),
                verticalArrangement = Arrangement.spacedBy(ItemSpacing)
            ) {
                day.exercises.forEachIndexed { exerciseIndex, exercise ->
                    Exercise(
                        exercise = exercise,
                        onExerciseChange = {
                            programViewModel.setDay(
                                dayIndex, day.copy(
                                    exercises = if (it == null) day.exercises - exercise
                                    else day.exercises.set(exerciseIndex, it)
                                )
                            )
                        },
                    )
                }

                AddExerciseButton(exerciseDao = exerciseDao,
                    onChoose = { programViewModel.setDay(dayIndex, day.copy(exercises = day.exercises + it)) })
            }
        }

    }
    item {
        Button(
            onClick = { programViewModel.addDay() },
            enabled = programViewModel.days.size < 7,
        ) { Text("Add Day") }
    }
}
