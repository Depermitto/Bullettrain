package io.github.depermitto.bullettrain.programs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.components.AnchoredFloatingActionButton
import io.github.depermitto.bullettrain.database.BackgroundSlave
import io.github.depermitto.bullettrain.database.ProgramDao
import io.github.depermitto.bullettrain.theme.ItemPadding
import io.github.depermitto.bullettrain.theme.notUnderlinedTextFieldColors
import kotlinx.coroutines.launch

@Composable
fun ProgramCreationScreen(
    programViewModel: ProgramViewModel,
    programDao: ProgramDao,
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
        singleLine = true,
        placeholder = { Text(text = "Workout Name") },
        shape = MaterialTheme.shapes.medium,
        colors = notUnderlinedTextFieldColors()
    )
    Box(modifier = Modifier.weight(1f)) {
        ProgramDaysScreen(
            modifier = Modifier.padding(top = ItemPadding), programViewModel = programViewModel, navController = navController
        )
        AnchoredFloatingActionButton(text = { Text(text = "Complete Program") }, onClick = {
            val program = programViewModel.constructProgram()
            if (program.name.isBlank()) {
                scope.launch { snackbarHostState.showSnackbar("Blank Program Name") }
                return@AnchoredFloatingActionButton
            }

            navController.navigateUp()
            programDao.insert(program)
            programViewModel.clear()

            BackgroundSlave.enqueue {
                snackbarHostState.showSnackbar("Successfully Created ${program.name}", withDismissAction = true)
            }
        })
    }
}