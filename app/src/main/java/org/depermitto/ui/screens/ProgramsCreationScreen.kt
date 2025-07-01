package org.depermitto.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.depermitto.database.Day
import org.depermitto.database.Program
import org.depermitto.database.ProgramDao
import org.depermitto.presentation.ProgramCreationViewModel
import org.depermitto.ui.DayCreation

@Composable
fun ProgramsCreationScreen(
    viewModel: ProgramCreationViewModel,
    programDao: ProgramDao,
    navController: NavController,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = viewModel.state.workoutName,
            onValueChange = { viewModel.setWorkoutName(it) },
            placeholder = { Text(text = "Workout Name") },
            shape = RoundedCornerShape(32.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
        )

        LazyColumn(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            items(viewModel.state.days) { day ->
                DayCreation(
                    day = day, onDayChanged = {
                        if (it != null) viewModel.set(day, it)
                        else viewModel.removeDay(day)
                    }, navController = navController
                )
            }
            item {
                Button(
                    onClick = { viewModel.addDay(Day("Day ${viewModel.state.days.size + 1}")) },
                    enabled = viewModel.state.days.size < 7,
                ) {
                    Text("Add Day")
                }
            }
        }

        val context = LocalContext.current
        Button(onClick = {
            val program = Program(name = viewModel.state.workoutName, trainingWork = viewModel.state.days)
            CoroutineScope(Dispatchers.IO).launch {
                programDao.upsert(program)
            }
            navController.popBackStack(Screen.MainScreen.route, false)
            Toast.makeText(context, "Successfully Created", Toast.LENGTH_SHORT).show()
        }) {
            Text(text = "Complete Program")
        }
    }
}