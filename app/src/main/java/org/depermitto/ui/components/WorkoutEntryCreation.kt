package org.depermitto.ui.components

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
import androidx.compose.ui.unit.times
import androidx.core.text.isDigitsOnly
import org.depermitto.database.WorkoutEntry
import org.depermitto.set
import org.depermitto.ui.theme.horizontalDp
import kotlin.math.min
import kotlin.math.roundToInt

const val spacerWeight = 0.2f
const val textBoxesWeight = 1f

// TODO Remove sets, make supersets
@Composable
fun WorkoutEntriesCreation(
    title: @Composable () -> Unit,
    workoutEntries: List<WorkoutEntry>,
    onWorkoutEntryChange: (List<WorkoutEntry>?) -> Unit,
) {
    ExpandableOutlinedCard(title = title) {
        LazyColumn(modifier = Modifier.heightIn(0.dp, 220.dp)) {
            itemsIndexed(workoutEntries) { i, workoutEntry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontalDp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.weight(spacerWeight))
                    OutlinedTextField(
                        modifier = Modifier.weight(textBoxesWeight),
                        label = { Text(text = "Set") },
                        value = (i + 1).toString(),
                        onValueChange = { },
                        singleLine = true,
                        readOnly = true,
                    )
                    Spacer(modifier = Modifier.weight(spacerWeight))
                    OutlinedTextField(
                        modifier = Modifier.weight(textBoxesWeight),
                        label = { Text(text = "Reps") },
                        value = workoutEntry.reps.roundToInt().takeUnless { it == 0 }?.toString() ?: "",
                        onValueChange = {
                            onWorkoutEntryChange(
                                workoutEntries.set(
                                    i, workoutEntry.copy(reps = it.ifBlank { "0" }.toFloat())
                                )
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Spacer(modifier = Modifier.weight(spacerWeight))
                    OutlinedTextField(
                        modifier = Modifier.weight(textBoxesWeight),
                        label = { Text(text = "RPE") },
                        value = workoutEntry.rpe.roundToInt().takeUnless { it == 0 }?.toString() ?: "",
                        onValueChange = {
                            onWorkoutEntryChange(
                                workoutEntries.set(
                                    i,
                                    workoutEntry.copy(
                                        rpe = min(
                                            10f, it.takeIf { it.isNotBlank() && it.isDigitsOnly() }?.toFloat() ?: 0f
                                        )
                                    ),
                                )
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Spacer(modifier = Modifier.weight(spacerWeight))
                }
            }

            item {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 3 * horizontalDp),
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    onClick = { onWorkoutEntryChange(workoutEntries + WorkoutEntry(exercise = workoutEntries.first().exercise)) },
                ) {
                    Text(text = "Add Set")
                }
            }
        }
    }
}