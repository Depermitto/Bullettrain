package io.github.depermitto.bullettrain.programs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.protos.SettingsProto.*
import io.github.depermitto.bullettrain.theme.Medium
import io.github.depermitto.bullettrain.theme.unlinedColors

@Composable
fun ProgramCreationScreen(
  modifier: Modifier = Modifier,
  programViewModel: ProgramViewModel,
  settings: Settings,
  navController: NavController,
) {
  Column(modifier = modifier.fillMaxSize()) {
    TextField(
      modifier = Modifier.fillMaxWidth().padding(horizontal = Dp.Medium),
      value = programViewModel.programName,
      onValueChange = { programViewModel.programName = it },
      maxLines = 1,
      singleLine = true,
      placeholder = { Text("Workout Name") },
      shape = MaterialTheme.shapes.medium,
      colors = TextFieldDefaults.unlinedColors(),
    )
    ProgramScreen(
      modifier = Modifier.padding(top = Dp.Medium),
      programViewModel = programViewModel,
      navController = navController,
    )
  }
}
