package io.github.depermitto.bullettrain.exercises

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.depermitto.bullettrain.db.ExerciseDao
import io.github.depermitto.bullettrain.db.HistoryDao
import io.github.depermitto.bullettrain.protos.ExercisesProto.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseChooser(
  onDismissRequest: () -> Unit,
  exerciseDao: ExerciseDao,
  historyDao: HistoryDao,
  filter: ((Exercise.Descriptor) -> Boolean)? = null,
  onSelection: (Exercise.Descriptor) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    modifier =
      Modifier.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
    onDismissRequest = onDismissRequest,
    sheetState = sheetState,
  ) {
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
