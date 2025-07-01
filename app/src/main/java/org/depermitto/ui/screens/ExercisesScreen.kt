package org.depermitto.ui.screens

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
import org.depermitto.database.ExerciseDao
import org.depermitto.database.WorkoutEntry
import org.depermitto.ui.theme.filledContainerColor
import org.depermitto.ui.theme.horizontalDp
import org.depermitto.ui.theme.notUnderlinedTextFieldColors
import org.depermitto.ui.theme.spacingDp

@Composable
fun ExercisesScreen(exerciseDao: ExerciseDao, onSelection: (WorkoutEntry) -> Unit) {
    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalDp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val exercises by exerciseDao.getAllFlow().collectAsState(emptyList())
            var searchText by remember { mutableStateOf("") }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(spacingDp)) {
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
                        onClick = { onSelection(WorkoutEntry(exercise)) }) {
                        Text(text = exercise.name, modifier = Modifier.padding(10.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .padding(bottom = 3 * horizontalDp, end = 2 * horizontalDp)
                .align(Alignment.BottomEnd),
            onClick = { TODO() },
            shape = MaterialTheme.shapes.large,
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
        }
    }
}
