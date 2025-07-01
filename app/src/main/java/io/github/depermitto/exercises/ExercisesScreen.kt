package io.github.depermitto.exercises

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import io.github.depermitto.components.AnchoredFloatingActionButton
import io.github.depermitto.components.TextFieldDialogTemplate
import io.github.depermitto.database.Exercise
import io.github.depermitto.database.ExerciseDao
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.ItemSpacing
import io.github.depermitto.theme.filledContainerColor
import io.github.depermitto.theme.notUnderlinedTextFieldColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(exerciseDao: ExerciseDao, onSelection: (Exercise) -> Unit) = Box(modifier = Modifier.fillMaxSize()) {
    val exercises by exerciseDao.getAll.collectAsStateWithLifecycle()
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

        items(exercises.filter { it.name.lowercase().contains(searchText.lowercase()) }) { exercise ->
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

    AnimatedVisibility(visible = showDialog, enter = scaleIn(), exit = scaleOut()) {
        BasicAlertDialog(onDismissRequest = { showDialog = false }) {
            var exercise by remember { mutableStateOf(Exercise(name = "")) }
            TextFieldDialogTemplate(value = exercise.name,
                onValueChange = { exercise = exercise.copy(name = it) },
                label = { Text("Exercise Name") },
                onDismiss = { showDialog = false },
                onConfirm = { name ->
                    if (exercises.any { it.name == name }) return@TextFieldDialogTemplate false

                    exerciseDao.upsert(exercise)
                    showDialog = false

                    true
                },
                errorMessage = { Text("Duplicate Name") })
        }
    }
}
