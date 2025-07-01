package io.github.depermitto.train

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.depermitto.components.DropdownButton
import io.github.depermitto.components.Header
import io.github.depermitto.components.NumberField
import io.github.depermitto.components.Placeholder
import io.github.depermitto.components.SwipeToDeleteBox
import io.github.depermitto.components.encodeToStringOutput
import io.github.depermitto.database.ExerciseDao
import io.github.depermitto.database.ExerciseSet
import io.github.depermitto.database.PerfVar
import io.github.depermitto.database.SettingsDao
import io.github.depermitto.exercises.exerciseChooser
import io.github.depermitto.theme.ExerciseSetNarrowWeight
import io.github.depermitto.theme.ExerciseSetSpacing
import io.github.depermitto.theme.ExerciseSetWideWeight
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.ItemSpacing
import io.github.depermitto.theme.filledContainerColor
import io.github.depermitto.util.SwapIcon
import java.time.Instant

@Composable
fun TrainingScreen(
    trainViewModel: TrainViewModel,
    settingsDao: SettingsDao,
    exerciseDao: ExerciseDao,
) = LazyColumn(
    modifier = Modifier.padding(horizontal = ItemPadding), verticalArrangement = Arrangement.spacedBy(ItemSpacing)
) {
    // TODO add colors for supersets here
    itemsIndexed(trainViewModel.getExercises()) { i, _ ->
        Card(modifier = Modifier, colors = CardDefaults.cardColors(containerColor = filledContainerColor())) {
            val exercise = trainViewModel.getExercise(index = i)
            var showDropdownButton by remember { mutableStateOf(false) }
            val swapExerciseChooser = exerciseChooser(exerciseDao = exerciseDao, onChoose = { exercise ->
                trainViewModel.setExercise(i, exercise.copy(name = exercise.name, id = exercise.id))
            })

            Column(modifier = Modifier.padding(ItemPadding)) {
                val lastPerformedSet = exercise.lastPerformedSet
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "${i + 1}. ${exercise.name}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    lastPerformedSet?.let<ExerciseSet, Unit> { exerciseSet ->
                        Card {
                            Text(
                                modifier = Modifier.padding(4.dp),
                                text = if (exercise.sets.all<ExerciseSet> { it.date != null }) "Done"
                                else trainViewModel.elapsedSince(exerciseSet.date!!),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    DropdownButton(show = showDropdownButton, onShowChange = { showDropdownButton = it }) {
                        DropdownMenuItem(leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                            text = { Text(text = "Delete") },
                            onClick = { trainViewModel.removeExercise(i) })
                        DropdownMenuItem(leadingIcon = { SwapIcon() }, text = { Text(text = "Swap") }, onClick = {
                            swapExerciseChooser() // TODO add alternatives here
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
                    Header(Modifier.weight(ExerciseSetWideWeight), exercise.perfVarCategory.trainName)
                    Header(Modifier.weight(ExerciseSetWideWeight), settingsDao.weightUnit())
                    if (trainViewModel.isWorkoutRunning()) {
                        Header(Modifier.weight(ExerciseSetNarrowWeight), "")
                    }
                }
                HorizontalDivider()

                exercise.sets.forEachIndexed { setIndex, set ->
                    SwipeToDeleteBox(onDelete = { trainViewModel.removeExerciseSet(i, setIndex) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = filledContainerColor())
                                .padding(vertical = ItemPadding), verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier.weight(ExerciseSetNarrowWeight),
                                text = (setIndex + 1).toString(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (set.intensity != null) Text(
                                modifier = Modifier.weight(ExerciseSetNarrowWeight),
                                text = set.intensity.toString(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                modifier = Modifier.weight(ExerciseSetNarrowWeight + 0.1f),
                                text = set.targetPerfVar.encodeToStringOutput().takeUnless(String::isEmpty) ?: "--",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            NumberField(
                                Modifier
                                    .weight(ExerciseSetWideWeight)
                                    .padding(horizontal = ExerciseSetSpacing),
                                value = set.actualPerfVar,
                                onValueChange = { trainViewModel.setExerciseSet(i, setIndex, set.copy(actualPerfVar = it)) },
                                placeholder = { lastPerformedSet?.let { Placeholder(it.actualPerfVar.encodeToStringOutput()) } })
                            NumberField(
                                Modifier
                                    .weight(ExerciseSetWideWeight)
                                    .padding(horizontal = ExerciseSetSpacing),
                                value = set.weight,
                                onValueChange = { trainViewModel.setExerciseSet(i, setIndex, set.copy(weight = it)) },
                                placeholder = { lastPerformedSet?.let { Placeholder(it.weight.encodeToStringOutput()) } })
                            Checkbox(modifier = Modifier
                                .size(20.dp)
                                .weight(ExerciseSetNarrowWeight),
                                checked = set.date != null,
                                onCheckedChange = {
                                    val set = if (it) set.copy(
                                        date = Instant.now(),

                                        weight = if (set.weight != 0f) set.weight
                                        else lastPerformedSet?.weight ?: 0f,

                                        actualPerfVar = if (set.actualPerfVar != 0f) set.actualPerfVar
                                        else lastPerformedSet?.actualPerfVar ?: 0f
                                    )
                                    else set.copy(date = null)

                                    trainViewModel.setExerciseSet(i, setIndex, set)
                                })
                        }
                    }
                }

                OutlinedButton(modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors()
                        .copy(contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
                    onClick = {
                        trainViewModel.setExercise(
                            i, exercise.copy(
                                sets = exercise.sets + ExerciseSet(
                                    intensity = exercise.intensityCategory?.let { 0f },
                                    targetPerfVar = PerfVar.of(exercise.perfVarCategory),
                                )
                            )
                        )
                    }) { Text(text = "Add Set") }
            }
        }
    }

    item {
        val exerciseChooserToggle = exerciseChooser(exerciseDao = exerciseDao, onChoose = { trainViewModel.addExercise(it) })
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { exerciseChooserToggle() }) {
            Text(text = "Add Exercise")
        }
    }
}
