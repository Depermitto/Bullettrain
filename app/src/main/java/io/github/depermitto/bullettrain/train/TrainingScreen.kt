package io.github.depermitto.bullettrain.train

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.depermitto.bullettrain.components.DropdownButton
import io.github.depermitto.bullettrain.components.Header
import io.github.depermitto.bullettrain.components.NumberField
import io.github.depermitto.bullettrain.components.Placeholder
import io.github.depermitto.bullettrain.components.SwipeToDeleteBox
import io.github.depermitto.bullettrain.components.encodeToStringOutput
import io.github.depermitto.bullettrain.database.ExerciseDao
import io.github.depermitto.bullettrain.database.ExerciseSet
import io.github.depermitto.bullettrain.database.PerfVar
import io.github.depermitto.bullettrain.database.SettingsDao
import io.github.depermitto.bullettrain.exercises.ExerciseChooser
import io.github.depermitto.bullettrain.theme.CompactIconSize
import io.github.depermitto.bullettrain.theme.ExerciseSetNarrowWeight
import io.github.depermitto.bullettrain.theme.ExerciseSetSpacing
import io.github.depermitto.bullettrain.theme.ExerciseSetWideWeight
import io.github.depermitto.bullettrain.theme.ItemPadding
import io.github.depermitto.bullettrain.theme.ItemSpacing
import io.github.depermitto.bullettrain.theme.SqueezableIconSize
import io.github.depermitto.bullettrain.theme.filledContainerColor
import io.github.depermitto.bullettrain.theme.numberFieldTextStyle
import io.github.depermitto.bullettrain.util.SwapIcon
import java.time.Instant
import kotlin.collections.all
import kotlin.collections.plus

@Composable
fun TrainingScreen(
    trainViewModel: TrainViewModel,
    settingsDao: SettingsDao,
    exerciseDao: ExerciseDao,
) = Column(
    modifier = Modifier
        .padding(horizontal = ItemPadding)
        .verticalScroll(rememberScrollState(0)),
    verticalArrangement = Arrangement.spacedBy(ItemSpacing)
) {
    trainViewModel.getExercises().forEachIndexed { i, exercise ->
        Card(modifier = Modifier, colors = CardDefaults.cardColors(containerColor = filledContainerColor())) {
            var showExerciseChooserSwapper by rememberSaveable { mutableStateOf(false) }
            if (showExerciseChooserSwapper) ExerciseChooser(exerciseDao = exerciseDao,
                onDismissRequest = { showExerciseChooserSwapper = false },
                onChoose = { it -> trainViewModel.setExercise(i, exercise.copy(name = it.name, id = it.id)) })

            Column(modifier = Modifier.padding(ItemPadding)) {
                val lastPerformedSet = exercise.lastPerformedSet
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier
                            .padding(start = ItemSpacing)
                            .weight(1f),
                        text = "${i + 1}. ${exercise.name}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    lastPerformedSet?.let { exerciseSet ->
                        Card {
                            Text(
                                modifier = Modifier.padding(4.dp),
                                text = if (exercise.sets.all<ExerciseSet> { it.date != null }) "Done"
                                else trainViewModel.elapsedSince(exerciseSet.date!!),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    var showDropdownButton by remember { mutableStateOf(false) }
                    DropdownButton(modifier = Modifier.size(SqueezableIconSize),
                        show = showDropdownButton,
                        onShowChange = { showDropdownButton = it }) {
                        DropdownMenuItem(leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                            text = { Text(text = "Delete") },
                            onClick = { trainViewModel.removeExercise(i) })
                        DropdownMenuItem(leadingIcon = { SwapIcon() }, text = { Text(text = "Swap") }, onClick = {
                            showDropdownButton = false
                            showExerciseChooserSwapper = true
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
                            Text(modifier = Modifier.weight(ExerciseSetNarrowWeight + 0.1f),
                                text = set.targetPerfVar.encodeToStringOutput()
                                    .takeIf { it.isNotBlank() && set.targetPerfVar.category == exercise.perfVarCategory } ?: "--",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium)
                            CompletableNumberField(modifier = Modifier
                                .weight(ExerciseSetWideWeight)
                                .padding(horizontal = ExerciseSetSpacing),
                                value = set.actualPerfVar,
                                onValueChange = { trainViewModel.setExerciseSet(i, setIndex, set.copy(actualPerfVar = it)) },
                                completed = set.completed,
                                placeholder = { lastPerformedSet?.let { Placeholder(it.actualPerfVar.encodeToStringOutput()) } })
                            CompletableNumberField(
                                modifier = Modifier
                                    .weight(ExerciseSetWideWeight)
                                    .padding(horizontal = ExerciseSetSpacing),
                                value = set.weight,
                                onValueChange = { trainViewModel.setExerciseSet(i, setIndex, set.copy(weight = it)) },
                                completed = set.completed,
                                placeholder = { lastPerformedSet?.let { Placeholder(it.weight.encodeToStringOutput()) } },
                            )
                            Checkbox(modifier = Modifier
                                .size(CompactIconSize)
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

    var showExerciseChooser by rememberSaveable { mutableStateOf(false) }
    if (showExerciseChooser) ExerciseChooser(exerciseDao = exerciseDao,
        onDismissRequest = { showExerciseChooser = false },
        onChoose = { trainViewModel.addExercise(it.copy(sets = listOf(ExerciseSet(targetPerfVar = PerfVar.of(it.perfVarCategory))))) })
    OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { showExerciseChooser = true }) {
        Text(text = "Add Exercise")
    }
}


@Composable
fun CompletableNumberField(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    completed: Boolean,
    placeholder: @Composable () -> Unit,
) {
    val (textStyle, unfocusedBorderThickness, colors) = if (completed) Triple(
        numberFieldTextStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold),
        OutlinedTextFieldDefaults.FocusedBorderThickness,
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.primary,
            disabledBorderColor = MaterialTheme.colorScheme.primary,
        )
    ) else Triple(
        numberFieldTextStyle(), OutlinedTextFieldDefaults.UnfocusedBorderThickness, OutlinedTextFieldDefaults.colors()
    )

    NumberField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        enabled = !completed,
        placeholder = placeholder,
        textStyle = textStyle,
        unfocusedBorderThickness = unfocusedBorderThickness,
        colors = colors
    )
}