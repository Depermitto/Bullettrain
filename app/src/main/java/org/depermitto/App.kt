package org.depermitto

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.depermitto.database.GymDatabase
import org.depermitto.ui.Ribbon
import org.depermitto.ui.Scaffold
import org.depermitto.ui.screens.*
import java.io.File

const val DB_FILENAME = "firetent.sqlite"

@Composable
fun App(db: GymDatabase, dbFile: File, fallbackBytes: ByteArray) = MaterialTheme {
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.ProgramsCreationScreen.route) {
        composable(Screen.MainScreen.route) {
            Scaffold(ribbon = { Ribbon(navController = navController, backButton = false) }) {
                MainScreen(db.getProgramDao(), navController)
            }
        }

        composable(Screen.ProgramsCreationScreen.route) {
            Scaffold(ribbon = { Ribbon(navController = navController) }) {
                ProgramsCreationScreen(programDao = db.getProgramDao(), navController = navController)
            }
        }

        composable(Screen.ExercisesScreen.route) {
            Scaffold(ribbon = { Ribbon(navController = navController) }) {
                ExercisesScreen(exerciseDao = db.getExerciseDao(), navController = navController)
            }
        }

        composable(Screen.ExercisesCreationScreen.route) {
            Scaffold(ribbon = { Ribbon(navController = navController) }) {
                ExercisesCreationScreen(exerciseDao = db.getExerciseDao())
            }
        }

        composable(Screen.SettingsScreen.route) {
            Scaffold(ribbon = { Ribbon(navController = navController, settingsGear = false) }) {
                SettingsScreen(db = db, dbFile = dbFile, fallbackBytes = fallbackBytes, scope = scope)
            }
        }
    }
}
