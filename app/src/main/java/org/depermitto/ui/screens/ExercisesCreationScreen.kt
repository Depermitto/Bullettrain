package org.depermitto.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.depermitto.database.ExerciseDao
import org.depermitto.database.ExerciseListing
import org.depermitto.ui.theme.horizontalDp
import org.depermitto.ui.theme.transparentTextFieldColors

@Composable
fun ExercisesCreationScreen(
    exerciseDao: ExerciseDao,
) {
    OutlinedCard {
        Column {
            var name by remember { mutableStateOf("") }
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(text = "Exercise Name") },
                colors = transparentTextFieldColors()
            )

            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontalDp),
                onClick = { CoroutineScope(Dispatchers.IO).launch { exerciseDao.upsert(ExerciseListing(name = name)) } },
            ) {
                Text(text = "Create")
            }
        }
    }
}
