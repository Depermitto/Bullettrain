package org.depermitto.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.depermitto.database.ExerciseDao
import org.depermitto.database.ExerciseListing
import org.depermitto.ui.theme.OutlinedCardColumn
import org.depermitto.ui.theme.transparentTextFieldColors

@Composable
fun ExercisesCreationScreen(
    exerciseDao: ExerciseDao,
) {
    val scope = rememberCoroutineScope { Dispatchers.IO }

    OutlinedCardColumn {
        var name by remember { mutableStateOf("") }
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = { name = it },
            placeholder = { Text(text = "Exercise Name") },
            colors = transparentTextFieldColors()
        )

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { scope.launch { exerciseDao.upsert(ExerciseListing(name = name)) } },
        ) {
            Text(text = "Create")
        }
    }
}
