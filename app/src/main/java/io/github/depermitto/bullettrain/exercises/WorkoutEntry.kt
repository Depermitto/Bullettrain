package io.github.depermitto.bullettrain.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.github.depermitto.bullettrain.components.DataPanel
import io.github.depermitto.bullettrain.components.SwipeToDeleteBox
import io.github.depermitto.bullettrain.database.entities.ExerciseDescriptor
import io.github.depermitto.bullettrain.database.entities.ExerciseSet
import io.github.depermitto.bullettrain.database.entities.Settings
import io.github.depermitto.bullettrain.database.entities.WorkoutEntry
import io.github.depermitto.bullettrain.theme.Medium
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
  settings: Settings,
  exerciseDescriptor: ExerciseDescriptor,
  scope: CoroutineScope = rememberCoroutineScope(),
  snackbarHostState: SnackbarHostState,
  content: @Composable RowScope.(Int, ExerciseSet) -> Unit,
) {
  DataPanel(
    items = workoutEntry.sets,
    modifier = modifier,
    backgroundColor = focalGround(settings.theme),
    headerPadding = PaddingValues(horizontal = Dp.Medium),
    headline = headline,
    headerContent = headerContent,
  ) { setIndex, set ->
    SwipeToDeleteBox(
      onDelete = {
        onWorkoutEntryChange(
          workoutEntry.copy(sets = workoutEntry.sets.filterIndexed { i, _ -> i != setIndex })
        )
        if (set.weight != 0f || set.actualPerfVar != 0f)
          scope.launch {
            val snackBarResult =
              snackbarHostState.showSnackbar(
                message = "Set ${setIndex + 1} of ${exerciseDescriptor.name} removed",
                actionLabel = "Undo",
                withDismissAction = true,
              )
            if (snackBarResult == SnackbarResult.ActionPerformed) {
              onWorkoutEntryChange(workoutEntry)
            }
          }
      }
    ) {
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .background(color = focalGround(settings.theme))
            .padding(Dp.Medium),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        content.invoke(this, setIndex, set)
      }
    }
  }
}
