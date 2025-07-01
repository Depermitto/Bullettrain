package io.github.depermitto.screens.exercises

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.depermitto.data.Exercise
import io.github.depermitto.data.ExerciseDao

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseChooser(exerciseDao: ExerciseDao, onChoose: (Exercise) -> Unit) {
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(true)
    OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { showBottomSheet = true }) {
        Text(text = "Add Exercise")
    }

    if (showBottomSheet) ModalBottomSheet(
        onDismissRequest = { showBottomSheet = false }, sheetState = sheetState
    ) {
        ExercisesScreen(exerciseDao = exerciseDao, onSelection = {
            onChoose(it)
            showBottomSheet = false
        })
    }
}