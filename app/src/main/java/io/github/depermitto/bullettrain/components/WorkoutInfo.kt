package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.depermitto.bullettrain.database.Day
import io.github.depermitto.bullettrain.database.Exercise
import io.github.depermitto.bullettrain.database.Program
import io.github.depermitto.bullettrain.database.ProgramDao
import io.github.depermitto.bullettrain.theme.CardPadding
import io.github.depermitto.bullettrain.theme.ItemPadding

@Composable
fun WorkoutInfo(
    modifier: Modifier = Modifier,
    map: (List<Exercise>) -> List<String>,
    workout: Day,
    program: Program,
    exercisesToSetsRatio: Float = 0.5f
) = Column(modifier = modifier) {
    assert(exercisesToSetsRatio <= 1f && exercisesToSetsRatio >= 0f)

    if (program == ProgramDao.EmptyWorkout) ListItem(
        headlineContent = { Text(text = "Impromptu Workout", style = MaterialTheme.typography.titleLarge) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    ) else ListItem(
        // values taken from https://m3.material.io/components/lists/specs#eeeb78e0-265d-4e81-96ba-c2340c348a90
        headlineContent = { Text(text = program.name, style = MaterialTheme.typography.titleLarge) },
        supportingContent = { Text(text = workout.name) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )

    val processedExercises = map(workout.exercises)
    if (processedExercises.isEmpty()) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ItemPadding), text = "Empty Workout", textAlign = TextAlign.Center
        )
        return@Column
    }

    // This weird padding is for equalizing padding for ListItem
    Column(modifier = Modifier.padding(CardPadding)) {
        Row {
            Text(text = "Exercise", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "Sets", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        }
        HorizontalDivider()

        workout.exercises.zip(processedExercises).forEach { (exercise, info) ->
            Row {
                Box(modifier = Modifier.weight(exercisesToSetsRatio), contentAlignment = CenterStart) {
                    val scroll = rememberScrollState(0)
                    Text(modifier = Modifier.horizontalScroll(scroll), text = exercise.name, maxLines = 1)
                }
                Spacer(modifier = Modifier.width(20.dp))
                Box(modifier = Modifier.weight(1f - exercisesToSetsRatio), contentAlignment = CenterEnd) {
                    val scroll = rememberScrollState(0)
                    Text(
                        modifier = Modifier.horizontalScroll(scroll),
                        text = info,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}