package io.github.depermitto

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import io.github.depermitto.presentation.ProgramViewModelFactory
import io.github.depermitto.presentation.TrainViewModel
import io.github.depermitto.presentation.TrainViewModelFactory
import io.github.depermitto.screens.MainScreen
import io.github.depermitto.screens.Screen
import io.github.depermitto.screens.SettingsScreen
import io.github.depermitto.screens.programs.ProgramCreationScreen
import io.github.depermitto.screens.programs.ProgramScreen
import kotlinx.coroutines.launch
import java.io.File

const val DB_FILENAME = "firetent.sqlite"

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun App(db: GymDatabase, dbFile: File, fallbackBytes: ByteArray) = MaterialTheme {
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    val exerciseDao = db.getExerciseDao()
    val programDao = db.getProgramDao()

    val programViewModel = viewModel<ProgramViewModel>()
    val trainViewModel = viewModel<TrainViewModel>(factory = TrainViewModelFactory(Day()))

    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        composable(Screen.MainScreen.route) {
            MainScreen(
                trainViewModel = trainViewModel,
                programDao = programDao,
                exerciseDao = exerciseDao,
                navController = navController
            )
        }

        composable(Screen.ProgramCreationScreen.route) {
            RibbonScaffold(ribbon = { Ribbon(navController = navController, title = "New Program") }) {
                ProgramCreationScreen(
                    programViewModel, programDao = programDao, exerciseDao = exerciseDao, navController = navController
                )
            }
        }

        composable(Screen.ProgramScreen.route) { navBackStackEntry ->
            val programId = navBackStackEntry.arguments?.getString("programId") ?: return@composable
            val programMaybe: Program? by programDao.whereId(id = programId.toLong())
                .collectAsStateWithLifecycle(initialValue = null)

            programMaybe?.let {
                val program = viewModel<ProgramViewModel>(factory = ProgramViewModelFactory(it))

                RibbonScaffold(ribbon = { Ribbon(navController = navController, title = program.name) }) {
                    ProgramScreen(program, exerciseDao = exerciseDao)
                    if (program.days != it.days) {
                        AnchoredFloatingActionButton(text = { Text("Finish Edit") }, onClick = {
                            scope.launch { programDao.upsert(Program(name = program.name, days = program.days)) }
                            navController.popBackStack(Screen.MainScreen.route, inclusive = false)
                        })
                    }
                }
            }
        }

        composable(Screen.SettingsScreen.route) {
            RibbonScaffold(ribbon = {
                Ribbon(navController = navController, settingsGear = false, title = "Settings")
            }) {
                SettingsScreen(db = db, dbFile = dbFile, fallbackBytes = fallbackBytes, scope = scope)
            }
        }
    }
}
