package org.depermitto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.depermitto.database.Day
import org.depermitto.database.ProgramDao
import org.depermitto.ui.Ribbon

// TODO Biggest road blocker as of this moment,
//  can't create any plans and the ui is clunky
@Composable
fun ProgramsCreationScreen(programDao: ProgramDao) = Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
) {
    var workoutName by remember { mutableStateOf("") }
    Text(text = "Workout Name")
    TextField(modifier = Modifier.fillMaxWidth(), value = workoutName, onValueChange = { workoutName = it })

    val days = remember { mutableStateListOf(Day("Day 1", listOf())) }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        days.forEachIndexed { i, day ->
            Column {
                Row(modifier = Modifier.padding(16.dp)) {
                    TextField(value = day.name, onValueChange = { days[i] = Day(it, day.exercises) })
                    IconButton(onClick = { days.removeAt(i) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = null)
                    }
                }

                Button(onClick = { days[i] = Day(day.name, listOf()) }) {
                    Text(text = "Add Exercise")
                }
            }
        }
    }

    Button(onClick = { days.add(Day("Day ${days.size + 1}", listOf())) }) {
        Text("Add day")
    }
}

