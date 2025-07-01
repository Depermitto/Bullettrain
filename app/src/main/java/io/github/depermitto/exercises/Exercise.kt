package io.github.depermitto.exercises

import android.os.Build
import androidx.annotation.RequiresApi
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
import io.github.depermitto.data.ExerciseDao
import io.github.depermitto.data.ExerciseSet
import io.github.depermitto.data.SwapIcon
import io.github.depermitto.programs.MutableDay
import io.github.depermitto.settings.SettingsViewModel
import io.github.depermitto.theme.*
import io.github.depermitto.train.TrainViewModel
import io.github.depermitto.train.WorkoutState
import java.time.Instant

@Composable
fun Exercise(
    mutableDay: MutableDay,
    exerciseIndex: Int,
) = Card(
    colors = CardDefaults.cardColors(containerColor = filledContainerColor())
) {
    val sets = mutableDay.exercises[exerciseIndex]

    Column(modifier = Modifier.padding(ItemPadding), verticalArrangement = Arrangement.spacedBy(2 * ItemSpacing)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = "${exerciseIndex + 1}. ${sets.first().name}",
                style = MaterialTheme.typography.titleMedium,
            )
            DropdownButton {
                DropdownMenuItem(leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                    text = { Text(text = "Delete") },
                    onClick = { mutableDay.exercises.removeAt(exerciseIndex) })
            }
        }

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2 * ItemSpacing)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 2 * ItemSpacing),
                horizontalArrangement = Arrangement.Center
            ) {
                Header("Set", ExerciseSetSetWeight)
                Header("Reps", ExerciseSetNumberFieldWeight)
                Header("RPE", ExerciseSetNumberFieldWeight)
            }
            HorizontalDivider()

            sets.forEachIndexed { j, set ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(ExerciseSetSetWeight),
                        text = (j + 1).toString(),
                        textAlign = TextAlign.Center
                    )
                    NumberField(
                        Modifier
                            .weight(ExerciseSetNumberFieldWeight)
                            .padding(horizontal = ExerciseSetSpacing),
                        value = set.reps,
                        onValueChange = { sets[j] = set.copy(reps = it) }
                    )
                    NumberField(
                        Modifier
                            .weight(ExerciseSetNumberFieldWeight)
                            .padding(horizontal = ExerciseSetSpacing),
                        value = set.rpe,
                        onValueChange = { sets[j] = set.copy(rpe = it) }
                    )
                }
            }
        }

        OutlinedButton(modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors()
                .copy(contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
            onClick = { sets += ExerciseSet(exerciseId = sets.first().exerciseId, name = sets.first().name) }) {
            Text(text = "Add Set")
        }
    }
}

// TODO don't depend on TrainViewModel
@RequiresApi(Build.VERSION_CODES.O)
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
    var showDropdownButton by remember { mutableStateOf(false) }
    val exerciseChooserToggle = exerciseChooser(exerciseDao = exerciseDao, onChoose = {
        sets.forEachIndexed { i, exerciseSet -> sets[i] = exerciseSet.copy(exerciseId = it.exerciseId, name = it.name) }
    })

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
                Header("Set", ExerciseSetSetWeight)
                Header("Previous", ExerciseSetNumberFieldWeight)
                Header("Reps", ExerciseSetNumberFieldWeight)
                Header(settingsViewModel.settings.unitSystem.weightUnit(), 0.9f)
                Header("RPE", ExerciseSetNumberFieldWeight)
                if (trainViewModel.workoutState == WorkoutState.Started) {
                    Header("", ExerciseSetSetWeight)
                }
            }
            HorizontalDivider()

            sets.forEachIndexed { j, set ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(ExerciseSetSetWeight),
                        text = (j + 1).toString(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        modifier = Modifier.weight(ExerciseSetNumberFieldWeight),
                        text = "5 x 10 kg", // TODO take this data from "history"
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    NumberField(
                        Modifier
                            .weight(ExerciseSetNumberFieldWeight)
                            .padding(horizontal = ExerciseSetSpacing),
                        value = set.reps,
                        onValueChange = { sets[j] = set.copy(reps = it) },
                        readOnly = trainViewModel.workoutState == WorkoutState.NotStartedYet
                    )
                    NumberField(
                        Modifier
                            .weight(ExerciseSetNumberFieldWeight)
                            .padding(horizontal = ExerciseSetSpacing),
                        value = set.weight,
                        onValueChange = { sets[j] = set.copy(weight = it) },
                        readOnly = trainViewModel.workoutState == WorkoutState.NotStartedYet
                    )
                    NumberField(
                        Modifier
                            .weight(ExerciseSetNumberFieldWeight)
                            .padding(horizontal = ExerciseSetSpacing),
                        value = set.rpe,
                        onValueChange = { sets[j] = set.copy(rpe = it) },
                        readOnly = trainViewModel.workoutState == WorkoutState.NotStartedYet
                    )

                    if (trainViewModel.workoutState == WorkoutState.Started) {
                        Checkbox(modifier = Modifier
                            .size(20.dp)
                            .weight(ExerciseSetSetWeight),
                            checked = set.date != null,
                            onCheckedChange = {
                                sets[j] = set.copy(date = if (it) Instant.now() else null)
                            })
                    }
                }
            }
        }

        OutlinedButton(modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors()
                .copy(contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
            onClick = { sets += ExerciseSet(exerciseId = sets.first().exerciseId, name = sets.first().name) }) {
            Text(text = "Add Set")
        }
    }
}

@Composable
fun RowScope.Header(
    text: String,
    weight: Float,
) = Text(
    modifier = Modifier.weight(weight),
    text = text,
    style = MaterialTheme.typography.titleSmall,
    textAlign = TextAlign.Center
)
