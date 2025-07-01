package org.depermitto.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.depermitto.database.Day
import org.depermitto.database.ProgramDao
import org.depermitto.ui.DayCreation

// TODO Biggest road blocker as of this moment,
//  can't create any plans and the ui is clunky
@Composable
fun ProgramsCreationScreen(programDao: ProgramDao, navController: NavController) = Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
) {
    var workoutName by remember { mutableStateOf("") }
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = workoutName,
        onValueChange = { workoutName = it },
        placeholder = { Text(text = "Workout Name") },
        shape = RoundedCornerShape(32.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
    )

    val days = remember { mutableStateListOf<Day>() }
    LazyColumn {
        itemsIndexed(days) { i, day ->
            DayCreation(
                day = day,
                onDayChanged = { if (it != null) days[i] = it else days.removeAt(i) },
                navController = navController
            )
        }
    }

    Button(modifier = Modifier.padding(top = 8.dp), onClick = { days.add(Day("Day ${days.size + 1}")) }) {
        Text("Add Day")
    }
}

