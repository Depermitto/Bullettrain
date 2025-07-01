package io.github.depermitto.bullettrain.exercises

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.depermitto.bullettrain.components.AnchoredFloatingActionButton
import io.github.depermitto.bullettrain.components.TextFieldAlertDialog
import io.github.depermitto.bullettrain.database.Exercise
import io.github.depermitto.bullettrain.database.ExerciseDao
import io.github.depermitto.bullettrain.theme.ItemPadding
import io.github.depermitto.bullettrain.theme.ItemSpacing
import io.github.depermitto.bullettrain.theme.filledContainerColor
import io.github.depermitto.bullettrain.theme.notUnderlinedTextFieldColors

@Composable
fun ExercisesScreen(exerciseDao: ExerciseDao, onSelection: (Exercise) -> Unit) = Box(modifier = Modifier.fillMaxSize()) {
    val exercises by exerciseDao.getSortedAlphabetically.collectAsStateWithLifecycle(initialValue = emptyList())
    var searchText by remember { mutableStateOf("") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(ItemSpacing), contentPadding = PaddingValues(ItemPadding)
    ) {
        item {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = searchText, onValueChange = { searchText = it },
                shape = MaterialTheme.shapes.medium,
                colors = notUnderlinedTextFieldColors(),
                maxLines = 1,
                singleLine = true,
                placeholder = { Text(text = "Search Exercises") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            )
        }

        items(exercises.filter { it.name.lowercase().contains(searchText.lowercase().trim()) }) { exercise ->
            OutlinedCard(colors = CardDefaults.cardColors(containerColor = filledContainerColor()),
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSelection(exercise) }) {
                Text(text = exercise.name, modifier = Modifier.padding(10.dp))
            }
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    AnchoredFloatingActionButton(
        onClick = { showDialog = true },
        icon = { Icon(Icons.Default.Add, contentDescription = "Add Exercise") },
        text = { Text(text = "Add") },
    )

    if (showDialog) {
        var isError by remember { mutableStateOf(false) }
        TextFieldAlertDialog(
            onDismissRequest = { showDialog = false },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
            confirmButton = { name ->
                TextButton(onClick = {
                    if (exercises.any { it.name == name }) {
                        isError = true
                        return@TextButton
                    }

                    exerciseDao.upsert(Exercise(name = name.trim()))
                    showDialog = false
                }) { Text("Confirm") }
            },
            errorMessage = "Duplicate Name",
            isError = isError
        )
    }
}
