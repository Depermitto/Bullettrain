package io.github.depermitto.bullettrain.programs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.components.AnchoredFloatingActionButton
import io.github.depermitto.bullettrain.database.Program
import io.github.depermitto.bullettrain.database.ProgramDao

@Composable
fun ProgramScreen(
    modifier: Modifier = Modifier,
    programViewModel: ProgramViewModel,
    programDao: ProgramDao,
    program: Program,
    navController: NavController
) = Box(modifier.fillMaxSize()) {
    ProgramDaysScreen(programViewModel = programViewModel, navController = navController)
    if (!programViewModel.areDaysEqual(program)) {
        AnchoredFloatingActionButton(text = { Text("Finish Edit") }, onClick = {
            programDao.update(programViewModel.constructProgram())
            navController.popBackStack()
        })
    }
}