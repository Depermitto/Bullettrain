package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destination
import io.github.depermitto.bullettrain.database.entities.*

sealed class Ratio {
    data object Unlimited : Ratio()
    data class Strict(val value: Float) : Ratio()
}

@Composable
fun <T> BasicTable(
    modifier: Modifier = Modifier,
    headlineContent: @Composable () -> Unit,
    headlineSupportingContent: @Composable (() -> Unit)? = null,
    headlineTrailingContent: @Composable (() -> Unit)? = null,
    overlayingContent: @Composable (() -> Unit)? = null,
    emptyMessage: String = "No Information To Present",
    ratio: Ratio = Ratio.Unlimited,
    headers: Pair<String, String>,
    separateHeadersAndContent: Boolean = true,
    list: List<T>,
    content: (Int, T) -> Pair<@Composable () -> Unit, @Composable () -> Unit>
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
        Row {
            Text(text = headers.first, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.weight(1f))
            Text(text = headers.second, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        }
        if (separateHeadersAndContent) HorizontalDivider()

        list.forEachIndexed { i, item ->
            Row {
                val (col1, col2) = content(i, item)

                if (ratio == Ratio.Unlimited) {
                    col1()
                    Spacer(modifier = Modifier.weight(1f))
                    col2()
                } else if (ratio is Ratio.Strict) {
                    assert(ratio.value > 0f && ratio.value < 1f)

                    Box(modifier = Modifier.weight(ratio.value), contentAlignment = CenterStart) {
                        col1()
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f - ratio.value), contentAlignment = CenterEnd) {
                        col2()
                    }
                }
            }
        }
    }

    Spacer(Modifier.weight(1f))
    overlayingContent?.let { content ->
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
    headers: Pair<String, String>,
    trailingContent: (@Composable () -> Unit)? = null,
    overlayingContent: (@Composable () -> Unit)? = null,
    exstractor: (WorkoutEntry) -> String?,
    ratio: Ratio = Ratio.Unlimited,
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
        ratio = ratio,
        emptyMessage = "Empty Workout",
        headers = headers,
        list = workout.entries.mapNotNull { exercise -> exstractor(exercise)?.let { text -> exercise to text } },
    ) { _, (exercise, text) ->
        val exerciseDescriptor = exerciseDao.where(exercise.descriptorId)
        Pair(first = {
            TextLink(exerciseDescriptor.name, navController, Destination.Exercise(exerciseDescriptor.id), maxLines = 2)
        }, second = {
            Text(text = text, overflow = TextOverflow.Ellipsis, maxLines = 2)
        })
    }
}
