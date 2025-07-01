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
import io.github.depermitto.data.*
import io.github.depermitto.programs.Program
import io.github.depermitto.programs.ProgramCreation
import io.github.depermitto.programs.ProgramViewModel
import io.github.depermitto.screen.MainScreen
import io.github.depermitto.screen.Screen
import io.github.depermitto.settings.Settings
import io.github.depermitto.settings.SettingsViewModel
import io.github.depermitto.train.TrainViewModel
import java.io.File

@Composable
fun App(db: GymDatabase, dbFile: File, fallbackBytes: ByteArray, settingsFile: File) = MaterialTheme {
    val navController = rememberNavController()

    val exerciseDao = db.getExerciseDao()
    val programDao = db.getProgramDao()

    val premadeTrainingDay = Day(
        name = "Arms", exercises = listOf(
            Exercise(name = "Bench", sets = listOf(ExerciseSet(ExerciseTarget.of(ExerciseTargetCategory.Reps)))),
            Exercise(name = "Squat")
        )
    )

    val globalProgramVM = viewModel<ProgramViewModel>(factory = ProgramViewModel.Factory(Program(), programDao))
    val globalTrainVM = viewModel<TrainViewModel>(factory = TrainViewModel.Factory(premadeTrainingDay))
    val globalSettingsVM =
        viewModel<SettingsViewModel>(factory = SettingsViewModel.Factory(db, dbFile, fallbackBytes, settingsFile))

    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        composable(Screen.MainScreen.route) {
            MainScreen(
                trainViewModel = globalTrainVM,
                settingsViewModel = globalSettingsVM,
                programDao = programDao,
                exerciseDao = exerciseDao,
                navController = navController
            )
        }

        composable(Screen.ProgramCreationScreen.route) {
            RibbonScaffold(ribbon = { Ribbon(navController = navController, title = "New Program") }) {
                ProgramCreation(
                    programViewModel = globalProgramVM, exerciseDao = exerciseDao, navController = navController
                )
            }
        }

        composable(Screen.ProgramScreen.route) { navBackStackEntry ->
            val program by programDao.whereId(
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
                Settings(globalSettingsVM)
            }
        }
    }
}
