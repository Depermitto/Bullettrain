package io.github.depermitto.screens.programs

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
import io.github.depermitto.data.Sets
import io.github.depermitto.set
import io.github.depermitto.components.ExpandableOutlinedCard
import io.github.depermitto.components.OutlinedTextField
import io.github.depermitto.theme.paddingDp
import kotlin.math.min
import kotlin.math.roundToInt

const val spacerWeight = 0.1f
const val textBoxesWeight = 1f

// TODO Remove sets, make supersets, duplicate exercises, duplicate days, obviously BETTER UI
@Composable
fun SetsScreen(
    title: @Composable () -> Unit,
    sets: Sets,
    onSetsChange: (Sets?) -> Unit,
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
                        value = exercise.reps.parseToNumericInput(),
                        onValueChange = {
                            onSetsChange(sets.set(setIndex, exercise.copy(reps = it.parseFromNumericInput())))
                        },
                        label = { Text(text = "Reps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Spacer(modifier = Modifier.weight(spacerWeight))
                    OutlinedTextField(
                        modifier = Modifier.weight(textBoxesWeight),
                        value = exercise.rpe.parseToNumericInput(),
                        onValueChange = {
                            onSetsChange(sets.set(setIndex, exercise.copy(rpe = min(10f, it.parseFromNumericInput()))))
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
                        onSetsChange(sets + firstExercise.copy(reps = 0f, rpe = 0f))
                    },
                ) {
                    Text(text = "Add Set")
                }
            }
        }
    }
}

fun String.parseFromNumericInput(): Float = this.takeIf { it.isNotBlank() && it.isDigitsOnly() }?.toFloat() ?: 0f
fun Float.parseToNumericInput(): String = this.roundToInt().takeUnless { it == 0 }?.toString() ?: ""