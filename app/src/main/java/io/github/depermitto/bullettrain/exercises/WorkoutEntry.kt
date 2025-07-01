package io.github.depermitto.bullettrain.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.depermitto.bullettrain.components.SwipeToDeleteBox
import io.github.depermitto.bullettrain.database.entities.*
import io.github.depermitto.bullettrain.database.entities.WorkoutEntry
import io.github.depermitto.bullettrain.theme.Medium
import io.github.depermitto.bullettrain.theme.Small
import io.github.depermitto.bullettrain.theme.focalGround
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun WorkoutEntry(
    workoutEntry: WorkoutEntry,
    onWorkoutEntryChange: (WorkoutEntry) -> Unit,
    modifier: Modifier = Modifier,
    headline: @Composable () -> Unit,
    headerContent: @Composable RowScope.() -> Unit,
    exerciseDescriptor: ExerciseDescriptor,
    settings: Settings,
    scope: CoroutineScope = rememberCoroutineScope(),
    snackbarHostState: SnackbarHostState,
    content: @Composable RowScope.(Int, ExerciseSet) -> Unit,
) = Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.focalGround(settings.theme))
) {
    // TODO next step: try to overwrite BasicTable with this
    headline.invoke()
    CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.merge(MaterialTheme.typography.titleSmall)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dp.Medium, vertical = Dp.Small),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            headerContent.invoke(this)
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = Dp.Medium))
    CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.merge(MaterialTheme.typography.bodyLarge)) {
        workoutEntry.sets.forEachIndexed<ExerciseSet> { setIndex, set ->
            SwipeToDeleteBox(onDelete = {
                val deletedExercise = workoutEntry
                onWorkoutEntryChange(workoutEntry.copy(sets = workoutEntry.sets.filterIndexed<ExerciseSet> { i, _ -> i != setIndex }))
                if (set.actualPerfVar != 0f) scope.launch {
                    val snackBarResult = snackbarHostState.showSnackbar(
                        message = "Set ${setIndex + 1} of ${exerciseDescriptor.name} deleted",
                        actionLabel = "Undo",
                        withDismissAction = true,
                    )
                    if (snackBarResult == SnackbarResult.ActionPerformed) {
                        onWorkoutEntryChange(deletedExercise)
                    }
                }
            }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.focalGround(settings.theme))
                        .padding(Dp.Medium),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    content.invoke(this, setIndex, set)
                }
            }
        }
    }
    Spacer(Modifier.height(12.dp)) // Equivalent to HeroTile vertical Dp
}
