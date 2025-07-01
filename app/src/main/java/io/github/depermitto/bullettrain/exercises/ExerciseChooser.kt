package io.github.depermitto.bullettrain.exercises

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import io.github.depermitto.bullettrain.database.daos.ExerciseDao
import io.github.depermitto.bullettrain.database.daos.HistoryDao
import io.github.depermitto.bullettrain.database.entities.ExerciseDescriptor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseChooser(
  onDismissRequest: () -> Unit,
  exerciseDao: ExerciseDao,
  historyDao: HistoryDao,
  filter: ((ExerciseDescriptor) -> Boolean)? = null,
  onSelection: (ExerciseDescriptor) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState) {
    ExercisesListScreen(
      exerciseDao = exerciseDao,
      historyDao = historyDao,
      filter = filter,
      onSelection = {
        onSelection(it)
        onDismissRequest()
      },
    )
  }
}
