package org.depermitto.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.depermitto.database.ExerciseDao
import org.depermitto.database.ExerciseListing

@Composable
fun ExercisesCreationScreen(
    modifier: Modifier,
    exerciseDao: ExerciseDao,
    navController: NavController,
) {
    val scope = rememberCoroutineScope { Dispatchers.IO }
    
    Column(modifier = modifier) {
        var name by remember { mutableStateOf("") }
        TextField(value = name, onValueChange = { name = it })

        Button(onClick = {
            scope.launch {
                exerciseDao.upsert(ExerciseListing(name = name))
            }
            navController.popBackStack()
        }) {
            Text(text = "Create")
        }
    }
}
