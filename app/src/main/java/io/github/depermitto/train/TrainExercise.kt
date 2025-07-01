package io.github.depermitto.train

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import io.github.depermitto.components.DropdownButton
import io.github.depermitto.components.NumberField
import io.github.depermitto.components.TargetNumberField
import io.github.depermitto.data.ExerciseDao
import io.github.depermitto.data.ExerciseSet
import io.github.depermitto.data.ExerciseTarget
import io.github.depermitto.exercises.Header
import io.github.depermitto.exercises.exerciseChooser
import io.github.depermitto.misc.SwapIcon
import io.github.depermitto.misc.set
import io.github.depermitto.settings.SettingsViewModel
import io.github.depermitto.theme.*
import java.time.Instant

@Composable
fun TrainExercise(
    settingsViewModel: SettingsViewModel,
    trainViewModel: TrainViewModel,
    exerciseIndex: Int,
    exerciseDao: ExerciseDao,
) = Card(colors = CardDefaults.cardColors(containerColor = filledContainerColor())) {
    val exercise = trainViewModel.exercises[exerciseIndex]
    val targetExercise = trainViewModel.targetExercises.getOrNull(exerciseIndex)

    var showDropdownButton by remember { mutableStateOf(false) }
    val swapExerciseChooser = exerciseChooser(exerciseDao = exerciseDao, onChoose = {
        trainViewModel.exercises[exerciseIndex] = exercise.copy(name = it.name, exerciseId = it.exerciseId)
    })

    Column(modifier = Modifier.padding(ItemPadding), verticalArrangement = Arrangement.spacedBy(2 * ItemSpacing)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = "${exerciseIndex + 1}. ${exercise.name}",
                style = MaterialTheme.typography.titleMedium,
            )
            if (trainViewModel.workoutPhase != WorkoutPhase.NotStartedYet) {
                exercise.sets.lastOrNull { it.date != null }?.let { exerciseSet ->
                    Card {
                        Text(
                            modifier = Modifier.padding(4.dp),
                            text = if (exercise.sets.all { it.date != null }) "Done"
                            else trainViewModel.elapsedSince(exerciseSet.date!!),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            DropdownButton(show = showDropdownButton, onShowChange = { showDropdownButton = it }) {
                DropdownMenuItem(leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                    text = { Text(text = "Delete") },
                    onClick = { trainViewModel.exercises.removeAt(exerciseIndex) })
                DropdownMenuItem(leadingIcon = { SwapIcon() }, text = { Text(text = "Swap") }, onClick = {
                    swapExerciseChooser() // TODO add alternatives here
                    showDropdownButton = false
                })
            }
        }

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2 * ItemSpacing)) {
            Row(modifier = Modifier.offset(y = 2 * ItemSpacing)) {
                Header(Modifier.weight(ExerciseSetNarrowWeight), "Set")
                if (targetExercise?.intensityCategory != null) {
                    Header(Modifier.weight(ExerciseSetNarrowWeight), targetExercise.intensityCategory.name)
                }
                if (targetExercise?.exerciseId == exercise.exerciseId) {
                    Header(Modifier.weight(ExerciseSetNarrowWeight + 0.1f), "Target")
                }
                Header(Modifier.weight(ExerciseSetWideWeight), exercise.targetCategory.prettyName)
                Header(Modifier.weight(ExerciseSetWideWeight), settingsViewModel.settings.unitSystem.weightUnit())
                if (trainViewModel.workoutPhase == WorkoutPhase.During) {
                    Header(Modifier.weight(ExerciseSetNarrowWeight), "")
                }
            }
            HorizontalDivider()

            exercise.sets.forEachIndexed { setIndex, set ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.weight(ExerciseSetNarrowWeight),
                        text = (setIndex + 1).toString(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (set.intensity != null) {
                        Text(
                            modifier = Modifier.weight(ExerciseSetNarrowWeight),
                            text = set.intensity.toString(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (targetExercise?.exerciseId == exercise.exerciseId) {
                        Text(
                            modifier = Modifier.weight(ExerciseSetNarrowWeight + 0.1f),
                            text = targetExercise.sets.getOrNull(setIndex)?.target?.toText() ?: "--",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    TargetNumberField(
                        Modifier
                            .weight(ExerciseSetWideWeight)
                            .padding(horizontal = ExerciseSetSpacing),
                        value = set.target,
                        onValueChange = {
                            trainViewModel.exercises[exerciseIndex] =
                                exercise.copy(sets = exercise.sets.set(setIndex, set.copy(target = it)))
                        },
                        readOnly = trainViewModel.workoutPhase == WorkoutPhase.NotStartedYet
                    )
                    NumberField(
                        Modifier
                            .weight(ExerciseSetWideWeight)
                            .padding(horizontal = ExerciseSetSpacing),
                        value = set.weight,
                        onValueChange = {
                            trainViewModel.exercises[exerciseIndex] =
                                exercise.copy(sets = exercise.sets.set(setIndex, set.copy(weight = it)))
                        },
                        readOnly = trainViewModel.workoutPhase == WorkoutPhase.NotStartedYet
                    )
                    if (trainViewModel.workoutPhase == WorkoutPhase.During) {
                        Checkbox(modifier = Modifier
                            .size(20.dp)
                            .weight(ExerciseSetNarrowWeight),
                            checked = set.date != null,
                            onCheckedChange = {
                                trainViewModel.exercises[exerciseIndex] = exercise.copy(
                                    sets = exercise.sets.set(setIndex, set.copy(date = if (it) Instant.now() else null))
                                )
                            })
                    }
                }
            }
        }

        OutlinedButton(modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors()
                .copy(contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
            onClick = {
                trainViewModel.exercises[exerciseIndex] = exercise.copy(
                    sets = exercise.sets + ExerciseSet(
                        intensity = exercise.intensityCategory?.let { 0f },
                        target = ExerciseTarget.of(exercise.targetCategory),
                    )
                )
            }) { Text(text = "Add Set") }
    }
}