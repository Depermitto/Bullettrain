package io.github.depermitto.exercises

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.sharp.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import io.github.depermitto.components.DropdownButton
import io.github.depermitto.components.NumberField
import io.github.depermitto.data.*
import io.github.depermitto.settings.SettingsViewModel
import io.github.depermitto.theme.*
import io.github.depermitto.train.TrainViewModel
import io.github.depermitto.train.WorkoutState
import java.time.Instant

@Composable
fun Exercise(exercise: Exercise) = Card(
    colors = CardDefaults.cardColors(containerColor = filledContainerColor())
) {
    var showSetEditDropdown by remember { mutableStateOf(false) }
    var showTargetEditDropdown by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(ItemPadding), verticalArrangement = Arrangement.spacedBy(2 * ItemSpacing)) {
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
                        mutableDay.exercises.removeAt(exerciseIndex)
                        showSetEditDropdown = false
                    })
                DropdownMenuItem(leadingIcon = { IntensityIcon() },
                    text = { Text(text = if (!hasIntensity) "Add Intensity" else "Remove Intensity") },
                    onClick = {
                        sets.replaceAll { it.copy(intensity = if (!hasIntensity) 0f else null) }
                        showSetEditDropdown = false
                    })
            }
        }

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2 * ItemSpacing)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 2 * ItemSpacing),
                horizontalArrangement = Arrangement.Center
            ) {
                Header(Modifier.weight(ExerciseSetNarrowWeight), "Set")
                Row(
                    modifier = Modifier
                        .weight(ExerciseSetWideWeight)
                        .clickable { showTargetEditDropdown = true },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Header(text = sets.first().target.name())
                    Icon(Icons.Sharp.KeyboardArrowDown, contentDescription = null)

                    DropdownMenu(
                        expanded = showTargetEditDropdown,
                        onDismissRequest = { showTargetEditDropdown = false }) {
                        if (sets.first().target !is ExerciseTarget.Reps) {
                            DropdownMenuItem(text = { Text("Reps") }, onClick = {
                                sets.replaceAll { it.copy(target = ExerciseTarget.Reps()) }
                                showTargetEditDropdown = false
                            })
                        }
                        if (sets.first().target !is ExerciseTarget.RepRange) {
                            DropdownMenuItem(text = { Text("Rep Range") }, onClick = {
                                sets.replaceAll { it.copy(target = ExerciseTarget.RepRange()) }
                                showTargetEditDropdown = false
                            })
                        }
                        if (sets.first().target !is ExerciseTarget.Time) {
                            DropdownMenuItem(text = { Text("Time") }, onClick = {
                                sets.replaceAll { it.copy(target = ExerciseTarget.Time()) }
                                showTargetEditDropdown = false
                            })
                        }
                    }
                }
                if (hasIntensity) {
                    Header(Modifier.weight(ExerciseSetWideWeight), "RPE")
                }
                Header(Modifier.weight(ExerciseSetNarrowWeight), "")
            }
            HorizontalDivider()

            sets.forEachIndexed { j, set ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(ExerciseSetNarrowWeight),
                        text = (j + 1).toString(),
                        textAlign = TextAlign.Center
                    )
                    TargetNumberField(
                        Modifier
                            .weight(ExerciseSetWideWeight)
                            .padding(horizontal = ExerciseSetSpacing),
                        value = set.target,
                        onValueChange = { sets[j] = set.copy(target = it) })
                    if (set.intensity != null) {
                        NumberField(
                            Modifier
                                .weight(ExerciseSetWideWeight)
                                .padding(horizontal = ExerciseSetSpacing),
                            value = set.intensity,
                            onValueChange = { sets[j] = set.copy(intensity = it) })
                    }
                    IconButton(
                        modifier = Modifier
                            .size(20.dp)
                            .weight(ExerciseSetNarrowWeight),
                        onClick = { sets += set }) { DuplicateIcon() }
                }
            }
        }

        OutlinedButton(modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors()
                .copy(contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
            onClick = {
                sets += ExerciseSet(
                    exerciseId = sets.first().exerciseId,
                    name = sets.first().name,
                    intensity = sets.first().intensity?.let { 0f },
                    target = sets.first().target.zero()
                )
            }) {
            Text(text = "Add Set")
        }
    }
}

// TODO don't depend on TrainViewModel
@Composable
fun TrainExercise(
    settingsViewModel: SettingsViewModel,
    trainViewModel: TrainViewModel,
    exerciseIndex: Int,
    exerciseDao: ExerciseDao,
) = Card(
    colors = CardDefaults.cardColors(containerColor = filledContainerColor())
) {
    val sets = trainViewModel.trainDay.exercises[exerciseIndex]
    val targetSets = trainViewModel.targetDay.exercises.getOrNull(exerciseIndex)
    val hasIntensity = targetSets?.any { it.intensity != null } ?: false

    val exerciseChooserToggle = exerciseChooser(exerciseDao = exerciseDao, onChoose = {
        sets.forEachIndexed { i, exerciseSet -> sets[i] = exerciseSet.copy(exerciseId = it.exerciseId, name = it.name) }
    })
    var showDropdownButton by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(ItemPadding), verticalArrangement = Arrangement.spacedBy(2 * ItemSpacing)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = "${exerciseIndex + 1}. ${sets.first().name}",
                style = MaterialTheme.typography.titleMedium,
            )
            if (trainViewModel.workoutState != WorkoutState.NotStartedYet) {
                sets.lastOrNull { it.date != null }?.let { exercise ->
                    Card {
                        Text(
                            modifier = Modifier.padding(4.dp),
                            text = if (sets.all { it.date != null }) "Done"
                            else trainViewModel.elapsedSince(exercise.date!!),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            DropdownButton(show = showDropdownButton, onShowChange = { showDropdownButton = it }) {
                DropdownMenuItem(leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                    text = { Text(text = "Delete") },
                    onClick = { trainViewModel.trainDay.exercises.removeAt(exerciseIndex) })
                DropdownMenuItem(leadingIcon = { SwapIcon() }, text = { Text(text = "Swap") }, onClick = {
                    exerciseChooserToggle() // TODO add alternatives here
                    showDropdownButton = false
                })
            }
        }

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2 * ItemSpacing)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 2 * ItemSpacing),
                horizontalArrangement = Arrangement.Center
            ) {
                Header(Modifier.weight(ExerciseSetNarrowWeight), "Set")
                if (hasIntensity) {
                    Header(Modifier.weight(ExerciseSetNarrowWeight), "RPE")
                }
                if (targetSets?.firstOrNull()?.exerciseId == sets.first().exerciseId) {
                    Header(Modifier.weight(ExerciseSetNarrowWeight + 0.1f), "Target")
                }
                Header(Modifier.weight(ExerciseSetWideWeight), "Reps")
                Header(Modifier.weight(ExerciseSetWideWeight), settingsViewModel.settings.unitSystem.weightUnit())
                if (trainViewModel.workoutState == WorkoutState.Started) {
                    Header(Modifier.weight(ExerciseSetNarrowWeight), "")
                }
            }
            HorizontalDivider()

            sets.forEachIndexed { setIndex, set ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(ExerciseSetNarrowWeight),
                        text = (setIndex + 1).toString(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (hasIntensity) {
                        Text(
                            modifier = Modifier.weight(ExerciseSetNarrowWeight),
                            text = set.intensity.toString(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    targetSets?.firstOrNull()?.let { targetSet ->
                        if (targetSet.exerciseId == sets.first().exerciseId) {
                            Text(
                                modifier = Modifier.weight(ExerciseSetNarrowWeight + 0.1f),
                                text = targetSets.getOrNull(setIndex)?.target?.toString() ?: "--",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    TargetNumberField(
                        Modifier
                            .weight(ExerciseSetWideWeight)
                            .padding(horizontal = ExerciseSetSpacing),
                        value = set.target,
                        onValueChange = { sets[setIndex] = set.copy(target = it) },
                        readOnly = trainViewModel.workoutState == WorkoutState.NotStartedYet
                    )
                    NumberField(
                        Modifier
                            .weight(ExerciseSetWideWeight)
                            .padding(horizontal = ExerciseSetSpacing),
                        value = set.weight,
                        onValueChange = { sets[setIndex] = set.copy(weight = it) },
                        readOnly = trainViewModel.workoutState == WorkoutState.NotStartedYet
                    )
                    if (trainViewModel.workoutState == WorkoutState.Started) {
                        Checkbox(modifier = Modifier
                            .size(20.dp)
                            .weight(ExerciseSetNarrowWeight),
                            checked = set.date != null,
                            onCheckedChange = { sets[setIndex] = set.copy(date = if (it) Instant.now() else null) })
                    }
                }
            }
        }

        OutlinedButton(modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors()
                .copy(contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
            onClick = {
                sets += ExerciseSet(
                    exerciseId = sets.first().exerciseId, name = sets.first().name, target = sets.first().target.zero()
                )
            }) {
            Text(text = "Add Set")
        }
    }
}

@Composable
fun Header(
    modifier: Modifier = Modifier,
    text: String,
) = Text(modifier = modifier, text = text, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)

@Composable
fun TargetNumberField(
    modifier: Modifier = Modifier,
    value: ExerciseTarget,
    onValueChange: (ExerciseTarget) -> Unit,
    readOnly: Boolean = false,
) = when (value) {
    is ExerciseTarget.Reps -> NumberField(
        modifier,
        value = value.reps.toFloat(),
        onValueChange = { onValueChange(value.copy(it.toInt())) },
        readOnly = readOnly
    )

    is ExerciseTarget.Time -> Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        NumberField(
            modifier = modifier,
            value = value.time.toFloat(),
            onValueChange = { onValueChange(value.copy(it.toLong())) },
            readOnly = readOnly
        )
        Text(text = "min")
    }

    is ExerciseTarget.RepRange -> Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        NumberField(
            modifier = modifier,
            value = value.min.toFloat(),
            onValueChange = { onValueChange(value.copy(min = it.toInt())) },
            readOnly = readOnly
        )
        Text(text = "-")
        NumberField(
            modifier = modifier,
            value = value.max.toFloat(),
            onValueChange = { onValueChange(value.copy(max = it.toInt())) },
            readOnly = readOnly
        )
    }
}