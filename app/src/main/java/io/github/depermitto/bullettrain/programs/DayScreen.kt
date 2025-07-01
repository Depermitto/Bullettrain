package io.github.depermitto.bullettrain.programs

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destination
import io.github.depermitto.bullettrain.components.AnchoredFloatingActionButton
import io.github.depermitto.bullettrain.components.DragButton
import io.github.depermitto.bullettrain.components.Header
import io.github.depermitto.bullettrain.components.ListItem
import io.github.depermitto.bullettrain.components.NumberField
import io.github.depermitto.bullettrain.components.SwipeToDeleteBox
import io.github.depermitto.bullettrain.components.TextLink
import io.github.depermitto.bullettrain.database.ExerciseDao
import io.github.depermitto.bullettrain.database.ExerciseSet
import io.github.depermitto.bullettrain.database.HistoryDao
import io.github.depermitto.bullettrain.database.Intensity
import io.github.depermitto.bullettrain.database.PerfVar
import io.github.depermitto.bullettrain.database.PerfVarCategory
import io.github.depermitto.bullettrain.database.WorkoutEntry
import io.github.depermitto.bullettrain.exercises.ExerciseChooser
import io.github.depermitto.bullettrain.theme.BigSpacing
import io.github.depermitto.bullettrain.theme.CompactIconSize
import io.github.depermitto.bullettrain.theme.DuplicateIcon
import io.github.depermitto.bullettrain.theme.HeartPlusIcon
import io.github.depermitto.bullettrain.theme.HeartRemoveIcon
import io.github.depermitto.bullettrain.theme.NarrowWeight
import io.github.depermitto.bullettrain.theme.RegularPadding
import io.github.depermitto.bullettrain.theme.ScrollPadding
import io.github.depermitto.bullettrain.theme.SmallPadding
import io.github.depermitto.bullettrain.theme.SmallSpacing
import io.github.depermitto.bullettrain.theme.SqueezableIconSize
import io.github.depermitto.bullettrain.theme.SwapIcon
import io.github.depermitto.bullettrain.theme.WideWeight
import io.github.depermitto.bullettrain.theme.focalGround
import io.github.depermitto.bullettrain.util.reorder
import io.github.depermitto.bullettrain.util.smallListSet
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableColumn
import kotlin.math.max
import kotlin.math.min

@Composable
fun DayScreen(
    modifier: Modifier = Modifier,
    programViewModel: ProgramViewModel,
    dayIndex: Int,
    exerciseDao: ExerciseDao,
    historyDao: HistoryDao,
    navController: NavController,
    snackbarHostState: SnackbarHostState
) = Box(modifier = modifier.fillMaxSize()) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val day = programViewModel.getDay(dayIndex)
    ReorderableColumn(modifier = Modifier
        .padding(horizontal = RegularPadding)
        .fillMaxSize()
        .verticalScroll(rememberScrollState(0))
        .padding(bottom = ScrollPadding),
        list = day.entries,
        verticalArrangement = Arrangement.spacedBy(BigSpacing),
        onMove = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
            }
        },
        onSettle = { fromIndex, toIndex ->
            programViewModel.setDay(dayIndex, day.copy(entries = day.entries.reorder(fromIndex, toIndex)))
        }) { exerciseIndex, exercise, isDragging ->
        fun setIntensity(cat: Intensity?) {
            val intensity = if (cat != null) 0f else null
            programViewModel.setExercise(
                dayIndex,
                exerciseIndex,
                exercise.copy(intensity = cat, sets = exercise.sets.map { it.copy(actualIntensity = intensity) })
            )
        }

        key(exercise.descriptorId) {
            val exerciseDescriptor = exerciseDao.where(exercise.descriptorId)
            val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
            var showSwapExerciseChooser by rememberSaveable { mutableStateOf(false) }

            Surface(shadowElevation = elevation, shape = MaterialTheme.shapes.medium) {
                SwipeToDeleteBox(modifier = Modifier.clip(MaterialTheme.shapes.medium), threshold = 0.9f, onDelete = {
                    val deletedDay = day
                    programViewModel.setDay(dayIndex, day.copy(entries = day.entries - exercise))
                    scope.launch {
                        val snackBarResult = snackbarHostState.showSnackbar(
                            message = (if (exercise.sets.size == 1) "A set" else "${exercise.sets.size} sets") + " of ${exerciseDescriptor.name} deleted",
                            actionLabel = "Undo",
                            withDismissAction = true
                        )
                        if (snackBarResult == SnackbarResult.ActionPerformed) {
                            programViewModel.setDay(dayIndex, deletedDay)
                        }
                    }
                }) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.focalGround)) {
                        var showTargetEditDropdown by remember { mutableStateOf(false) }
                        ListItem(headlineContent = {
                            TextLink(
                                exerciseDescriptor.name,
                                navController = navController,
                                destination = Destination.Exercise(exerciseDescriptor.id),
                                contentPadding = PaddingValues(RegularPadding),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }, trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(modifier = Modifier.size(SqueezableIconSize),
                                    onClick = { showSwapExerciseChooser = true }) {
                                    SwapIcon()
                                }

                                if (!exercise.hasIntensity) IconButton(modifier = Modifier.size(SqueezableIconSize),
                                    onClick = { setIntensity(Intensity.RPE) }) {
                                    HeartPlusIcon()
                                }
                                else IconButton(modifier = Modifier.size(SqueezableIconSize), onClick = { setIntensity(null) }) {
                                    HeartRemoveIcon()
                                }

                                DragButton(this@ReorderableColumn, view)
                            }
                        })

                        Row(modifier = Modifier.padding(SmallPadding), verticalAlignment = Alignment.CenterVertically) {
                            Header(Modifier.weight(NarrowWeight), "Set")
                            // PerfVarCategory Dropdown with Icon
                            Row(
                                modifier = Modifier
                                    .weight(WideWeight)
                                    .clickable { showTargetEditDropdown = true },
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Header(text = exercise.perfVarCategory.prettyName)
                                Icon(Sharp.KeyboardArrowDown, contentDescription = null)

                                DropdownMenu(expanded = showTargetEditDropdown,
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
                                Header(Modifier.weight(WideWeight), "RPE")
                            }
                            Header(Modifier.weight(NarrowWeight), "")
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
                                            message = "${set.targetPerfVar.encodeToStringOutput()} of ${exerciseDescriptor.name} deleted",
                                            actionLabel = "Undo",
                                            withDismissAction = true
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
                                        .background(color = MaterialTheme.colorScheme.focalGround)
                                        .padding(vertical = RegularPadding, horizontal = SmallPadding),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        modifier = Modifier.weight(NarrowWeight),
                                        text = (setIndex + 1).toString(),
                                        textAlign = TextAlign.Center
                                    )
                                    ExerciseTargetField(Modifier
                                        .weight(WideWeight)
                                        .padding(horizontal = SmallSpacing),
                                        value = set.targetPerfVar,
                                        onValueChange = {
                                            programViewModel.setExercise(
                                                dayIndex, exerciseIndex, exercise.copy(
                                                    sets = exercise.sets.smallListSet(setIndex, set.copy(targetPerfVar = it))
                                                )
                                            )
                                        })
                                    if (set.actualIntensity != null) {
                                        NumberField(modifier = Modifier
                                            .weight(WideWeight)
                                            .padding(horizontal = SmallSpacing),
                                            value = set.actualIntensity,
                                            onValueChange = {
                                                programViewModel.setExercise(
                                                    dayIndex, exerciseIndex, exercise.copy(
                                                        sets = exercise.sets.smallListSet(
                                                            setIndex, set.copy(actualIntensity = max(min(it, 10f), 0f))
                                                        )
                                                    )
                                                )
                                            })
                                    }
                                    IconButton(modifier = Modifier
                                        .size(CompactIconSize)
                                        .weight(NarrowWeight), onClick = {
                                        programViewModel.setExercise(
                                            dayIndex, exerciseIndex, exercise.copy(sets = exercise.sets + set)
                                        )
                                    }) {
                                        DuplicateIcon()
                                    }
                                }
                            }
                        }

                        OutlinedButton(modifier = Modifier
                            .fillMaxWidth()
                            .padding(RegularPadding),
                            colors = ButtonDefaults.outlinedButtonColors()
                                .copy(contentColor = MaterialTheme.colorScheme.tertiary),
                            onClick = {
                                programViewModel.setExercise(
                                    dayIndex, exerciseIndex, exercise.copy(
                                        sets = exercise.sets + ExerciseSet(
                                            actualIntensity = exercise.intensity?.let { 0f },
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

            if (showSwapExerciseChooser) ExerciseChooser(exerciseDao = exerciseDao,
                historyDao = historyDao,
                onDismissRequest = { showSwapExerciseChooser = false },
                onChoose = { programViewModel.setExercise(dayIndex, exerciseIndex, exercise.copy(descriptorId = it.id)) })
        }
    }

    var showAddExerciseChooser by rememberSaveable { mutableStateOf(false) }
    if (showAddExerciseChooser) ExerciseChooser(exerciseDao = exerciseDao,
        historyDao = historyDao,
        onDismissRequest = { showAddExerciseChooser = false },
        onChoose = {
            programViewModel.setDay(
                dayIndex, day.copy(
                    entries = day.entries + WorkoutEntry(
                        descriptorId = it.id, sets = listOf(ExerciseSet(targetPerfVar = PerfVar.Reps()))
                    )
                )
            )
        })
    AnchoredFloatingActionButton(text = { Text("Add Exercise") },
        icon = { Icon(Icons.Filled.Add, null) },
        onClick = { showAddExerciseChooser = true })
}