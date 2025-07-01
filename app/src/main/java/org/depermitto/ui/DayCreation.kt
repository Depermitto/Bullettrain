package org.depermitto.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.depermitto.database.Day
import org.depermitto.ui.screens.Screen
import org.depermitto.ui.theme.transparentTextFieldColors

@Composable
fun DayCreation(day: Day, onDayChanged: (Day?) -> Unit, navController: NavController) {
    OutlinedCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    modifier = Modifier.weight(1f),
                    value = day.name,
                    onValueChange = { onDayChanged(day.copy(name = it)) },
                    trailingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                    textStyle = MaterialTheme.typography.titleMedium,
                    colors = transparentTextFieldColors()
                )
                IconButton(onClick = { onDayChanged(null) }) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                }
            }

            // TODO Popup listing of exercises, set reps, sets and supersets
            OutlinedButton(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
                onClick = { navController.navigate(Screen.ExercisesScreen.route) }) {
                Text(text = "Add Exercise")
            }
        }
    }
}
