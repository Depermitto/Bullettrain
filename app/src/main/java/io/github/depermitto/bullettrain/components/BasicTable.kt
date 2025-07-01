package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.depermitto.bullettrain.database.Day
import io.github.depermitto.bullettrain.database.Exercise
import io.github.depermitto.bullettrain.database.Program
import io.github.depermitto.bullettrain.theme.RegularPadding

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
    headlineLeadingContent: @Composable (() -> Unit)? = null,
    headlineOverlineContent: @Composable (() -> Unit)? = null,
    emptyMessage: String = "No Information To Present",
    ratio: Ratio = Ratio.Unlimited,
    headers: Pair<String, String>,
    separateHeadersAndContent: Boolean = true,
    list: List<T>,
    content: (Int, T) -> Pair<String, String>
) = Column(modifier = modifier) {
    ListItem(
        headlineContent = headlineContent,
        supportingContent = headlineSupportingContent,
        overlineContent = headlineOverlineContent,
        leadingContent = headlineLeadingContent,
        trailingContent = headlineTrailingContent,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )

    if (list.isEmpty()) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RegularPadding), text = emptyMessage, textAlign = TextAlign.Center
        )
        return@Column
    }

    // This weird padding is for equalizing padding for ListItem
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
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
                    Text(text = col1, overflow = TextOverflow.Ellipsis, maxLines = 1)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = col2, overflow = TextOverflow.Ellipsis, maxLines = 1)
                } else if (ratio is Ratio.Strict) {
                    assert(ratio.value > 0f && ratio.value < 1f)

                    Box(modifier = Modifier.weight(ratio.value), contentAlignment = CenterStart) {
                        Text(text = col1, overflow = TextOverflow.Ellipsis, maxLines = 1)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f - ratio.value), contentAlignment = CenterEnd) {
                        Text(text = col2, overflow = TextOverflow.Ellipsis, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutTable(
    modifier: Modifier = Modifier,
    workout: Day,
    program: Program,
    trailingContent: (@Composable () -> Unit)? = null,
    exstractor: (Exercise) -> String?,
    ratio: Ratio = Ratio.Unlimited
) {
    var header = program.name
    var supportingText: String? = workout.name

    if (program == Program.EmptyWorkout) {
        header = "Impromptu Workout"
        supportingText = null
    }

    BasicTable(
        modifier = modifier,
        headlineContent = { Text(text = header, style = MaterialTheme.typography.titleLarge) },
        headlineSupportingContent = supportingText?.let { { Text(text = supportingText) } },
        headlineTrailingContent = trailingContent,
        ratio = ratio,
        emptyMessage = "Empty Workout",
        headers = Pair("Exercise", "Sets"),
        list = workout.exercises.mapNotNull { exercise -> exstractor(exercise)?.let { exercise.name to it } },
    ) { _, item ->
        item
    }
}
