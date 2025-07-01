package io.github.depermitto.train

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.depermitto.components.RibbonScaffold
import io.github.depermitto.data.ExerciseDao
import io.github.depermitto.exercises.AddExerciseButton
import io.github.depermitto.settings.SettingsViewModel
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.ItemSpacing

@Composable
fun TrainTab(
    trainViewModel: TrainViewModel, settingsViewModel: SettingsViewModel, exerciseDao: ExerciseDao,
) = RibbonScaffold(ribbon = {
    OutlinedCard(modifier = Modifier.padding(start = ItemPadding, end = ItemPadding, bottom = ItemPadding)) {
        when (trainViewModel.workoutState) {
            WorkoutState.NotStartedYet -> {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { trainViewModel.startWorkoutOnce() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) { Text(text = "Start") }
            }

            WorkoutState.Started -> {
                Row(Modifier.padding(horizontal = ItemPadding), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = trainViewModel.elapsedSince(), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = { trainViewModel.stopWorkoutOnce() },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                    ) { Text(text = "Finish") }
                }
            }

            WorkoutState.Done -> {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { trainViewModel.startWorkoutOnce() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Workout Done")
                        Text(text = "(Click To Undo)", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = ItemPadding), verticalArrangement = Arrangement.spacedBy(ItemSpacing)
    ) {
        // TODO add colors for supersets here
        itemsIndexed(trainViewModel.exercises) { i, _ ->
            TrainExercise(
                settingsViewModel = settingsViewModel,
                trainViewModel = trainViewModel,
                exerciseIndex = i,
                exerciseDao = exerciseDao
            )
        }

        item { AddExerciseButton(exerciseDao = exerciseDao, onChoose = { trainViewModel.exercises.add(it) }) }
    }
}
