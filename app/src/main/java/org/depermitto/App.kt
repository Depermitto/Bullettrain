package org.depermitto

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.depermitto.database.GymDatabase
import org.depermitto.presentation.ProgramCreationViewModel
import org.depermitto.ui.Ribbon
import org.depermitto.ui.RibbonScaffold
import org.depermitto.ui.screens.*
import java.io.File

const val DB_FILENAME = "firetent.sqlite"

@Composable
fun App(db: GymDatabase, dbFile: File, fallbackBytes: ByteArray) = MaterialTheme {
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    val programCreationViewModel: ProgramCreationViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.ProgramsCreationScreen.route) {
        composable(Screen.MainScreen.route) {
            RibbonScaffold(ribbon = { Ribbon(navController = navController, backButton = false) }) {
                MainScreen(db.getProgramDao(), navController)
            }
        }

        composable(Screen.ProgramsCreationScreen.route) {
            RibbonScaffold(ribbon = { Ribbon(navController = navController, title = "New Program") }) {
                ProgramsCreationScreen(
                    programCreationViewModel,
                    programDao = db.getProgramDao(),
                    exerciseDao = db.getExerciseDao(),
                    navController = navController
                )
            }
        }

        composable(Screen.ExercisesScreen.route) {
            RibbonScaffold(ribbon = { Ribbon(navController = navController, title = "Exercises") }) {
                ExercisesScreen(exerciseDao = db.getExerciseDao(), onSelection = { })
            }
        }

        composable(Screen.ExercisesCreationScreen.route) {
            RibbonScaffold(ribbon = { Ribbon(navController = navController) }) {
                ExercisesCreationScreen(exerciseDao = db.getExerciseDao())
            }
        }

        composable(Screen.SettingsScreen.route) {
            RibbonScaffold(ribbon = { Ribbon(navController = navController, settingsGear = false, title = "Settings") }) {
                SettingsScreen(db = db, dbFile = dbFile, fallbackBytes = fallbackBytes, scope = scope)
            }
        }
    }
}
