package org.depermitto

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.depermitto.database.GymDatabase
import org.depermitto.ui.BackButton
import org.depermitto.ui.QuickScaffoldWithTopBar
import org.depermitto.ui.SettingsGear
import org.depermitto.ui.screens.*
import java.io.File

const val DB_FILENAME = "firetent.sqlite"

@Composable
fun App(db: GymDatabase, dbFile: File, fallbackBytes: ByteArray) = MaterialTheme {
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val exerciseDao = db.getExerciseDao()

    NavHost(
        navController = navController, startDestination = Screen.MainScreen.route
    ) {
        composable(Screen.MainScreen.route) {
            QuickScaffoldWithTopBar(topBar = { SettingsGear(navController = navController) }) {
                MainScreen(db.getProgramDao(), navController)
            }
        }

        composable(Screen.ProgramsCreationScreen.route) {
            QuickScaffoldWithTopBar(navController) {
                ProgramsCreationScreen(programDao = db.getProgramDao())
            }
        }

        composable(Screen.ExercisesScreen.route) {
            QuickScaffoldWithTopBar(navController) {
                ExercisesScreen(exerciseDao = exerciseDao, navController = navController)
            }
        }

        composable(Screen.ExercisesCreationScreen.route) {
            QuickScaffoldWithTopBar(navController) {
                ExercisesCreationScreen(
                    exerciseDao = exerciseDao,
                )
            }
        }

        composable(Screen.SettingsScreen.route) {
            QuickScaffoldWithTopBar(topBar = { BackButton(navController = navController) }) {
                SettingsScreen(
                    db = db,
                    dbFile = dbFile,
                    fallbackBytes = fallbackBytes,
                    scope = scope,
                )
            }
        }
    }
}
