package io.github.depermitto.bullettrain.exercises

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import io.github.depermitto.bullettrain.database.Exercise
import io.github.depermitto.bullettrain.database.ExerciseDao

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseChooser(onDismissRequest: () -> Unit, exerciseDao: ExerciseDao, onChoose: (Exercise) -> Unit) {
    val sheetState = rememberModalBottomSheetState(true)
    ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState) {
        ExercisesScreen(exerciseDao = exerciseDao, onSelection = { onChoose(it); onDismissRequest() })
    }
}