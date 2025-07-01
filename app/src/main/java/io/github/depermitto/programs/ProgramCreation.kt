package io.github.depermitto.programs

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import io.github.depermitto.Screen
import io.github.depermitto.components.AnchoredFloatingActionButton
import io.github.depermitto.database.ExerciseDao
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.notUnderlinedTextFieldColors

@Composable
fun ProgramCreation(
    programViewModel: ProgramViewModel,
    exerciseDao: ExerciseDao,
    navController: NavController,
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
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
            Program(programViewModel = programViewModel, exerciseDao = exerciseDao)
            AnchoredFloatingActionButton(text = { Text(text = "Complete Program") }, onClick = {
                if (programViewModel.programName.isBlank()) {
                    Toast.makeText(context, "Blank Program Name", Toast.LENGTH_SHORT).show()
                    return@AnchoredFloatingActionButton
                }

                programViewModel.upload()
                Toast.makeText(context, "Successfully Created", Toast.LENGTH_SHORT).show()
                navController.popBackStack(Screen.MainScreen.route, false)
            })
        }
    }
}