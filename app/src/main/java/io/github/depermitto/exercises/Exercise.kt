package io.github.depermitto.exercises

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.sharp.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import io.github.depermitto.components.DropdownButton
import io.github.depermitto.components.NumberField
import io.github.depermitto.components.TargetNumberField
import io.github.depermitto.data.*
import io.github.depermitto.theme.*

@Composable
fun Exercise(exercise: Exercise, onExerciseChange: (Exercise?) -> Unit) = Card(
    colors = CardDefaults.cardColors(containerColor = filledContainerColor())
) {
    var showSetEditDropdown by remember { mutableStateOf(false) }
    var showTargetEditDropdown by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(ItemPadding), verticalArrangement = Arrangement.spacedBy(2 * ItemSpacing)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
            )
            DropdownButton(show = showSetEditDropdown, onShowChange = { showSetEditDropdown = it }) {
                DropdownMenuItem(leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                    text = { Text(text = "Delete") },
                    onClick = {
                        onExerciseChange(null)
                        showSetEditDropdown = false
                    })
                DropdownMenuItem(leadingIcon = { IntensityIcon() },
                    text = { Text(text = if (!exercise.hasIntensity) "Add Intensity" else "Remove Intensity") },
                    onClick = {
                        onExerciseChange(
                            exercise.copy(intensityCategory = if (!exercise.hasIntensity) IntensityCategory.RPE else null,
                                sets = exercise.sets.map { it.copy(intensity = if (!exercise.hasIntensity) 0f else null) })
                        )
                        showSetEditDropdown = false
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
                Header(Modifier.weight(ExerciseSetNarrowWeight), "Set")
                Row(
                    modifier = Modifier
                        .weight(ExerciseSetWideWeight)
                        .clickable { showTargetEditDropdown = true },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Header(text = exercise.targetCategory.prettyName)
                    Icon(Icons.Sharp.KeyboardArrowDown, contentDescription = null)

                    DropdownMenu(
                        expanded = showTargetEditDropdown,
                        onDismissRequest = { showTargetEditDropdown = false }) {
                        ExerciseTargetCategory.entries.forEach { entry ->
                            DropdownMenuItem(text = { Text(entry.prettyName) }, onClick = {
                                onExerciseChange(
                                    exercise.copy(
                                        sets = exercise.sets.map { it.copy(target = ExerciseTarget.of(entry)) },
                                        targetCategory = entry
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

            exercise.sets.forEachIndexed { j, set ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(ExerciseSetNarrowWeight),
                        text = (j + 1).toString(),
                        textAlign = TextAlign.Center
                    )
                    TargetNumberField(
                        Modifier
                            .weight(ExerciseSetWideWeight)
                            .padding(horizontal = ExerciseSetSpacing),
                        value = set.target,
                        onValueChange = {
                            onExerciseChange(exercise.copy(sets = exercise.sets.set(j, set.copy(target = it))))
                        })
                    if (set.intensity != null) {
                        NumberField(
                            Modifier
                                .weight(ExerciseSetWideWeight)
                                .padding(horizontal = ExerciseSetSpacing),
                            value = set.intensity,
                            onValueChange = {
                                onExerciseChange(exercise.copy(sets = exercise.sets.set(j, set.copy(intensity = it))))
                            })
                    }
                    IconButton(modifier = Modifier
                        .size(20.dp)
                        .weight(ExerciseSetNarrowWeight),
                        onClick = { onExerciseChange(exercise.copy(sets = exercise.sets + set)) }) { DuplicateIcon() }
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
                            intensity = exercise.intensityCategory?.let { 0f },
                            target = ExerciseTarget.of(exercise.targetCategory)
                        )
                    )
                )
            }) {
            Text(text = "Add Set")
        }
    }
}

@Composable
fun Header(
    modifier: Modifier = Modifier,
    text: String,
) = Text(modifier = modifier, text = text, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
