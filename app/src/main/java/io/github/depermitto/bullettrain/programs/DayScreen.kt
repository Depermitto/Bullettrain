package io.github.depermitto.bullettrain.programs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons.Sharp
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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import io.github.depermitto.bullettrain.components.Header
import io.github.depermitto.bullettrain.components.NumberField
import io.github.depermitto.bullettrain.components.SwipeToDeleteBox
import io.github.depermitto.bullettrain.database.Exercise
import io.github.depermitto.bullettrain.database.ExerciseSet
import io.github.depermitto.bullettrain.database.IntensityCategory
import io.github.depermitto.bullettrain.database.PerfVar
import io.github.depermitto.bullettrain.database.PerfVarCategory
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
import sh.calvin.reorderable.ReorderableColumn
import sh.calvin.reorderable.ReorderableScope
import kotlin.math.max
import kotlin.math.min

@Composable
fun DayScreen(
    modifier: Modifier = Modifier, programViewModel: ProgramViewModel, dayIndex: Int
) {
    val day = programViewModel.getDay(dayIndex)
    ReorderableColumn(modifier = modifier,
        list = day.exercises,
        verticalArrangement = Arrangement.spacedBy(CardSpacing),
        onSettle = { fromIndex, toIndex ->
            programViewModel.setDay(dayIndex, day.copy(exercises = day.exercises.reorder(fromIndex, toIndex)))
        }) { exerciseIndex, exercise, isDragging ->
        key(exercise.id) {
            SwipeToDeleteBox(modifier = Modifier.clip(MaterialTheme.shapes.medium),
                threshold = 0.9f,
                onDelete = { programViewModel.setDay(dayIndex, day.copy(exercises = day.exercises - exercise)) }) {
                val onExerciseChange = { it: Exercise -> programViewModel.setExercise(dayIndex, exerciseIndex, it) }
                Card(modifier = Modifier, colors = CardDefaults.cardColors(containerColor = filledContainerColor())) {
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
                                onExerciseChange(
                                    exercise.copy(intensityCategory = cat,
                                        sets = exercise.sets.map<ExerciseSet, ExerciseSet> { it.copy(intensity = intensity) })
                                )
                            }

                            if (!exercise.hasIntensity) IconButton(modifier = Modifier.size(SqueezableIconSize),
                                onClick = { setIntensity(IntensityCategory.RPE) }) {
                                HeartPlusIcon()
                            }
                            else IconButton(modifier = Modifier.size(SqueezableIconSize),
                                onClick = { setIntensity(null) }) {
                                HeartRemoveIcon()
                            }

                            IconButton(modifier = with<ReorderableScope, Modifier>(
                                receiver = this@ReorderableColumn
                            ) { Modifier.draggableHandle() }.size(SqueezableIconSize), onClick = {}) {
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

                                DropdownMenu(expanded = showTargetEditDropdown,
                                    onDismissRequest = { showTargetEditDropdown = false }) {
                                    PerfVarCategory.entries.forEach { entry ->
                                        DropdownMenuItem(text = { Text(entry.prettyName) }, onClick = {
                                            onExerciseChange(
                                                exercise.copy(
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

                        exercise.sets.forEachIndexed<ExerciseSet> { setIndex, set ->
                            SwipeToDeleteBox(onDelete = {
                                programViewModel.setExercise(
                                    dayIndex,
                                    exerciseIndex,
                                    exercise.copy(sets = exercise.sets.filterIndexed<ExerciseSet> { i, _ -> i != setIndex })
                                )
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
                                                    sets = exercise.sets.smallListSet(
                                                        setIndex, set.copy(targetPerfVar = it)
                                                    )
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
                                    IconButton(modifier = Modifier
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
                                onExerciseChange(
                                    exercise.copy(
                                        sets = exercise.sets + ExerciseSet(
                                            intensity = exercise.intensityCategory?.let<IntensityCategory, Float> { 0f },
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