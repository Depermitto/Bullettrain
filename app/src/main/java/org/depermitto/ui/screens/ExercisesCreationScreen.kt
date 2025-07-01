package org.depermitto.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.depermitto.database.ExerciseDao
import org.depermitto.database.ExerciseListing

@Composable
fun ExercisesCreationScreen(
    exerciseDao: ExerciseDao,
) {
    val scope = rememberCoroutineScope { Dispatchers.IO }

    Column {
        var name by remember { mutableStateOf("") }
        TextField(value = name, onValueChange = { name = it })

        Button(onClick = {
            scope.launch {
                exerciseDao.upsert(ExerciseListing(name = name))
            }
        }) {
            Text(text = "Create")
        }
    }
}
