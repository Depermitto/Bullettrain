package io.github.depermitto.screens.train

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import io.github.depermitto.presentation.SettingsViewModel
import io.github.depermitto.presentation.TrainViewModel
import io.github.depermitto.presentation.WorkoutState
import io.github.depermitto.screens.exercises.exerciseChooser
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.ItemSpacing
import io.github.depermitto.theme.filledContainerColor
import kotlin.math.min

// TODO don't depend on TrainViewModel
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Exercise(
    trainViewModel: TrainViewModel,
    settingsViewModel: SettingsViewModel,
    sets: SnapshotStateList<ExerciseSet>,
    exerciseDao: ExerciseDao,
) = Card(
    colors = CardDefaults.cardColors(containerColor = filledContainerColor())
) {
    val setsIndex = trainViewModel.exercises.indexOf(sets)
    var showDropdownButton by remember { mutableStateOf(false) }
    val exerciseChooserToggle = exerciseChooser(exerciseDao = exerciseDao, onChoose = {
        sets.forEachIndexed { i, exerciseSet -> sets[i] = exerciseSet.copy(exerciseId = it.exerciseId, name = it.name) }
    })

    Column(modifier = Modifier.padding(ItemPadding), verticalArrangement = Arrangement.spacedBy(2 * ItemSpacing)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = "${setsIndex + 1}. ${sets.first().name}",
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
                    onClick = { trainViewModel.exercises.removeAt(setsIndex) })
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
                Header("Set", 0.3f)
                Header("Previous")
                Header("Reps")
                Header("Weight")
                Header("RPE")
            }
            HorizontalDivider()

            sets.forEachIndexed { j, set ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(modifier = Modifier.weight(0.3f), text = (j + 1).toString(), textAlign = TextAlign.Center)
                    Text(
                        modifier = Modifier.weight(1f), text = "15 x 10 kg", // TODO take this data from "history"
                        textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium
                    )
                    NumberField(
                        Modifier
                            .weight(1f)
                            .padding(horizontal = ItemSpacing),
                        value = set.reps,
                        onValueChange = { sets[j] = set.copy(reps = it) })
                    NumberField(
                        Modifier
                            .weight(1f)
                            .padding(horizontal = ItemSpacing),
                        value = set.weight,
                        onValueChange = { sets[j] = set.copy(weight = it) },
                        trailingText = settingsViewModel.settings.unitSystem.weightUnit()
                    )
                    NumberField(
                        Modifier
                            .weight(1f)
                            .padding(horizontal = ItemSpacing),
                        value = set.rpe,
                        onValueChange = { sets[j] = set.copy(rpe = min(10f, it)) })
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
    weight: Float = 1f,
) = Text(
    modifier = Modifier.weight(weight),
    text = text,
    style = MaterialTheme.typography.titleSmall,
    textAlign = TextAlign.Center
)