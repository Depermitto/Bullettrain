package io.github.depermitto.bullettrain.exercises

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.depermitto.bullettrain.components.AnchoredFloatingActionButton
import io.github.depermitto.bullettrain.components.TextFieldAlertDialog
import io.github.depermitto.bullettrain.database.Exercise
import io.github.depermitto.bullettrain.database.ExerciseDao
import io.github.depermitto.bullettrain.theme.CardPadding
import io.github.depermitto.bullettrain.theme.ItemPadding
import io.github.depermitto.bullettrain.theme.ItemSpacing
import io.github.depermitto.bullettrain.theme.focalGround
import io.github.depermitto.bullettrain.theme.unlinedColors

@Composable
fun ExercisesListScreen(
    exerciseDao: ExerciseDao,
    modifier: Modifier = Modifier,
    onSelection: (Exercise) -> Unit,
) = Box(modifier = modifier.fillMaxSize()) {
    var searchText by remember { mutableStateOf("") }
    val exercises by exerciseDao.where(name = searchText, errorTolerance = 2, ignoreCase = true)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    Column(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(horizontal = ItemPadding)
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = CardPadding)
                .clip(shape = MaterialTheme.shapes.medium),
            value = searchText, onValueChange = { searchText = it },
            colors = TextFieldDefaults.unlinedColors(),
            maxLines = 1,
            singleLine = true,
            placeholder = { Text(text = "Search Exercises") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Exercises") },
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(ItemSpacing), contentPadding = PaddingValues(bottom = 100.dp)) {
            items(exercises) { exercise ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.focalGround),
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
        TextFieldAlertDialog(onDismissRequest = { showDialog = false },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
            confirmButton = { name ->
                TextButton(onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Empty Exercise Name"
                        return@TextButton
                    }

                    if (exercises.any { it.name.contentEquals(name.trim(), ignoreCase = true) }) {
                        errorMessage = "Duplicate Exercise Name"
                        return@TextButton
                    }

                    exerciseDao.upsert(Exercise(name = name))
                    showDialog = false
                }) {
                    Text("Confirm")
                }
            },
            errorMessage = errorMessage,
            isError = errorMessage.isNotBlank()
        )
    }
}
