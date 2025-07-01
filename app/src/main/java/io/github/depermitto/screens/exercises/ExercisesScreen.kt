package io.github.depermitto.screens.exercises

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import io.github.depermitto.data.Exercise
import io.github.depermitto.data.ExerciseDao
import io.github.depermitto.theme.filledContainerColor
import io.github.depermitto.theme.notUnderlinedTextFieldColors
import io.github.depermitto.theme.paddingDp
import io.github.depermitto.theme.spacingDp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(exerciseDao: ExerciseDao, onSelection: (Exercise) -> Unit) {
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val exercises by exerciseDao.getAllFlow().collectAsState(emptyList())

    Box(modifier = Modifier.fillMaxSize()) {
        var searchText by remember { mutableStateOf("") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(spacingDp), contentPadding = PaddingValues(paddingDp)) {
            item {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = searchText, onValueChange = { searchText = it },
                    shape = MaterialTheme.shapes.medium,
                    colors = notUnderlinedTextFieldColors(),
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
        FloatingActionButton(
            modifier = Modifier
                .padding(bottom = 2 * paddingDp, end = 2 * paddingDp)
                .align(Alignment.BottomEnd),
            onClick = { showDialog = true },
            shape = MaterialTheme.shapes.large,
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
        }

        if (showDialog) {
            BasicAlertDialog(
                modifier = Modifier.align(Alignment.Center),
                onDismissRequest = { showDialog = false },
            ) {
                ExercisesCreationScreen(newExercise = {
                    if (it != null) scope.launch { exerciseDao.upsert(it) }
                    showDialog = false
                })
            }
        }
    }
}
