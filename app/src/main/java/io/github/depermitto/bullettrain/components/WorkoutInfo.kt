package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.depermitto.bullettrain.database.Day
import io.github.depermitto.bullettrain.database.Exercise
import io.github.depermitto.bullettrain.database.Program
import io.github.depermitto.bullettrain.database.ProgramDao
import io.github.depermitto.bullettrain.theme.CardPadding

@Composable
fun WorkoutInfo(
    modifier: Modifier = Modifier,
    exerciseInfo: @Composable (Exercise) -> Unit,
    workout: Day,
    program: Program,
) = Column(modifier = modifier) {
    if (program == ProgramDao.EmptyWorkout) ListItem(
        headlineContent = { Text(text = "Impromptu Workout", style = MaterialTheme.typography.titleLarge) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    ) else ListItem(
        // values taken from https://m3.material.io/components/lists/specs#eeeb78e0-265d-4e81-96ba-c2340c348a90
        headlineContent = { Text(text = program.name, style = MaterialTheme.typography.titleLarge) },
        supportingContent = { Text(text = "${workout.name} Week ${program.weekStreak}") },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )

    // This weird padding is for equalizing padding for ListItem
    LazyColumn(contentPadding = PaddingValues(CardPadding)) {
        item {
            Row {
                Text(text = "Exercise", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "Sets", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
            HorizontalDivider()
        }

        items(workout.exercises) { exercise ->
            Row {
                Box(modifier = Modifier.weight(0.5f), contentAlignment = CenterStart) {
                    val scroll = rememberScrollState(0)
                    Text(modifier = Modifier.horizontalScroll(scroll), text = exercise.name, maxLines = 1)
                }
                Spacer(modifier = Modifier.width(20.dp))
                Box(modifier = Modifier.weight(0.5f), contentAlignment = CenterEnd) {
                    exerciseInfo(exercise)
                }
            }
        }
    }
}