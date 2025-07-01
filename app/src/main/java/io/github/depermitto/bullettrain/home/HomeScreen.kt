package io.github.depermitto.bullettrain.home

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destination.Home.Tab
import io.github.depermitto.bullettrain.database.ExerciseDao
import io.github.depermitto.bullettrain.database.HistoryDao
import io.github.depermitto.bullettrain.database.ProgramDao
import io.github.depermitto.bullettrain.database.SettingsDao
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
    settingsDao: SettingsDao,
    navController: NavController,
) {
    when (homeViewModel.activeTab) {
        Tab.Exercises -> ExerciseTab(
            modifier = modifier, exerciseDao = exerciseDao, navController = navController
        )

        Tab.History -> HistoryTab(
            modifier = modifier,
            homeViewModel = homeViewModel,
            trainViewModel = trainViewModel,
            settingsDao = settingsDao,
            historyDao = historyDao,
            programDao = programDao,
            navController = navController
        )

        Tab.Train -> TrainTab(
            modifier = modifier, trainViewModel = trainViewModel, programDao = programDao, navController = navController
        )

        Tab.Programs -> ProgramsTab(
            modifier = modifier,
            programViewModel = programViewModel,
            programDao = programDao,
            navController = navController,
        )
    }
}