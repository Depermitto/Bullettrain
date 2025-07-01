package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destination
import io.github.depermitto.bullettrain.database.entities.*

@Composable
fun <T> BasicTable(
    modifier: Modifier = Modifier,
    headlineContent: @Composable () -> Unit,
    headlineSupportingContent: @Composable (() -> Unit)? = null,
    headlineTrailingContent: @Composable (() -> Unit)? = null,
    overlayingContent: @Composable (() -> Unit)? = null,
    emptyMessage: String = "No Information To Present",
    headers: List<String>,
    separateHeadersAndContent: Boolean = true,
    list: List<T>,
    content: @Composable (Int, T) -> Unit
) = Column(modifier) {
    HeroTile(
        headlineContent = headlineContent,
        supportingContent = headlineSupportingContent,
        trailingContent = headlineTrailingContent,
    )

    if (list.isEmpty()) {
        Spacer(Modifier.weight(1f))
        Text(
            text = emptyMessage, modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp), textAlign = TextAlign.Center
        )
    } else Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
        // ^ This weird padding is for equalizing padding for ListItem ^
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            headers.forEach { header ->
                Text(text = header, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
        }
        if (separateHeadersAndContent) HorizontalDivider()

        list.forEachIndexed { i, item ->
            content(i, item)
        }
    }

    overlayingContent?.let { content ->
        Spacer(Modifier.weight(1f))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@Composable
fun WorkoutTable(
    modifier: Modifier = Modifier,
    workout: Workout,
    program: Program,
    headers: List<String>,
    trailingContent: (@Composable () -> Unit)? = null,
    overlayingContent: (@Composable () -> Unit)? = null,
    exstractor: (WorkoutEntry) -> String?,
    exerciseDao: ExerciseDao,
    navController: NavController,
) {
    var header = program.name
    var supportingText: String? = workout.name

    if (program corresponds Program.EmptyWorkout) {
        header = "Impromptu Workout"
        supportingText = null
    }

    BasicTable(
        modifier = modifier,
        headlineContent = { Text(text = header, style = MaterialTheme.typography.titleLarge) },
        headlineSupportingContent = supportingText?.let { { Text(text = supportingText) } },
        headlineTrailingContent = trailingContent,
        overlayingContent = overlayingContent,
        emptyMessage = "Empty Workout",
        headers = headers,
        list = workout.entries.mapNotNull { exercise -> exstractor(exercise)?.let { text -> exercise to text } },
    ) { _, (exercise, text) ->
        val exerciseDescriptor = exerciseDao.where(exercise.descriptorId)
        HeroTile(
            headlineContent = { Text(text = exerciseDescriptor.name, maxLines = 2) },
            trailingContent = { Text(text = text, overflow = TextOverflow.Ellipsis, maxLines = 2) },
            modifier = Modifier.clip(MaterialTheme.shapes.small),
            onClick = { navController.navigate(Destination.Exercise(exerciseDescriptor.id)) },
            contentPadding = PaddingValues(0.dp)
        )
    }
}
