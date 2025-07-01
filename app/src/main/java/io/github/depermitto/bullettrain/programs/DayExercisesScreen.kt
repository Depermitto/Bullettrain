package io.github.depermitto.bullettrain.programs

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.Sharp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.sharp.KeyboardArrowDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.depermitto.bullettrain.components.AnchoredFloatingActionButton
import io.github.depermitto.bullettrain.components.Header
import io.github.depermitto.bullettrain.components.NumberField
import io.github.depermitto.bullettrain.components.SwipeToDeleteBox
import io.github.depermitto.bullettrain.database.ExerciseDao
import io.github.depermitto.bullettrain.database.ExerciseSet
import io.github.depermitto.bullettrain.database.IntensityCategory
import io.github.depermitto.bullettrain.database.PerfVar
import io.github.depermitto.bullettrain.database.PerfVarCategory
import io.github.depermitto.bullettrain.exercises.ExerciseChooser
import io.github.depermitto.bullettrain.theme.CardSpacing
import io.github.depermitto.bullettrain.theme.CompactIconSize
import io.github.depermitto.bullettrain.theme.ExerciseSetNarrowWeight
import io.github.depermitto.bullettrain.theme.ExerciseSetSpacing
import io.github.depermitto.bullettrain.theme.ExerciseSetWideWeight
import io.github.depermitto.bullettrain.theme.ItemPadding
import io.github.depermitto.bullettrain.theme.ItemSpacing
import io.github.depermitto.bullettrain.theme.SqueezableIconSize
import io.github.depermitto.bullettrain.theme.filledContainerColor
import io.github.depermitto.bullettrain.util.DragHandleIcon
import io.github.depermitto.bullettrain.util.DuplicateIcon
import io.github.depermitto.bullettrain.util.HeartPlusIcon
import io.github.depermitto.bullettrain.util.HeartRemoveIcon
import io.github.depermitto.bullettrain.util.reorder
import io.github.depermitto.bullettrain.util.smallListSet
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableColumn
import kotlin.math.max
import kotlin.math.min

@Composable
fun DayExercisesScreen(
    modifier: Modifier = Modifier,
    programViewModel: ProgramViewModel,
    dayIndex: Int,
    exerciseDao: ExerciseDao,
    snackbarHostState: SnackbarHostState
) = Box(modifier = modifier.fillMaxSize()) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val day = programViewModel.getDay(dayIndex)
    ReorderableColumn(modifier = Modifier
        .padding(horizontal = ItemPadding)
        .fillMaxSize()
        .verticalScroll(rememberScrollState(0)),
        list = day.exercises,
        verticalArrangement = Arrangement.spacedBy(CardSpacing),
        onMove = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
            }
        },
        onSettle = { fromIndex, toIndex ->
            programViewModel.setDay(dayIndex, day.copy(exercises = day.exercises.reorder(fromIndex, toIndex)))
        }) { exerciseIndex, exercise, isDragging ->
        key(exercise.id) {
            val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

            Surface(shadowElevation = elevation, shape = MaterialTheme.shapes.medium) {
                SwipeToDeleteBox(modifier = Modifier.clip(MaterialTheme.shapes.medium), threshold = 0.9f, onDelete = {
                    val deletedDay = day
                    programViewModel.setDay(dayIndex, day.copy(exercises = day.exercises - exercise))
                    scope.launch {
                        val snackBarResult = snackbarHostState.showSnackbar(
                            message = (if (exercise.sets.size == 1) "A set" else "${exercise.sets.size} sets") + " of ${exercise.name} deleted",
                            actionLabel = "Undo",
                            duration = SnackbarDuration.Short
                        )
                        if (snackBarResult == SnackbarResult.ActionPerformed) {
                            programViewModel.setDay(dayIndex, deletedDay)
                        }
                    }
                }) {
                    Card(colors = CardDefaults.cardColors(containerColor = filledContainerColor())) {
                        Column(modifier = Modifier.padding(ItemPadding)) {
                            var showTargetEditDropdown by remember { mutableStateOf(false) }
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = ItemSpacing),
                                    text = exercise.name,
                                    style = MaterialTheme.typography.titleMedium,
                                )

                                fun setIntensity(cat: IntensityCategory?) {
                                    val intensity = if (cat != null) 0f else null
                                    programViewModel.setExercise(
                                        dayIndex,
                                        exerciseIndex,
                                        exercise.copy(
                                            intensityCategory = cat,
                                            sets = exercise.sets.map { it.copy(intensity = intensity) })
                                    )
                                }

                                if (!exercise.hasIntensity) IconButton(modifier = Modifier.size(SqueezableIconSize),
                                    onClick = { setIntensity(IntensityCategory.RPE) }) {
                                    HeartPlusIcon()
                                }
                                else IconButton(modifier = Modifier.size(SqueezableIconSize), onClick = { setIntensity(null) }) {
                                    HeartRemoveIcon()
                                }

                                IconButton(modifier = with(
                                    receiver = this@ReorderableColumn
                                ) {
                                    Modifier.draggableHandle(
                                        onDragStarted = {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                                view.performHapticFeedback(HapticFeedbackConstants.DRAG_START)
                                            }
                                        },
                                        onDragStopped = {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                                            }
                                        },
                                    )
                                }.size(SqueezableIconSize), onClick = {}) {
                                    DragHandleIcon()
                                }
                            }

                            Row(modifier = Modifier.padding(top = ItemPadding, bottom = ItemSpacing)) {
                                Header(Modifier.weight(ExerciseSetNarrowWeight), "Set")
                                Row(
                                    modifier = Modifier
                                        .weight(ExerciseSetWideWeight)
                                        .clickable { showTargetEditDropdown = true },
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Header(text = exercise.perfVarCategory.prettyName)
                                    Icon(Sharp.KeyboardArrowDown, contentDescription = null)

                                    DropdownMenu(
                                        expanded = showTargetEditDropdown,
                                        onDismissRequest = { showTargetEditDropdown = false }) {
                                        PerfVarCategory.entries.forEach { entry ->
                                            DropdownMenuItem(text = { Text(entry.prettyName) }, onClick = {
                                                programViewModel.setExercise(
                                                    dayIndex, exerciseIndex, exercise.copy(
                                                        sets = exercise.sets.map { it.copy(targetPerfVar = PerfVar.of(entry)) },
                                                        perfVarCategory = entry
                                                    )
                                                )
                                                showTargetEditDropdown = false
                                            })
                                        }
                                    }
                                }
                                if (exercise.hasIntensity) {
                                    Header(Modifier.weight(ExerciseSetWideWeight), "RPE")
                                }
                                Header(Modifier.weight(ExerciseSetNarrowWeight), "")
                            }
                            HorizontalDivider()

                            exercise.sets.forEachIndexed { setIndex, set ->
                                SwipeToDeleteBox(onDelete = {
                                    val deletedExercise = exercise
                                    programViewModel.setExercise(
                                        dayIndex,
                                        exerciseIndex,
                                        exercise.copy(sets = exercise.sets.filterIndexed { i, _ -> i != setIndex })
                                    )
                                    scope.launch {
                                        if (set.targetPerfVar != PerfVar.of(exercise.perfVarCategory)) {
                                            val snackBarResult = snackbarHostState.showSnackbar(
                                                message = "${set.targetPerfVar.encodeToStringOutput()} of ${exercise.name} deleted",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (snackBarResult == SnackbarResult.ActionPerformed) {
                                                programViewModel.setExercise(dayIndex, exerciseIndex, deletedExercise)
                                            }
                                        }
                                    }
                                }) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(color = filledContainerColor())
                                            .padding(vertical = ItemPadding), verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            modifier = Modifier.weight(ExerciseSetNarrowWeight),
                                            text = (setIndex + 1).toString(),
                                            textAlign = TextAlign.Center
                                        )
                                        ExerciseTargetField(
                                            Modifier
                                                .weight(ExerciseSetWideWeight)
                                                .padding(horizontal = ExerciseSetSpacing),
                                            value = set.targetPerfVar,
                                            onValueChange = {
                                                programViewModel.setExercise(
                                                    dayIndex, exerciseIndex, exercise.copy(
                                                        sets = exercise.sets.smallListSet(setIndex, set.copy(targetPerfVar = it))
                                                    )
                                                )
                                            })
                                        if (set.intensity != null) {
                                            NumberField(modifier = Modifier
                                                .weight(ExerciseSetWideWeight)
                                                .padding(horizontal = ExerciseSetSpacing),
                                                value = set.intensity,
                                                onValueChange = {
                                                    programViewModel.setExercise(
                                                        dayIndex, exerciseIndex, exercise.copy(
                                                            sets = exercise.sets.smallListSet(
                                                                setIndex, set.copy(intensity = max(min(it, 10f), 0f))
                                                            )
                                                        )
                                                    )
                                                })
                                        }
                                        IconButton(
                                            modifier = Modifier
                                                .size(CompactIconSize)
                                                .weight(ExerciseSetNarrowWeight),
                                            onClick = {
                                                programViewModel.setExercise(
                                                    dayIndex, exerciseIndex, exercise.copy(sets = exercise.sets + set)
                                                )
                                            }) {
                                            DuplicateIcon()
                                        }
                                    }
                                }
                            }

                            OutlinedButton(modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors()
                                    .copy(contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
                                onClick = {
                                    programViewModel.setExercise(
                                        dayIndex, exerciseIndex, exercise.copy(
                                            sets = exercise.sets + ExerciseSet(
                                                intensity = exercise.intensityCategory?.let { 0f },
                                                targetPerfVar = PerfVar.of(exercise.perfVarCategory)
                                            )
                                        )
                                    )
                                }) {
                                Text(text = "Add Set")
                            }
                        }
                    }
                }
            }
        }
    }

    var showExerciseChooser by rememberSaveable { mutableStateOf(false) }
    if (showExerciseChooser) ExerciseChooser(exerciseDao = exerciseDao,
        onDismissRequest = { showExerciseChooser = false },
        onChoose = {
            programViewModel.setDay(
                dayIndex,
                day.copy(exercises = day.exercises + it.copy(sets = listOf(ExerciseSet(targetPerfVar = PerfVar.of(it.perfVarCategory)))))
            )
        })
    AnchoredFloatingActionButton(text = { Text("Add Exercise") },
        icon = { Icon(Icons.Filled.Add, null) },
        onClick = { showExerciseChooser = true })
}