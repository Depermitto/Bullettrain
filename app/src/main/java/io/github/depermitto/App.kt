package io.github.depermitto

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.depermitto.components.AnchoredFloatingActionButton
import io.github.depermitto.components.Ribbon
import io.github.depermitto.components.RibbonScaffold
import io.github.depermitto.data.entities.Program
import io.github.depermitto.Screen.MainScreen.Tabs
import io.github.depermitto.programs.Program
import io.github.depermitto.programs.ProgramCreation
import io.github.depermitto.programs.ProgramViewModel
import io.github.depermitto.settings.PersistentData
import io.github.depermitto.settings.SettingsScreen
import io.github.depermitto.settings.SettingsViewModel
import io.github.depermitto.train.TrainViewModel
import io.github.depermitto.train.TrainingScreen

@Composable
fun App(persistentData: PersistentData) = MaterialTheme {
    val programDao = persistentData.db.getProgramDao()
    val historyDao = persistentData.db.getHistoryDao()
    val exerciseDao = persistentData.db.getExerciseDao()

    val navController = rememberNavController()

    val programViewModel = viewModel<ProgramViewModel>(factory = ProgramViewModel.Factory(Program(), programDao))
    val settingsViewModel = viewModel<SettingsViewModel>(factory = SettingsViewModel.Factory(persistentData))
    val trainViewModel =
        viewModel<TrainViewModel>(factory = TrainViewModel.Factory(historyDao, programDao, navController))

    LaunchedEffect(Unit) {
        if (trainViewModel.restoreWorkout()) navController.navigate(Screen.TrainingScreen.route)
    }

    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        // HistoryTab, TrainTab and ProgramsTab
        composable(Screen.MainScreen.route) { navBackStackEntry ->
            val activeTab = Tabs.valueOf(navBackStackEntry.arguments?.getString("tab") ?: Tabs.History.name)

            MainScreen(
                trainViewModel = trainViewModel,
                settingsViewModel = settingsViewModel,
                programDao = programDao,
                historyDao = historyDao,
                navController = navController,
                activeTab = activeTab
            )
        }

        composable(Screen.TrainingScreen.route) {
            TrainingScreen(
                trainViewModel = trainViewModel,
                settingsViewModel = settingsViewModel,
                exerciseDao = exerciseDao,
            )
        }

        composable(Screen.ProgramCreationScreen.route) {
            RibbonScaffold(ribbon = { Ribbon(navController = navController, title = "New Program") }) {
                ProgramCreation(
                    programViewModel = programViewModel, exerciseDao = exerciseDao, navController = navController
                )
            }
        }

        composable(Screen.ProgramScreen.route) { navBackStackEntry ->
            val program by programDao.whereIdFlow(
                (navBackStackEntry.arguments?.getString("programId") ?: return@composable).toLong()
            ).collectAsStateWithLifecycle(initialValue = null)

            program?.let {
                val programViewModel = viewModel<ProgramViewModel>(factory = ProgramViewModel.Factory(it, programDao))

                RibbonScaffold(ribbon = { Ribbon(navController, title = programViewModel.programName) }) {
                    Program(programViewModel, exerciseDao = exerciseDao)
                    if (programViewModel.days.toList() != it.days.toList()) {
                        AnchoredFloatingActionButton(text = { Text("Finish Edit") }, onClick = {
                            programViewModel.upsert()
                            navController.popBackStack(Screen.MainScreen.route, inclusive = false)
                        })
                    }
                }
            }
        }

        composable(Screen.SettingsScreen.route) {
            RibbonScaffold(ribbon = { Ribbon(navController, settingsGear = false, title = "Settings") }) {
                SettingsScreen(settingsViewModel = settingsViewModel)
            }
        }
    }
}
