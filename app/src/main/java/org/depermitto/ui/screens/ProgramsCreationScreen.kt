package org.depermitto.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.times
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.depermitto.data.Day
import org.depermitto.data.ExerciseDao
import org.depermitto.data.Program
import org.depermitto.data.ProgramDao
import org.depermitto.presentation.ProgramCreationViewModel
import org.depermitto.ui.theme.paddingDp
import org.depermitto.ui.theme.notUnderlinedTextFieldColors
import org.depermitto.ui.theme.spacingDp

@Composable
fun ProgramsCreationScreen(
    viewModel: ProgramCreationViewModel,
    exerciseDao: ExerciseDao,
    programDao: ProgramDao,
    navController: NavController,
) {
    val scope = rememberCoroutineScope { Dispatchers.IO }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = paddingDp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacingDp)
        ) {
            item {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = viewModel.state.workoutName,
                    onValueChange = { viewModel.setWorkoutName(it) },
                    placeholder = { Text(text = "Workout Name") },
                    shape = MaterialTheme.shapes.medium,
                    colors = notUnderlinedTextFieldColors()
                )
            }

            itemsIndexed(viewModel.state.days) { i, day ->
                DayCreationScreen(
                    day = day, onDayChange = {
                        if (it != null) viewModel.setDayAt(i, it)
                        else viewModel.removeDay(day)
                    }, exerciseDao = exerciseDao
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
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(2 * paddingDp),
            onClick = {
                if (viewModel.state.workoutName.isBlank()) {
                    Toast.makeText(context, "Blank Program Name", Toast.LENGTH_SHORT).show()
                    return@FloatingActionButton
                }

                val program = Program(name = viewModel.state.workoutName, trainingWork = viewModel.state.days)
                scope.launch { programDao.upsert(program) }
                Toast.makeText(context, "Successfully Created", Toast.LENGTH_SHORT).show()

                viewModel.reset()
                navController.popBackStack(Screen.MainScreen.route, false)
            }) {
            Icon(Icons.Filled.Check, contentDescription = "Complete Program")
        }
    }
}