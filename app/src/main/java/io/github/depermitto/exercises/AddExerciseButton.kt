package io.github.depermitto.exercises

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.depermitto.database.Exercise
import io.github.depermitto.database.ExerciseDao

@Composable
fun AddExerciseButton(exerciseDao: ExerciseDao, onChoose: (Exercise) -> Unit) {
    val toggler = exerciseChooser(exerciseDao = exerciseDao, onChoose = onChoose)
    OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { toggler() }) {
        Text(text = "Add Exercise")
    }
}

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