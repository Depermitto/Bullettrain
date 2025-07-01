package io.github.depermitto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.depermitto.components.AnchoredFloatingActionButton
import io.github.depermitto.components.Ribbon
import io.github.depermitto.components.RibbonScaffold
import io.github.depermitto.database.BackgroundSlave
import io.github.depermitto.database.Database
import io.github.depermitto.database.Program
import io.github.depermitto.home.HomeScreen
import io.github.depermitto.home.HomeViewModel
import io.github.depermitto.home.Screen.*
import io.github.depermitto.home.Screen.HomeScreen.Tabs
import io.github.depermitto.home.Screen.TrainingScreen
import io.github.depermitto.programs.ProgramCreation
import io.github.depermitto.programs.ProgramScreen
import io.github.depermitto.programs.ProgramViewModel
import io.github.depermitto.settings.SettingsScreen
import io.github.depermitto.theme.GymAppTheme
import io.github.depermitto.theme.ItemPadding
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

// TODO make tests and benchmarks, mostly for backend probably
@Composable
fun App(db: Database) = MaterialTheme {
    val navController = rememberNavController()

    val homeViewModel = viewModel<HomeViewModel>(factory = HomeViewModel.Factory(startingBar = Tabs.History))
    val programViewModel = viewModel<ProgramViewModel>(factory = ProgramViewModel.Factory(Program(), db.programDao))
    val trainViewModel = viewModel<TrainViewModel>(factory = TrainViewModel.Factory(db.historyDao, db.programDao, navController))

    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { paddingValues ->
        NavHost(
            modifier = Modifier.padding(paddingValues),
            navController = navController,
            startDestination = if (runBlocking { trainViewModel.restoreWorkout() }) TrainingScreen.route else HomeScreen.route
        ) {
            composable(HomeScreen.route) { navBackStackEntry ->
                navBackStackEntry.arguments?.getString("tab")?.let { homeViewModel.activeBar = Tabs.valueOf(it) }

                RibbonScaffold(ribbon = {
                    if (homeViewModel.activeBar != Tabs.History) {
                        Ribbon(navController = navController, title = "Home", backButton = false)
                    }
                }) {
                    HomeScreen(
                        homeViewModel = homeViewModel,
                        trainViewModel = trainViewModel,
                        settingsDao = db.settingsDao,
                        programDao = db.programDao,
                        historyDao = db.historyDao,
                        navController = navController
                    )
                }
            }

            composable(TrainingScreen.route) {
                if (!trainViewModel.isWorkoutRunning()) return@composable

                RibbonScaffold(ribbon = {
                    OutlinedCard(modifier = Modifier.padding(start = ItemPadding, end = ItemPadding, bottom = ItemPadding)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = ItemPadding)
                        ) {
                            TextButton(
                                modifier = Modifier.align(Alignment.CenterStart),
                                onClick = { trainViewModel.cancelWorkout() },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Cancel Workout",
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                                Text("Stop")
                            }
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = trainViewModel.elapsedSince(),
                                style = MaterialTheme.typography.titleMedium
                            )
                            TextButton(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                onClick = { trainViewModel.completeWorkout() },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                            ) { Text(text = "Finish") }
                        }
                    }
                }) {
                    TrainingScreen(
                        trainViewModel = trainViewModel,
                        settingsDao = db.settingsDao,
                        exerciseDao = db.exerciseDao,
                    )
                }
            }

            composable(ProgramCreationScreen.route) {
                RibbonScaffold(ribbon = {
                    Ribbon(
                        navController = navController,
                        title = programViewModel.programName.ifBlank { "New Program" },
                        settingsGear = false
                    )
                }) {
                    ProgramCreation(
                        programViewModel = programViewModel,
                        programDao = db.programDao,
                        exerciseDao = db.exerciseDao,
                        snackbarHostState = snackbarHostState,
                        navController = navController
                    )
                }
            }

            composable(ProgramScreen.route) { navBackStackEntry ->
                val programId = (navBackStackEntry.arguments?.getString("programId") ?: return@composable).toInt()
                val program = runBlocking { db.programDao.where(programId).firstOrNull() } ?: return@composable
                val programViewModel = viewModel<ProgramViewModel>(factory = ProgramViewModel.Factory(program, db.programDao))

                RibbonScaffold(ribbon = { Ribbon(navController, title = programViewModel.programName) }) {
                    ProgramScreen(programViewModel, exerciseDao = db.exerciseDao)
                    if (programViewModel.days.toList() != program.days.toList()) {
                        AnchoredFloatingActionButton(text = { Text("Finish Edit") }, onClick = {
                            db.programDao.update(programViewModel.constructProgram())
                            navController.popBackStack(HomeScreen.route, inclusive = false)
                        })
                    }
                }
            }

            composable(SettingsScreen.route) {
                RibbonScaffold(ribbon = { Ribbon(navController = navController, title = "Settings", settingsGear = false) }) {
                    SettingsScreen(db = db, snackbarHostState = snackbarHostState)
                }
            }
        }
    }
}