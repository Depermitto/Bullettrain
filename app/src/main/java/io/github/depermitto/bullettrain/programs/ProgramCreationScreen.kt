package io.github.depermitto.bullettrain.programs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.theme.ItemPadding
import io.github.depermitto.bullettrain.theme.notUnderlinedTextFieldColors

@Composable
fun ProgramCreationScreen(
    programViewModel: ProgramViewModel,
    navController: NavController,
) = Column(modifier = Modifier.fillMaxSize()) {
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
    ProgramScreen(
        modifier = Modifier.padding(top = ItemPadding),
        programViewModel = programViewModel,
        navController = navController
    )
}