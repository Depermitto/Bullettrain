package org.depermitto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import org.depermitto.data.Exercise
import org.depermitto.set
import org.depermitto.ui.components.ExpandableOutlinedCard
import org.depermitto.ui.components.OutlinedTextField
import org.depermitto.ui.theme.paddingDp
import kotlin.math.min
import kotlin.math.roundToInt

const val spacerWeight = 0.1f
const val textBoxesWeight = 1f

// TODO Remove sets, make supersets, duplicate exercises, duplicate days, obviously BETTER UI
@Composable
fun ProgramExercisesCreationScreen(
    title: @Composable () -> Unit,
    sets: List<Exercise>,
    onExerciseChange: (List<Exercise>?) -> Unit,
) {
    ExpandableOutlinedCard(title = title) {
        LazyColumn(
            modifier = Modifier.heightIn(0.dp, 220.dp),
            contentPadding = PaddingValues(paddingDp),
            verticalArrangement = Arrangement.spacedBy(paddingDp)
        ) {
            itemsIndexed(sets) { setIndex, exercise ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(textBoxesWeight),
                        label = { Text(text = "Set") },
                        value = (setIndex + 1).toString(),
                        onValueChange = { throw UnsupportedOperationException("Unreachable") },
                        singleLine = true,
                        readOnly = true,
                    )
                    Spacer(modifier = Modifier.weight(spacerWeight))
                    OutlinedTextField(
                        modifier = Modifier.weight(textBoxesWeight),
                        value = exercise.reps.roundToInt().takeUnless { it == 0 }?.toString() ?: "",
                        onValueChange = {
                            onExerciseChange(
                                sets.set(
                                    setIndex, exercise.copy(reps = it.ifBlank { "0" }.toFloat())
                                )
                            )
                        },
                        label = { Text(text = "Reps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Spacer(modifier = Modifier.weight(spacerWeight))
                    OutlinedTextField(
                        modifier = Modifier.weight(textBoxesWeight),
                        value = exercise.rpe.roundToInt().takeUnless { it == 0 }?.toString() ?: "",
                        onValueChange = {
                            onExerciseChange(
                                sets.set(
                                    setIndex,
                                    exercise.copy(
                                        rpe = min(
                                            10f, it.takeIf { it.isNotBlank() && it.isDigitsOnly() }?.toFloat() ?: 0f
                                        )
                                    ),
                                )
                            )
                        },
                        label = { Text(text = "RPE") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
            }

            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    onClick = {
                        val firstExercise = sets.firstOrNull() ?: return@Button
                        onExerciseChange(sets + firstExercise.copy(reps = 0f, rpe = 0f))
                    },
                ) {
                    Text(text = "Add Set")
                }
            }
        }
    }
}