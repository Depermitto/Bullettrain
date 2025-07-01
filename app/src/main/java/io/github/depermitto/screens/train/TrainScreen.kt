package io.github.depermitto.screens.train

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import io.github.depermitto.components.DropdownButton
import io.github.depermitto.components.NumberField
import io.github.depermitto.components.RibbonScaffold
import io.github.depermitto.data.ExerciseDao
import io.github.depermitto.presentation.TrainViewModel
import io.github.depermitto.presentation.WorkoutState
import io.github.depermitto.screens.exercises.ExerciseChooser
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.ItemSpacing
import io.github.depermitto.theme.filledContainerColor
import java.time.Instant

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TrainScreen(trainViewModel: TrainViewModel, exerciseDao: ExerciseDao) {
    RibbonScaffold(ribbon = {
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
            itemsIndexed(trainViewModel.exercises) { setIndex, set ->
                Card(colors = CardDefaults.cardColors(containerColor = filledContainerColor())) {
                    Column(
                        modifier = Modifier.padding(ItemPadding),
                        verticalArrangement = Arrangement.spacedBy(2 * ItemSpacing)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = "${setIndex + 1}. ${set.first().name}",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            set.lastOrNull { it.date != null }?.let { exercise ->
                                Card {
                                    Text(
                                        modifier = Modifier.padding(4.dp),
                                        text = if (set.all { it.date != null }) "Done"
                                        else trainViewModel.elapsedSince(exercise.date!!),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                            DropdownButton {
                                // TODO add swap, etc..
                            }
                        }

                        set.forEachIndexed { exerciseIndex, exercise ->
                            Column {
                                Text(text = "Set ${exerciseIndex + 1}\t", style = MaterialTheme.typography.titleSmall)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(ItemSpacing)
                                ) {
                                    NumberField(
                                        modifier = Modifier.weight(0.4f), value = exercise.reps, onValueChange = {
                                            trainViewModel.exercises[setIndex][exerciseIndex] = exercise.copy(reps = it)
                                        }, label = "Reps"
                                    )
                                    NumberField(
                                        modifier = Modifier.weight(0.4f), value = exercise.rpe, onValueChange = {
                                            trainViewModel.exercises[setIndex][exerciseIndex] = exercise.copy(rpe = it)
                                        }, label = "RPE"
                                    )
                                    Checkbox(modifier = Modifier.weight(0.1f),
                                        checked = trainViewModel.exercises[setIndex][exerciseIndex].date != null,
                                        onCheckedChange = {
                                            trainViewModel.exercises[setIndex][exerciseIndex] = exercise.copy(
                                                date = if (it) Instant.now() else null
                                            )
                                        })
                                }
                            }
                        }

                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors()
                                .copy(contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
                            onClick = {
                                trainViewModel.exercises[setIndex] += set.first().copy(rpe = 0f, reps = 0f, date = null)
                            },
                        ) {
                            Text(text = "Add Set")
                        }
                    }
                }
            }


            item {
                ExerciseChooser(exerciseDao = exerciseDao, onChoose = {
                    trainViewModel.exercises += mutableStateListOf(mutableStateListOf(it))
                })
            }
        }
    }
}