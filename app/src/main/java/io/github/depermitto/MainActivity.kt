package io.github.depermitto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.depermitto.Screen.*
import io.github.depermitto.Screen.MainScreen
import io.github.depermitto.Screen.MainScreen.Tabs
import io.github.depermitto.Screen.TrainingScreen
import io.github.depermitto.components.AnchoredFloatingActionButton
import io.github.depermitto.components.Ribbon
import io.github.depermitto.components.RibbonScaffold
import io.github.depermitto.database.BackgroundSlave
import io.github.depermitto.database.Database
import io.github.depermitto.database.Program
import io.github.depermitto.programs.ProgramCreation
import io.github.depermitto.programs.ProgramScreen
import io.github.depermitto.programs.ProgramViewModel
import io.github.depermitto.settings.SettingsScreen
import io.github.depermitto.theme.GymAppTheme
import io.github.depermitto.train.TrainViewModel
import io.github.depermitto.train.TrainingScreen
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FileKit.init(this)

        setContent {
            GymAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    App(Database(application.filesDir))
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        BackgroundSlave.waitForAll()
    }

    override fun onDestroy() {
        super.onDestroy()
        BackgroundSlave.quit()
    }
}

// TODO P3 make tests and benchmarks, mostly for backend probably
@Composable
fun App(db: Database) = MaterialTheme {
    val navController = rememberNavController()

    val programViewModel = viewModel<ProgramViewModel>(factory = ProgramViewModel.Factory(Program(), db.programDao))
    val trainViewModel =
        viewModel<TrainViewModel>(factory = TrainViewModel.Factory(db.historyDao, db.programDao, navController))

    NavHost(
        navController = navController,
        startDestination = if (runBlocking { trainViewModel.restoreWorkout() }) TrainingScreen.route else MainScreen.route
    ) {
        composable(MainScreen.route) { navBackStackEntry ->
            val activeTab = Tabs.valueOf(navBackStackEntry.arguments?.getString("tab") ?: Tabs.Programs.name)

            MainScreen(
                trainViewModel = trainViewModel,
                settingsDao = db.settingsDao,
                programDao = db.programDao,
                historyDao = db.historyDao,
                navController = navController,
                activeTab = activeTab
            )
        }

        composable(TrainingScreen.route) {
            TrainingScreen(
                trainViewModel = trainViewModel,
                settingsDao = db.settingsDao,
                exerciseDao = db.exerciseDao,
            )
        }

        composable(ProgramCreationScreen.route) {
            ProgramCreation(
                programViewModel = programViewModel,
                programDao = db.programDao,
                exerciseDao = db.exerciseDao,
                navController = navController
            )
        }

        composable(ProgramScreen.route) { navBackStackEntry ->
            val programId = (navBackStackEntry.arguments?.getString("programId") ?: return@composable).toInt()

            val program = runBlocking { db.programDao.where(programId).firstOrNull() }
            if (program != null) {
                val programViewModel =
                    viewModel<ProgramViewModel>(factory = ProgramViewModel.Factory(program, db.programDao))

                RibbonScaffold(ribbon = { Ribbon(navController, title = programViewModel.programName) }) {
                    ProgramScreen(programViewModel, exerciseDao = db.exerciseDao)
                    if (programViewModel.days.toList() != program.days.toList()) {
                        AnchoredFloatingActionButton(text = { Text("Finish Edit") }, onClick = {
                            db.programDao.update(programViewModel.constructProgram())
                            navController.popBackStack(MainScreen.route, inclusive = false)
                        })
                    }
                }
            }
        }

        composable(SettingsScreen.route) {
            RibbonScaffold(ribbon = { Ribbon(navController, settingsGear = false, title = "Settings") }) {
                SettingsScreen(db = db)
            }
        }
    }
}
