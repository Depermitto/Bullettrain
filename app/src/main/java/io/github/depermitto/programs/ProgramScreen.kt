package io.github.depermitto.programs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.sharp.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.depermitto.components.DropdownButton
import io.github.depermitto.components.ExpandableOutlinedCard
import io.github.depermitto.components.Header
import io.github.depermitto.components.NumberField
import io.github.depermitto.components.SwipeToDeleteBox
import io.github.depermitto.database.*
import io.github.depermitto.exercises.exerciseChooser
import io.github.depermitto.theme.*
import io.github.depermitto.util.*

@Composable
fun ProgramScreen(
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
                maxLines = 1,
                singleLine = true,
                colors = transparentTextFieldColors()
            )
        }, dropdownItems = {
            DropdownMenuItem(text = { Text(text = "Duplicate Day") },
                leadingIcon = { DuplicateIcon() },
                onClick = { programViewModel.addDay(day) })
            DropdownMenuItem(text = { Text(text = "Delete Day") },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                onClick = { programViewModel.removeDayAt(dayIndex) })
        }, startExpanded = true) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ItemPadding), verticalArrangement = Arrangement.spacedBy(ItemSpacing)
            ) {
                day.exercises.forEachIndexed { exerciseIndex, exercise ->
                    // TODO reorder exercises, reorder sets maybe?
                    ProgramExercise(
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

                val exerciseChooserToggle = exerciseChooser(exerciseDao = exerciseDao,
                    onChoose = { programViewModel.setDay(dayIndex, day.copy(exercises = day.exercises + it)) })
                OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { exerciseChooserToggle() }) {
                    Text(text = "Add Exercise")
                }
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

@Composable
fun ProgramExercise(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    onExerciseChange: (Exercise?) -> Unit,
) = Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = filledContainerColor())) {
    var showSetEditDropdown by remember { mutableStateOf(false) }
    var showTargetEditDropdown by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(ItemPadding)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
            )
            DropdownButton(show = showSetEditDropdown, onShowChange = { showSetEditDropdown = it }) {
                DropdownMenuItem(leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                    text = { Text(text = "Delete") },
                    onClick = {
                        onExerciseChange(null)
                        showSetEditDropdown = false
                    })
                DropdownMenuItem(leadingIcon = { IntensityIcon() },
                    text = { Text(text = if (!exercise.hasIntensity) "Add Intensity" else "Remove Intensity") },
                    onClick = {
                        onExerciseChange(
                            exercise.copy(intensityCategory = if (!exercise.hasIntensity) IntensityCategory.RPE else null,
                                sets = exercise.sets.map { it.copy(intensity = if (!exercise.hasIntensity) 0f else null) })
                        )
                        showSetEditDropdown = false
                    })
            }
        }

        Row(modifier = Modifier.padding(top = ItemPadding, bottom = ItemSpacing)) {
            Header(Modifier.weight(ExerciseSetNarrowWeight), "Set")
            Row(
                modifier = Modifier
                    .weight(ExerciseSetWideWeight)
                    .clickable { showTargetEditDropdown = true },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Header(text = exercise.perfVarCategory.prettyName)
                Icon(Icons.Sharp.KeyboardArrowDown, contentDescription = null)

                DropdownMenu(expanded = showTargetEditDropdown, onDismissRequest = { showTargetEditDropdown = false }) {
                    PerfVarCategory.entries.forEach { entry ->
                        DropdownMenuItem(text = { Text(entry.prettyName) }, onClick = {
                            onExerciseChange(
                                exercise.copy(
                                    sets = exercise.sets.map { it.copy(targetPerfVar = PerfVar.of(entry)) },
                                    perfVarCategory = entry
                                )
                            )
                            showTargetEditDropdown = false
                        })
                    }
                }
            }
            if (exercise.hasIntensity) {
                Header(Modifier.weight(ExerciseSetWideWeight), "RPE")
            }
            Header(Modifier.weight(ExerciseSetNarrowWeight), "")
        }
        HorizontalDivider()

        exercise.sets.forEachIndexed { setIndex, set ->
            SwipeToDeleteBox(onDelete = { onExerciseChange(exercise.copy(sets = exercise.sets.filterIndexed { i, _ -> i != setIndex })) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = filledContainerColor())
                        .padding(vertical = ItemPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(ExerciseSetNarrowWeight),
                        text = (setIndex + 1).toString(),
                        textAlign = TextAlign.Center
                    )
                    ExerciseTargetField(
                        Modifier
                            .weight(ExerciseSetWideWeight)
                            .padding(horizontal = ExerciseSetSpacing),
                        value = set.targetPerfVar,
                        onValueChange = {
                            onExerciseChange(
                                exercise.copy(sets = exercise.sets.set(setIndex, set.copy(targetPerfVar = it)))
                            )
                        })
                    if (set.intensity != null) {
                        NumberField(modifier = Modifier
                            .weight(ExerciseSetWideWeight)
                            .padding(horizontal = ExerciseSetSpacing),
                            value = set.intensity,
                            onValueChange = {
                                onExerciseChange(
                                    exercise.copy(sets = exercise.sets.set(setIndex, set.copy(intensity = it)))
                                )
                            })
                    }
                    IconButton(modifier = Modifier
                        .size(20.dp)
                        .weight(ExerciseSetNarrowWeight),
                        onClick = { onExerciseChange(exercise.copy(sets = exercise.sets + set)) }) { DuplicateIcon() }
                }
            }
        }

        OutlinedButton(modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors().copy(contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
            onClick = {
                onExerciseChange(
                    exercise.copy(
                        sets = exercise.sets + ExerciseSet(
                            intensity = exercise.intensityCategory?.let { 0f },
                            targetPerfVar = PerfVar.of(exercise.perfVarCategory)
                        )
                    )
                )
            }) { Text(text = "Add Set") }
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
