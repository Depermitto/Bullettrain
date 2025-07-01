package io.github.depermitto.bullettrain.exercises

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import io.github.depermitto.bullettrain.database.ExerciseDao
import io.github.depermitto.bullettrain.database.ExerciseDescriptor
import io.github.depermitto.bullettrain.database.HistoryDao

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseChooser(
    onDismissRequest: () -> Unit, exerciseDao: ExerciseDao, historyDao: HistoryDao, onChoose: (ExerciseDescriptor) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(true)
    ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState) {
        ExercisesListScreen(exerciseDao = exerciseDao,
            historyDao = historyDao,
            onSelection = { onChoose(it); onDismissRequest() })
    }
}