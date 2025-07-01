package io.github.depermitto.bullettrain.exercises

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.depermitto.bullettrain.database.Exercise
import io.github.depermitto.bullettrain.database.ExerciseDao

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun exerciseChooser(exerciseDao: ExerciseDao, onChoose: (Exercise) -> Unit): () -> Unit {
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(true)

    if (showBottomSheet) ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState) {
        ExercisesScreen(exerciseDao = exerciseDao, onSelection = {
            onChoose(it)
            showBottomSheet = false
        })
    }

    return { showBottomSheet = !showBottomSheet }
}