package io.github.depermitto.bullettrain.exercises

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destination
import io.github.depermitto.bullettrain.db.ExerciseDao
import io.github.depermitto.bullettrain.db.HistoryDao

@Composable
fun ExerciseTab(
  modifier: Modifier = Modifier,
  exerciseDao: ExerciseDao,
  historyDao: HistoryDao,
  navController: NavController,
) {
  ExercisesListScreen(
    modifier = modifier,
    exerciseDao = exerciseDao,
    historyDao = historyDao,
    filter = null,
  ) {
    navController.navigate(Destination.Exercise(descriptorId = it.id))
  }
}
