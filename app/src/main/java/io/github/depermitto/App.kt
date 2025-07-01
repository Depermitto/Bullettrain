package io.github.depermitto

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.depermitto.components.AnchoredFloatingActionButton
import io.github.depermitto.components.Ribbon
import io.github.depermitto.components.RibbonScaffold
import io.github.depermitto.data.Program
import io.github.depermitto.programs.Program
import io.github.depermitto.programs.ProgramCreation
import io.github.depermitto.programs.ProgramViewModel
import io.github.depermitto.screen.MainScreen
import io.github.depermitto.screen.Screen
import io.github.depermitto.screen.Screen.MainScreen.Tabs
import io.github.depermitto.settings.PersistentData
import io.github.depermitto.settings.Settings
import io.github.depermitto.settings.SettingsViewModel

@Composable
fun App(persistentData: PersistentData) = MaterialTheme {
    val programDao = persistentData.db.getProgramDao()
    val historyDao = persistentData.db.getHistoryDao()
    val exerciseDao = persistentData.db.getExerciseDao()

    val programViewModel = viewModel<ProgramViewModel>(factory = ProgramViewModel.Factory(Program(), programDao))
    val settingsViewModel = viewModel<SettingsViewModel>(factory = SettingsViewModel.Factory(persistentData))

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        composable(Screen.MainScreen.route) { navBackStackEntry ->
            val activeTab = Tabs.valueOf(navBackStackEntry.arguments?.getString("tab") ?: Tabs.Train.name)

            MainScreen(
                settingsViewModel = settingsViewModel,
                programDao = programDao,
                historyDao = historyDao,
                exerciseDao = exerciseDao,
                navController = navController,
                activeTab = activeTab
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
            val program by programDao.whereIdIs(
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
                Settings(settingsViewModel)
            }
        }
    }
}
