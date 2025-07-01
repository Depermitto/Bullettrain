package io.github.depermitto.screens.programs

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import io.github.depermitto.data.ExerciseDao
import io.github.depermitto.data.Program
import io.github.depermitto.data.ProgramDao
import io.github.depermitto.presentation.ProgramViewModel
import io.github.depermitto.screens.Screen
import io.github.depermitto.theme.notUnderlinedTextFieldColors
import io.github.depermitto.theme.paddingDp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ProgramsCreationScreen(
    viewModel: ProgramViewModel,
    exerciseDao: ExerciseDao,
    programDao: ProgramDao,
    navController: NavController,
) {
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = paddingDp),
            value = viewModel.state.name,
            onValueChange = { viewModel.setWorkoutName(it) },
            placeholder = { Text(text = "Workout Name") },
            shape = MaterialTheme.shapes.medium,
            colors = notUnderlinedTextFieldColors()
        )
        ProgramScreen(viewModel = viewModel, onFabClick = {
            if (viewModel.state.name.isBlank()) {
                Toast.makeText(context, "Blank Program Name", Toast.LENGTH_SHORT).show()
                return@ProgramScreen
            }

            val program = Program(name = viewModel.state.name, days = viewModel.state.days)
            scope.launch { programDao.upsert(program) }
            Toast.makeText(context, "Successfully Created", Toast.LENGTH_SHORT).show()

            viewModel.reset()
            navController.popBackStack(Screen.MainScreen.route, false)
        }, exerciseDao = exerciseDao, fabText = { "Complete Program" })
    }
}