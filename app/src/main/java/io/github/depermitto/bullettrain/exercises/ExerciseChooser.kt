package io.github.depermitto.bullettrain.exercises

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import io.github.depermitto.bullettrain.database.entities.ExerciseDao
import io.github.depermitto.bullettrain.database.entities.ExerciseDescriptor
import io.github.depermitto.bullettrain.database.entities.HistoryDao

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseChooser(
    onDismissRequest: () -> Unit,
    exerciseDao: ExerciseDao,
    historyDao: HistoryDao,
    filter: ((ExerciseDescriptor) -> Boolean)? = null,
    onSelection: (ExerciseDescriptor) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState) {
        ExercisesListScreen(exerciseDao = exerciseDao,
            historyDao = historyDao,
            filter = filter,
            onSelection = { onSelection(it); onDismissRequest() })
    }
}