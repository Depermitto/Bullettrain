package org.depermitto.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.depermitto.database.Day
import org.depermitto.database.ExerciseDao
import org.depermitto.database.Program
import org.depermitto.database.ProgramDao
import org.depermitto.presentation.ProgramCreationViewModel
import org.depermitto.ui.DayCreation
import org.depermitto.ui.theme.horizontalDp
import org.depermitto.ui.theme.notUnderlinedTextFieldColors
import org.depermitto.ui.theme.spacingDp

@Composable
fun ProgramsCreationScreen(
    viewModel: ProgramCreationViewModel,
    exerciseDao: ExerciseDao,
    programDao: ProgramDao,
    navController: NavController,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalDp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacingDp)
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = viewModel.state.workoutName,
            onValueChange = { viewModel.setWorkoutName(it) },
            placeholder = { Text(text = "Workout Name") },
            shape = MaterialTheme.shapes.medium,
            colors = notUnderlinedTextFieldColors()
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacingDp)
        ) {
            items(viewModel.state.days) { day ->
                DayCreation(
                    day = day, onDayChanged = {
                        if (it != null) viewModel.set(day, it)
                        else viewModel.removeDay(day)
                    },
                    exerciseDao = exerciseDao
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
            if (viewModel.state.workoutName.isBlank()) {
                Toast.makeText(context, "Blank Program Name", Toast.LENGTH_SHORT).show()
                return@Button
            }

            val program = Program(name = viewModel.state.workoutName, trainingWork = viewModel.state.days)
            CoroutineScope(Dispatchers.IO).launch {
                programDao.upsert(program)
            }
            viewModel.reset()

            navController.popBackStack(Screen.MainScreen.route, false)
            Toast.makeText(context, "Successfully Created", Toast.LENGTH_SHORT).show()
        }) {
            Text(text = "Complete Program")
        }
    }
}