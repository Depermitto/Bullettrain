package io.github.depermitto

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
import io.github.depermitto.components.Ribbon
import io.github.depermitto.components.RibbonScaffold
import io.github.depermitto.data.GymDatabase
import io.github.depermitto.data.Program
import io.github.depermitto.presentation.ProgramCreationViewModel
import io.github.depermitto.screens.MainScreen
import io.github.depermitto.screens.Screen
import io.github.depermitto.screens.SettingsScreen
import io.github.depermitto.screens.programs.ProgramOverviewScreen
import io.github.depermitto.screens.programs.ProgramsCreationScreen
import java.io.File

const val DB_FILENAME = "firetent.sqlite"

@Composable
fun App(db: GymDatabase, dbFile: File, fallbackBytes: ByteArray) = MaterialTheme {
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    val exerciseDao = db.getExerciseDao()
    val programDao = db.getProgramDao()

    val programCreationViewModel: ProgramCreationViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        composable(Screen.MainScreen.route) {
            RibbonScaffold(ribbon = { Ribbon(navController = navController, backButton = false) }) {
                MainScreen(programDao, navController)
            }
        }

        composable(Screen.ProgramsCreationScreen.route) {
            RibbonScaffold(ribbon = { Ribbon(navController = navController, title = "New Program") }) {
                ProgramsCreationScreen(
                    programCreationViewModel,
                    programDao = programDao,
                    exerciseDao = exerciseDao,
                    navController = navController
                )
            }
        }

        composable(Screen.ProgramOverviewScreen.route) { navBackStackEntry ->
            val programId = navBackStackEntry.arguments?.getString("programId") ?: return@composable
            val program: Program? by programDao.where(id = programId.toLong())
                .collectAsStateWithLifecycle(initialValue = null)

            program?.let {
                RibbonScaffold(ribbon = { Ribbon(navController = navController, title = it.name) }) {
                    ProgramOverviewScreen(program = it, exerciseDao = exerciseDao)
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
