package io.github.depermitto.screens.programs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.depermitto.components.NumberField
import io.github.depermitto.data.Exercise
import io.github.depermitto.replaceAt
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.ItemSpacing

// TODO Remove sets, make supersets, duplicate exercises, duplicate days, obviously BETTER UI
@Composable
fun SetScreen(set: List<Exercise>, onSetChange: (List<Exercise>?) -> Unit) {
    LazyColumn(
        modifier = Modifier.heightIn(0.dp, 220.dp),
        contentPadding = PaddingValues(ItemPadding),
        verticalArrangement = Arrangement.spacedBy(ItemPadding)
    ) {
        itemsIndexed(set) { i, exercise ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ItemSpacing)
            ) {
                NumberField(value = i + 1f, onValueChange = { }, label = "Set", readOnly = true)
                NumberField(
                    value = exercise.reps,
                    onValueChange = { onSetChange(set.replaceAt(i, exercise.copy(reps = it))) },
                    label = "Reps"
                )
                NumberField(
                    value = exercise.rpe,
                    onValueChange = { onSetChange(set.replaceAt(i, exercise.copy(rpe = it))) },
                    label = "RPE"
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
                    val firstExercise = set.firstOrNull() ?: return@Button
                    onSetChange(set + firstExercise.copy(reps = 0f, rpe = 0f))
                },
            ) {
                Text(text = "Add Set")
            }
        }
    }
}