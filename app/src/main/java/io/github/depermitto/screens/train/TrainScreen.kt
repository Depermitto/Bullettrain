package io.github.depermitto.screens.train

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.depermitto.components.RibbonScaffold
import io.github.depermitto.data.ExerciseDao
import io.github.depermitto.presentation.SettingsViewModel
import io.github.depermitto.presentation.TrainViewModel
import io.github.depermitto.presentation.WorkoutState
import io.github.depermitto.screens.exercises.AddExerciseButton
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.ItemSpacing

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TrainScreen(
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
        modifier = Modifier.padding(horizontal = ItemPadding),
        verticalArrangement = Arrangement.spacedBy(ItemSpacing)
    ) {
        items(trainViewModel.exercises) { sets ->
            Exercise(
                trainViewModel = trainViewModel,
                settingsViewModel = settingsViewModel,
                sets = sets,
                exerciseDao = exerciseDao
            )
        }

        item {
            AddExerciseButton(exerciseDao = exerciseDao, onChoose = {
                trainViewModel.exercises += mutableStateListOf(mutableStateListOf(it))
            })
        }
    }
}
