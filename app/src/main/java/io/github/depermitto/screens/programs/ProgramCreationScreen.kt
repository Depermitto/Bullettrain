package io.github.depermitto.screens.programs

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import io.github.depermitto.components.AnchoredFloatingActionButton
import io.github.depermitto.data.ExerciseDao
import io.github.depermitto.data.Program
import io.github.depermitto.data.ProgramDao
import io.github.depermitto.presentation.ProgramViewModel
import io.github.depermitto.screens.Screen
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.notUnderlinedTextFieldColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ProgramCreationScreen(
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
                .padding(horizontal = ItemPadding),
            value = viewModel.name,
            onValueChange = { viewModel.name = it },
            placeholder = { Text(text = "Workout Name") },
            shape = MaterialTheme.shapes.medium,
            colors = notUnderlinedTextFieldColors()
        )
        Box(modifier = Modifier.weight(1f)) {
            ProgramScreen(programViewModel = viewModel, exerciseDao = exerciseDao)
            AnchoredFloatingActionButton(text = { Text(text = "Complete Program") }, onClick = {
                if (viewModel.name.isBlank()) {
                    Toast.makeText(context, "Blank Program Name", Toast.LENGTH_SHORT).show()
                    return@AnchoredFloatingActionButton
                }

                scope.launch { programDao.upsert(Program(name = viewModel.name, days = viewModel.days)) }
                Toast.makeText(context, "Successfully Created", Toast.LENGTH_SHORT).show()

                viewModel.name = ""
                viewModel.days = listOf()

                navController.popBackStack(Screen.MainScreen.route, false)
            })
        }
    }
}