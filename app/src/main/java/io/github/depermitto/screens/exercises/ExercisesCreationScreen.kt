package io.github.depermitto.screens.exercises

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.depermitto.data.Exercise
import io.github.depermitto.theme.paddingDp

@Composable
fun ExercisesCreationScreen(newExercise: (Exercise?) -> Unit) {
    var exerciseState by remember { mutableStateOf(Exercise(name = "")) }

    OutlinedCard(
        modifier = Modifier
            .height(200.dp)
            .width(200.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = paddingDp)
        ) {
            OutlinedTextField(
                modifier = Modifier.align(Alignment.TopCenter),
                value = exerciseState.name, onValueChange = { exerciseState = exerciseState.copy(name = it) },
                label = { Text(text = "Exercise Name") },
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                    cursorColor = MaterialTheme.colorScheme.tertiary,
                    selectionColors = TextSelectionColors(
                        MaterialTheme.colorScheme.tertiary,
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f),
                    ),
                ),
            )

            Row(modifier = Modifier.align(Alignment.BottomEnd)) {
                TextButton(onClick = { newExercise(null) }) {
                    Text(text = "Cancel")
                }

                TextButton(onClick = { newExercise(exerciseState) }) {
                    Text(text = "Confirm")
                }
            }
        }
    }
}