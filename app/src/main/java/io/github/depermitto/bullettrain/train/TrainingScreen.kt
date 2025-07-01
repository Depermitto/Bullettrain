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
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destination
import io.github.depermitto.bullettrain.components.DiscardConfirmationAlertDialog
import io.github.depermitto.bullettrain.components.DropdownButton
import io.github.depermitto.bullettrain.components.Header
import io.github.depermitto.bullettrain.components.NumberField
import io.github.depermitto.bullettrain.components.Placeholder
import io.github.depermitto.bullettrain.components.SwipeToDeleteBox
import io.github.depermitto.bullettrain.components.TextLink
import io.github.depermitto.bullettrain.components.encodeToStringOutput
import io.github.depermitto.bullettrain.database.ExerciseDao
import io.github.depermitto.bullettrain.database.ExerciseSet
import io.github.depermitto.bullettrain.database.PerfVar
import io.github.depermitto.bullettrain.database.SettingsDao
import io.github.depermitto.bullettrain.exercises.ExerciseChooser
import io.github.depermitto.bullettrain.theme.CompactIconSize
import io.github.depermitto.bullettrain.theme.NarrowWeight
import io.github.depermitto.bullettrain.theme.RegularPadding
import io.github.depermitto.bullettrain.theme.SmallPadding
import io.github.depermitto.bullettrain.theme.SmallSpacing
import io.github.depermitto.bullettrain.theme.SqueezableIconSize
import io.github.depermitto.bullettrain.theme.SwapIcon
import io.github.depermitto.bullettrain.theme.WideSpacing
import io.github.depermitto.bullettrain.theme.WideWeight
import io.github.depermitto.bullettrain.theme.focalGround
import io.github.depermitto.bullettrain.theme.numeric
import kotlinx.coroutines.launch
import kotlin.collections.all
import kotlin.collections.plus

@Composable
fun TrainingScreen(
    trainViewModel: TrainViewModel,
    settingsDao: SettingsDao,
    exerciseDao: ExerciseDao,
    modifier: Modifier = Modifier,
    navController: NavController,
    snackbarHostState: SnackbarHostState
) = Column(
    modifier = modifier
        .padding(horizontal = RegularPadding)
        .verticalScroll(rememberScrollState(0))
        .padding(bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(WideSpacing)
) {
    val scope = rememberCoroutineScope()
    trainViewModel.getExercises().forEachIndexed { exerciseIndex, exercise ->
        var showExerciseDeleteDialog by remember { mutableStateOf(false) }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.focalGround)) {
            var showSwapExerciseChooser by rememberSaveable { mutableStateOf(false) }
            if (showSwapExerciseChooser) ExerciseChooser(exerciseDao = exerciseDao,
                onDismissRequest = { showSwapExerciseChooser = false },
                onChoose = { it -> trainViewModel.setExercise(exerciseIndex, exercise.copy(name = it.name, id = it.id)) })

            val lastPerformedSet = exercise.lastPerformedSet()
            ListItem(colors = ListItemDefaults.colors(containerColor = Color.Transparent), headlineContent = {
                TextLink(
                    "${exerciseIndex + 1}. ${exercise.name}",
                    navController = navController,
                    destination = Destination.Exercise(exercise.id),
                    style = MaterialTheme.typography.titleMedium,
                )
            }, trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!trainViewModel.isWorkoutEditing()) lastPerformedSet?.let { exerciseSet ->
                        Card {
                            Text(
                                modifier = Modifier.padding(4.dp),
                                text = if (exercise.sets.all { it.completed }) "Done"
                                else trainViewModel.elapsedSince(exerciseSet.doneTs!!),
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
                            onClick = {
                                showDropdownButton = false
                                showExerciseDeleteDialog = true
                            })
                        DropdownMenuItem(leadingIcon = { SwapIcon() }, text = { Text(text = "Swap") }, onClick = {
                            showDropdownButton = false
                            showSwapExerciseChooser = true
                        })
                    }
                }
            })

            Row(modifier = Modifier.padding(SmallPadding)) {
                Header(Modifier.weight(NarrowWeight), "Set")
                if (exercise.intensity != null) {
                    Header(Modifier.weight(NarrowWeight), exercise.intensity.name)
                }
                Header(Modifier.weight(NarrowWeight + 0.1f), "Target")
                Header(Modifier.weight(WideWeight), exercise.perfVarCategory.trainName)
                Header(Modifier.weight(WideWeight), settingsDao.weightUnit())
                if (trainViewModel.isWorkoutRunning()) {
                    Header(Modifier.weight(NarrowWeight), "")
                }
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = SmallPadding))

            exercise.sets.forEachIndexed { setIndex, set ->
                SwipeToDeleteBox(onDelete = {
                    val deletedExercise = exercise
                    trainViewModel.removeExerciseSet(exerciseIndex, setIndex)
                    if (set.actualPerfVar != 0f) scope.launch {
                        val snackBarResult = snackbarHostState.showSnackbar(
                            message = "Set ${setIndex + 1} of ${exercise.name} deleted",
                            actionLabel = "Undo",
                            withDismissAction = true,
                        )
                        if (snackBarResult == SnackbarResult.ActionPerformed) {
                            trainViewModel.setExercise(exerciseIndex, deletedExercise)
                        }
                    }
                }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.focalGround)
                            .padding(vertical = RegularPadding, horizontal = SmallPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(NarrowWeight),
                            text = (setIndex + 1).toString(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (set.actualIntensity != null) Text(
                            modifier = Modifier.weight(NarrowWeight),
                            text = set.actualIntensity.encodeToStringOutput().ifBlank { "0" },
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(modifier = Modifier.weight(NarrowWeight + 0.1f),
                            text = set.targetPerfVar.encodeToStringOutput()
                                .takeIf { it.isNotBlank() && set.targetPerfVar.category == exercise.perfVarCategory } ?: "--",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium)
                        CompletableNumberField(modifier = Modifier
                            .weight(WideWeight)
                            .padding(horizontal = SmallSpacing),
                            value = set.actualPerfVar,
                            onValueChange = {
                                trainViewModel.setExerciseSet(exerciseIndex, setIndex, set.copy(actualPerfVar = it))
                            },
                            completed = set.completed,
                            placeholder = { lastPerformedSet?.let { Placeholder(it.actualPerfVar.encodeToStringOutput()) } })
                        CompletableNumberField(
                            modifier = Modifier
                                .weight(WideWeight)
                                .padding(horizontal = SmallSpacing),
                            value = set.weight,
                            onValueChange = { trainViewModel.setExerciseSet(exerciseIndex, setIndex, set.copy(weight = it)) },
                            completed = set.completed,
                            placeholder = { lastPerformedSet?.let { Placeholder(it.weight.encodeToStringOutput()) } },
                        )
                        Checkbox(modifier = Modifier
                            .size(CompactIconSize)
                            .weight(NarrowWeight),
                            checked = set.doneTs != null,
                            onCheckedChange = { trainViewModel.toggleCompletion(it, exerciseIndex, setIndex) })
                    }
                }
            }

            OutlinedButton(modifier = Modifier
                .fillMaxWidth()
                .padding(RegularPadding),
                colors = ButtonDefaults.outlinedButtonColors().copy(contentColor = MaterialTheme.colorScheme.tertiary),
                onClick = {
                    trainViewModel.setExercise(
                        exerciseIndex, exercise.copy(
                            sets = exercise.sets + ExerciseSet(
                                actualIntensity = exercise.intensity?.let { 0f },
                                targetPerfVar = PerfVar.of(exercise.perfVarCategory),
                            )
                        )
                    )
                }) {
                Text(text = "Add Set")
            }
        }

        if (showExerciseDeleteDialog) DiscardConfirmationAlertDialog(onDismissRequest = { showExerciseDeleteDialog = false },
            text = "Do you definitely want to discard ${exercise.name}?",
            onConfirm = { trainViewModel.removeExercise(exerciseIndex) })
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
        TextStyle.numeric(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold),
        OutlinedTextFieldDefaults.FocusedBorderThickness,
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.primary,
            disabledBorderColor = MaterialTheme.colorScheme.primary,
        )
    ) else Triple(
        TextStyle.numeric(), OutlinedTextFieldDefaults.UnfocusedBorderThickness, OutlinedTextFieldDefaults.colors()
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