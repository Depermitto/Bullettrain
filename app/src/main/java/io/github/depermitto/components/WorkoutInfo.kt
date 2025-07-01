package io.github.depermitto.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.depermitto.data.entities.Day
import io.github.depermitto.data.entities.Exercise
import io.github.depermitto.data.entities.Program
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.filledContainerColor

@Composable
fun WorkoutInfo(
    modifier: Modifier = Modifier,
    exerciseInfo: @Composable (Exercise) -> Unit,
    workout: Day,
    program: Program,
) = Column(modifier = modifier.padding(ItemPadding * 2)) {
    ListItem(
        // values taken from https://m3.material.io/components/lists/specs#eeeb78e0-265d-4e81-96ba-c2340c348a90
        modifier = Modifier.offset(x = (-16).dp, y = (-16).dp),
        headlineContent = { Text(text = program.name, style = MaterialTheme.typography.titleLarge) },
        supportingContent = { Text(text = "Day ${program.nextDay + 1} Week ${program.weekStreak}") },
        colors = ListItemDefaults.colors(containerColor = filledContainerColor())
    )

    Row {
        Text(text = "Exercise", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "Sets", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
    }
    HorizontalDivider()

    LazyColumn {
        items(workout.exercises) { exercise ->
            Row {
                Text(text = exercise.name, maxLines = 1)
                Spacer(modifier = Modifier.weight(1f))
                exerciseInfo(exercise)
            }
        }
    }
}