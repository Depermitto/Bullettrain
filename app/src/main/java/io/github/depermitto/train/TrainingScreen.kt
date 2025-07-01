package io.github.depermitto.train

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.depermitto.components.DropdownButton
import io.github.depermitto.components.Header
import io.github.depermitto.components.NumberField
import io.github.depermitto.components.RibbonScaffold
import io.github.depermitto.components.SwipeToDeleteBox
import io.github.depermitto.data.entities.ExerciseDao
import io.github.depermitto.data.entities.ExerciseSet
import io.github.depermitto.data.entities.PerfVar
import io.github.depermitto.exercises.AddExerciseButton
import io.github.depermitto.exercises.exerciseChooser
import io.github.depermitto.misc.SwapIcon
import io.github.depermitto.misc.set
import io.github.depermitto.settings.SettingsViewModel
import io.github.depermitto.theme.*
import java.time.Instant

@Composable
fun TrainingScreen(
    trainViewModel: TrainViewModel,
    settingsViewModel: SettingsViewModel,
    exerciseDao: ExerciseDao,
) = RibbonScaffold(ribbon = {
    if (trainViewModel.isWorkoutRunning()) {
        OutlinedCard(modifier = Modifier.padding(start = ItemPadding, end = ItemPadding, bottom = ItemPadding)) {
            Row(Modifier.padding(horizontal = ItemPadding), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { trainViewModel.cancelWorkout() }) {
                    Icon(Icons.Sharp.Close, contentDescription = "Cancel Workout")
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(text = trainViewModel.elapsedSince(), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = { trainViewModel.completeWorkout() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) { Text(text = "Finish") }
            }
        }
    }
}) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = ItemPadding), verticalArrangement = Arrangement.spacedBy(ItemSpacing)
    ) {
        // TODO add colors for supersets here P2
        itemsIndexed(trainViewModel.getExercises()) { i, _ ->
            TrainExercise(
                settingsViewModel = settingsViewModel,
                trainViewModel = trainViewModel,
                exerciseIndex = i,
                exerciseDao = exerciseDao
            )
        }

        item { AddExerciseButton(exerciseDao = exerciseDao, onChoose = { trainViewModel.addExercise(it) }) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrainExercise(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel,
    trainViewModel: TrainViewModel,
    exerciseIndex: Int,
    exerciseDao: ExerciseDao,
) = Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = filledContainerColor())) {
    val exercise = trainViewModel.getExercise(exerciseIndex)
    var showDropdownButton by remember { mutableStateOf(false) }
    val swapExerciseChooser = exerciseChooser(exerciseDao = exerciseDao, onChoose = {
        trainViewModel.setExercise(exerciseIndex, exercise.copy(name = it.name, exerciseId = it.exerciseId))
    })

    Column(modifier = Modifier.padding(ItemPadding)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = "${exerciseIndex + 1}. ${exercise.name}",
                style = MaterialTheme.typography.titleMedium,
            )
            if (trainViewModel.isWorkoutRunning()) {
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
                    onClick = { trainViewModel.removeExercise(exerciseIndex) })
                DropdownMenuItem(leadingIcon = { SwapIcon() }, text = { Text(text = "Swap") }, onClick = {
                    swapExerciseChooser() // TODO add alternatives here P2
                    showDropdownButton = false
                })
            }
        }

        Row(modifier = Modifier.padding(top = ItemPadding, bottom = ItemSpacing)) {
            Header(Modifier.weight(ExerciseSetNarrowWeight), "Set")
            if (exercise.intensityCategory != null) {
                Header(Modifier.weight(ExerciseSetNarrowWeight), exercise.intensityCategory.name)
            }
            Header(Modifier.weight(ExerciseSetNarrowWeight + 0.1f), "Target")
            Header(Modifier.weight(ExerciseSetWideWeight), exercise.perfVarCategory.trainName())
            Header(Modifier.weight(ExerciseSetWideWeight), settingsViewModel.weightUnit())
            if (trainViewModel.isWorkoutRunning()) {
                Header(Modifier.weight(ExerciseSetNarrowWeight), "")
            }
        }
        HorizontalDivider()

        exercise.sets.forEachIndexed { setIndex, set ->
            SwipeToDeleteBox(onDelete = { trainViewModel.removeSet(exerciseIndex, setIndex) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = filledContainerColor())
                        .padding(ItemPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    Text(
                        modifier = Modifier.weight(ExerciseSetNarrowWeight + 0.1f),
                        text = set.targetPerfVar.toText().takeUnless(String::isEmpty) ?: "--",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    NumberField(
                        Modifier
                            .weight(ExerciseSetWideWeight)
                            .padding(horizontal = ExerciseSetSpacing),
                        value = set.actualPerfVar,
                        onValueChange = {
                            trainViewModel.setExercise(
                                exerciseIndex,
                                exercise.copy(sets = exercise.sets.set(setIndex, set.copy(actualPerfVar = it)))
                            )
                        },
                    )
                    NumberField(
                        Modifier
                            .weight(ExerciseSetWideWeight)
                            .padding(horizontal = ExerciseSetSpacing),
                        value = set.weight,
                        onValueChange = {
                            trainViewModel.setExercise(
                                exerciseIndex, exercise.copy(sets = exercise.sets.set(setIndex, set.copy(weight = it)))
                            )
                        },
                    )
                    if (trainViewModel.isWorkoutRunning()) {
                        Checkbox(modifier = Modifier
                            .size(20.dp)
                            .weight(ExerciseSetNarrowWeight),
                            checked = set.date != null,
                            onCheckedChange = {
                                trainViewModel.setExercise(
                                    exerciseIndex, exercise.copy(
                                        sets = exercise.sets.set(
                                            setIndex, set.copy(date = if (it) Instant.now() else null)
                                        )
                                    )
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
                trainViewModel.setExercise(
                    exerciseIndex, exercise.copy(
                        sets = exercise.sets + ExerciseSet(
                            intensity = exercise.intensityCategory?.let { 0f },
                            targetPerfVar = PerfVar.of(exercise.perfVarCategory),
                        )
                    )
                )
            }) { Text(text = "Add Set") }
    }
}
