package io.github.depermitto.bullettrain.exercises

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.depermitto.bullettrain.components.AnchoredFloatingActionButton
import io.github.depermitto.bullettrain.components.GhostCard
import io.github.depermitto.bullettrain.components.TextFieldAlertDialog
import io.github.depermitto.bullettrain.database.Exercise
import io.github.depermitto.bullettrain.database.ExerciseDao
import io.github.depermitto.bullettrain.theme.RegularPadding
import io.github.depermitto.bullettrain.theme.RegularSpacing
import io.github.depermitto.bullettrain.theme.unlinedColors

@Composable
fun ExercisesListScreen(
    exerciseDao: ExerciseDao,
    modifier: Modifier = Modifier,
    onSelection: (Exercise) -> Unit,
) = Box(modifier = modifier.fillMaxSize()) {
    var searchText by rememberSaveable { mutableStateOf("") }
    val exercises by exerciseDao.where(name = searchText, errorTolerance = 3, ignoreCase = true)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    Column(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(horizontal = RegularPadding)
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(shape = RoundedCornerShape(24.dp))
                .shadow(2.dp, shape = RoundedCornerShape(24.dp)),
            value = searchText, onValueChange = { searchText = it },
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.unlinedColors(),
            maxLines = 1,
            singleLine = true,
            placeholder = { Text(text = "Search Exercises") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Exercises") },
        )

        LazyColumn(contentPadding = PaddingValues(bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(RegularSpacing)) {
            items(exercises) { exercise ->
                GhostCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelection(exercise) }) {
                    Text(text = exercise.name, modifier = Modifier.padding(10.dp))
                }
            }
        }
    }

    var showDialog by rememberSaveable { mutableStateOf(false) }
    AnchoredFloatingActionButton(
        onClick = { showDialog = true },
        icon = { Icon(Icons.Default.Add, contentDescription = "Add Exercise") },
        text = { Text(text = "Add") },
    )

    if (showDialog) {
        var errorMessage by rememberSaveable { mutableStateOf("") }
        TextFieldAlertDialog(
            onDismissRequest = { showDialog = false },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
            confirmButton = { name ->
                TextButton(onClick = {
                    errorMessage = exerciseDao.validateName(name) ?: "".also {
                        showDialog = false
                        exerciseDao.insert(Exercise(name = name))
                    }
                }) {
                    Text("Confirm")
                }
            },
            errorMessage = errorMessage,
            isError = errorMessage.isNotBlank()
        )
    }
}
