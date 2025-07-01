package io.github.depermitto

import android.os.Build
import androidx.annotation.RequiresApi
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
import io.github.depermitto.data.Day
import io.github.depermitto.data.GymDatabase
import io.github.depermitto.data.Program
import io.github.depermitto.presentation.ProgramViewModel
import io.github.depermitto.presentation.SettingsViewModel
import io.github.depermitto.presentation.TrainViewModel
import io.github.depermitto.screens.MainScreen
import io.github.depermitto.screens.Screen
import io.github.depermitto.screens.SettingsScreen
import io.github.depermitto.screens.programs.ProgramCreationScreen
import io.github.depermitto.screens.programs.ProgramScreen
import java.io.File

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun App(db: GymDatabase, dbFile: File, fallbackBytes: ByteArray, settingsFile: File) = MaterialTheme {
    val navController = rememberNavController()

    val exerciseDao = db.getExerciseDao()
    val programDao = db.getProgramDao()

    val globalProgramVM = viewModel<ProgramViewModel>(factory = ProgramViewModel.Factory(Program(), programDao))
    val globalTrainVM = viewModel<TrainViewModel>(factory = TrainViewModel.Factory(Day()))
    val globalSettingsVM = viewModel<SettingsViewModel>(factory = SettingsViewModel.Factory(db, dbFile, fallbackBytes, settingsFile))

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
                ProgramCreationScreen(
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

                RibbonScaffold(ribbon = { Ribbon(navController = navController, title = programViewModel.name) }) {
                    ProgramScreen(programViewModel, exerciseDao = exerciseDao)
                    if (programViewModel.days != it.days) {
                        AnchoredFloatingActionButton(text = { Text("Finish Edit") }, onClick = {
                            programViewModel.upsert()
                            navController.popBackStack(Screen.MainScreen.route, inclusive = false)
                        })
                    }
                }
            }
        }

        composable(Screen.SettingsScreen.route) {
            RibbonScaffold(ribbon = {
                Ribbon(navController = navController, settingsGear = false, title = "Settings")
            }) { SettingsScreen(globalSettingsVM) }
        }
    }
}
