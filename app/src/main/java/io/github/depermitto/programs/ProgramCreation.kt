package io.github.depermitto.programs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import io.github.depermitto.home.Screen
import io.github.depermitto.components.AnchoredFloatingActionButton
import io.github.depermitto.database.ExerciseDao
import io.github.depermitto.database.ProgramDao
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.notUnderlinedTextFieldColors
import kotlinx.coroutines.launch

@Composable
fun ProgramCreation(
    programViewModel: ProgramViewModel,
    programDao: ProgramDao,
    exerciseDao: ExerciseDao,
    snackbarHostState: SnackbarHostState,
    navController: NavController,
) = Column(modifier = Modifier.fillMaxSize()) {
    val scope = rememberCoroutineScope()

    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ItemPadding),
        value = programViewModel.programName,
        onValueChange = { programViewModel.programName = it },
        maxLines = 1,
        placeholder = { Text(text = "Workout Name") },
        shape = MaterialTheme.shapes.medium,
        colors = notUnderlinedTextFieldColors()
    )
    Box(modifier = Modifier.weight(1f)) {
        ProgramScreen(programViewModel = programViewModel, exerciseDao = exerciseDao)
        AnchoredFloatingActionButton(text = { Text(text = "Complete Program") }, onClick = {
            if (programViewModel.programName.isBlank()) {
                scope.launch { snackbarHostState.showSnackbar("Blank Program Name") }
                return@AnchoredFloatingActionButton
            }

            programDao.insert(programViewModel.constructProgram())
            scope.launch { snackbarHostState.showSnackbar("Successfully Created") }
            navController.popBackStack(Screen.HomeScreen.route, false)
        })
    }
}