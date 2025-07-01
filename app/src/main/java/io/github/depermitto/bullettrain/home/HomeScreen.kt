package io.github.depermitto.bullettrain.home

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destination.Home.Tab
import io.github.depermitto.bullettrain.database.entities.*
import io.github.depermitto.bullettrain.exercises.ExerciseTab
import io.github.depermitto.bullettrain.history.HistoryTab
import io.github.depermitto.bullettrain.programs.ProgramViewModel
import io.github.depermitto.bullettrain.programs.ProgramsTab
import io.github.depermitto.bullettrain.train.TrainTab
import io.github.depermitto.bullettrain.train.TrainViewModel

@Composable
fun HomeScreen(
  modifier: Modifier = Modifier,
  homeViewModel: HomeViewModel,
  trainViewModel: TrainViewModel,
  programViewModel: ProgramViewModel,
  exerciseDao: ExerciseDao,
  programDao: ProgramDao,
  historyDao: HistoryDao,
  settings: Settings,
  pagerState: PagerState,
  navController: NavController,
) {
  HorizontalPager(modifier = modifier, state = pagerState) { page ->
    when (Tab.entries[page]) {
      Tab.Exercises ->
        ExerciseTab(
          exerciseDao = exerciseDao,
          historyDao = historyDao,
          navController = navController,
        )

      Tab.History ->
        HistoryTab(
          homeViewModel = homeViewModel,
          trainViewModel = trainViewModel,
          settings = settings,
          historyDao = historyDao,
          programDao = programDao,
          exerciseDao = exerciseDao,
          navController = navController,
        )

      Tab.Train ->
        TrainTab(
          trainViewModel = trainViewModel,
          programDao = programDao,
          exerciseDao = exerciseDao,
          settings = settings,
          navController = navController,
        )

      Tab.Programs ->
        ProgramsTab(
          programViewModel = programViewModel,
          programDao = programDao,
          settings = settings,
          navController = navController,
        )
    }
  }
}
