package io.github.depermitto.bullettrain

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
import androidx.compose.material.icons.filled.Add
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.depermitto.bullettrain.components.AnchoredFloatingActionButton
import io.github.depermitto.bullettrain.components.Ribbon
import io.github.depermitto.bullettrain.components.RibbonScaffold
import io.github.depermitto.bullettrain.database.BackgroundSlave
import io.github.depermitto.bullettrain.database.Database
import io.github.depermitto.bullettrain.database.Day
import io.github.depermitto.bullettrain.database.Program
import io.github.depermitto.bullettrain.home.HomeScreen
import io.github.depermitto.bullettrain.home.HomeViewModel
import io.github.depermitto.bullettrain.programs.DayScreen
import io.github.depermitto.bullettrain.programs.ProgramCreation
import io.github.depermitto.bullettrain.programs.ProgramScreen
import io.github.depermitto.bullettrain.programs.ProgramViewModel
import io.github.depermitto.bullettrain.settings.SettingsScreen
import io.github.depermitto.bullettrain.theme.GymAppTheme
import io.github.depermitto.bullettrain.theme.ItemPadding
import io.github.depermitto.bullettrain.train.TrainViewModel
import io.github.depermitto.bullettrain.train.TrainingScreen
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import kotlin.reflect.typeOf

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FileKit.init(this)

        setContent {
            GymAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    App(Database(application.filesDir, applicationContext))
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

@Composable
fun App(db: Database) = MaterialTheme {
    val navController = rememberNavController()

    val homeViewModel = viewModel<HomeViewModel>(factory = HomeViewModel.Factory(startingBar = Screen.HomeScreen.Tabs.History))
    val newProgramViewModel = viewModel<ProgramViewModel>(factory = ProgramViewModel.Factory(Program(), db.programDao))
    val trainViewModel = viewModel<TrainViewModel>(factory = TrainViewModel.Factory(db.historyDao, db.programDao, navController))

    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { paddingValues ->
        NavHost(
            modifier = Modifier.padding(paddingValues),
            navController = navController,
            startDestination = if (runBlocking { trainViewModel.restoreWorkout() }) Screen.TrainingScreen.route else Screen.HomeScreen.route
        ) {
            composable(Screen.HomeScreen.route) { navBackStackEntry ->
                navBackStackEntry.arguments?.getString("tab")
                    ?.let { homeViewModel.activeBar = Screen.HomeScreen.Tabs.valueOf(it) }

                RibbonScaffold(ribbon = {
                    if (homeViewModel.activeBar != Screen.HomeScreen.Tabs.History) {
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

            composable(Screen.TrainingScreen.route) {
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

            composable(Screen.ProgramCreationScreen.route) {
                RibbonScaffold(ribbon = {
                    Ribbon(
                        navController = navController,
                        title = newProgramViewModel.programName.ifBlank { "New Program" },
                        settingsGear = false
                    )
                }) {
                    ProgramCreation(
                        programViewModel = newProgramViewModel,
                        programDao = db.programDao,
                        snackbarHostState = snackbarHostState,
                        navController = navController
                    )
                }
            }

            var programViewModel = newProgramViewModel

            composable<Program>(
                typeMap = mapOf(
                    typeOf<List<Day>>() to serializableType<List<Day>>(),
                    typeOf<Instant>() to serializableType<Instant>(),
                )
            ) { navBackStackEntry ->
                val program = navBackStackEntry.toRoute<Program>()

                RibbonScaffold(ribbon = { Ribbon(navController, title = programViewModel.programName, settingsGear = false) }) {
                    ProgramScreen(programViewModel, navController)
                    if (programViewModel.days.toList() != program.days.toList()) {
                        AnchoredFloatingActionButton(text = { Text("Finish Edit") }, onClick = {
                            db.programDao.update(programViewModel.constructProgram())
                            navController.popBackStack(Screen.HomeScreen.route, inclusive = false)
                        })
                    }
                }
            }

            composable<Day> { navBackStackEntry ->
                val day = navBackStackEntry.toRoute<Day>()

                RibbonScaffold(ribbon = { Ribbon(navController, title = day.name, settingsGear = false) }) {
                    DayScreen(programViewModel = programViewModel, dayIndex = programViewModel.days.indexOf(day))
                    AnchoredFloatingActionButton(text = { Text("Add Exercise") },
                        icon = { Icon(Icons.Filled.Add, null) },
                        onClick = { TODO() })
                }
            }

            composable(Screen.SettingsScreen.route) {
                RibbonScaffold(ribbon = { Ribbon(navController = navController, title = "Settings", settingsGear = false) }) {
                    SettingsScreen(db = db, snackbarHostState = snackbarHostState)
                }
            }
        }
    }
}

inline fun <reified T : Any> serializableType(
    isNullableAllowed: Boolean = false,
    json: Json = Json,
) = object : NavType<T>(isNullableAllowed = isNullableAllowed) {
    override fun get(bundle: Bundle, key: String) =
        bundle.getString(key)?.let<String, T>(json::decodeFromString)

    override fun parseValue(value: String): T = json.decodeFromString(value)

    override fun serializeAsValue(value: T): String = json.encodeToString(value)

    override fun put(bundle: Bundle, key: String, value: T) {
        bundle.putString(key, json.encodeToString(value))
    }
}