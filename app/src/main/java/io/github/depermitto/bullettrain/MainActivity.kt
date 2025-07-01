package io.github.depermitto.bullettrain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.depermitto.bullettrain.components.DiscardConfirmationAlertDialog
import io.github.depermitto.bullettrain.components.Ribbon
import io.github.depermitto.bullettrain.components.RibbonScaffold
import io.github.depermitto.bullettrain.database.BackgroundSlave
import io.github.depermitto.bullettrain.database.Database
import io.github.depermitto.bullettrain.database.Day
import io.github.depermitto.bullettrain.database.Program
import io.github.depermitto.bullettrain.home.HomeScreen
import io.github.depermitto.bullettrain.home.HomeViewModel
import io.github.depermitto.bullettrain.programs.DayExercisesScreen
import io.github.depermitto.bullettrain.programs.ProgramCreationScreen
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

    val trainViewModel = viewModel<TrainViewModel>(factory = TrainViewModel.Factory(db.historyDao, db.programDao, navController))
    var programViewModel = viewModel<ProgramViewModel>(factory = ProgramViewModel.Factory(Program()))

    val localFocusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(modifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            localFocusManager.clearFocus()
        })
    }, snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { paddingValues ->
        var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
        var showFinishDialog by rememberSaveable { mutableStateOf(false) }
        NavHost(
            modifier = Modifier.padding(paddingValues),
            navController = navController,
            startDestination = if (runBlocking { trainViewModel.restoreWorkout() }) {
                Destinations.Training
            } else {
                Destinations.Home(Destinations.Home.Tabs.Programs)
            }
        ) {
            composable<Destinations.Home> { navBackStackEntry ->
                val homeViewModel = viewModel<HomeViewModel>(
                    factory = HomeViewModel.Factory(tab = navBackStackEntry.toRoute<Destinations.Home>().tab)
                )

                RibbonScaffold(ribbon = {
                    if (homeViewModel.activeBar != Destinations.Home.Tabs.History) {
                        Ribbon(navController = navController, title = "Home", backButton = false)
                    }
                }) {
                    HomeScreen(
                        homeViewModel = homeViewModel,
                        trainViewModel = trainViewModel,
                        programViewModel = programViewModel,
                        settingsDao = db.settingsDao,
                        programDao = db.programDao,
                        historyDao = db.historyDao,
                        navController = navController
                    )
                }
            }

            composable<Destinations.Training> {
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
                                onClick = { showDiscardDialog = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Cancel Workout",
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                                Text("Discard")
                            }
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = trainViewModel.elapsedSince(),
                                style = MaterialTheme.typography.titleMedium
                            )
                            TextButton(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                onClick = { showFinishDialog = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text(text = "Finish")
                            }
                        }
                    }
                }) {
                    TrainingScreen(
                        trainViewModel = trainViewModel,
                        settingsDao = db.settingsDao,
                        exerciseDao = db.exerciseDao,
                    )
                }

                if (showDiscardDialog) DiscardConfirmationAlertDialog(onDismissRequest = { showDiscardDialog = false },
                    text = "All sets will be lost forever. Do you definitely want to discard the workout?",
                    onConfirm = { trainViewModel.cancelWorkout() })

                if (showFinishDialog) AlertDialog(text = { Text("Do you truly want to conclude the workout?") },
                    onDismissRequest = { showFinishDialog = false },
                    dismissButton = { TextButton(onClick = { showFinishDialog = false }) { Text("One More Set \uD83D\uDCAA") } },
                    confirmButton = { TextButton(onClick = { trainViewModel.completeWorkout() }) { Text("Finish") } })
            }

            composable<Destinations.ProgramCreation> {
                RibbonScaffold(ribbon = {
                    Ribbon(
                        title = programViewModel.programName.ifBlank { "New Program" },
                        settingsGear = false,
                        navController = navController
                    )
                }) {
                    ProgramCreationScreen(
                        programViewModel = programViewModel,
                        programDao = db.programDao,
                        snackbarHostState = snackbarHostState,
                        navController = navController
                    )
                }

                if (showDiscardDialog) DiscardConfirmationAlertDialog(onDismissRequest = { showDiscardDialog = false },
                    text = "Do you want to discard ${programViewModel.programName.ifBlank { "new program" }}?",
                    onConfirm = { navController.navigateUp(); programViewModel.clear() })

                if (!programViewModel.isEmpty() && programViewModel.getDays().toList() != listOf(Day())) {
                    BackHandler { showDiscardDialog = true }
                }
            }

            composable<Destinations.Program>(typeMap = mapOf(typeOf<Program>() to serializableType<Program>())) { navBackStackEntry ->
                val program = navBackStackEntry.toRoute<Destinations.Program>().program
                programViewModel = viewModel(factory = ProgramViewModel.Factory(program))

                RibbonScaffold(ribbon = { Ribbon(navController, title = programViewModel.programName, settingsGear = false) }) {
                    ProgramScreen(
                        programViewModel = programViewModel,
                        programDao = db.programDao,
                        program = program,
                        navController = navController
                    )
                }

                if (showDiscardDialog) DiscardConfirmationAlertDialog(onDismissRequest = { showDiscardDialog = false },
                    text = "Do you want to discard changes made to ${programViewModel.programName}?",
                    onConfirm = { navController.navigateUp() })

                if (!programViewModel.isEqual(program)) BackHandler { showDiscardDialog = true }
            }

            composable<Destinations.Day> { navBackStackEntry ->
                val dayIndex = navBackStackEntry.toRoute<Destinations.Day>().dayIndex
                val day = programViewModel.getDay(dayIndex)

                RibbonScaffold(ribbon = { Ribbon(navController, title = day.name, settingsGear = false) }) {
                    DayExercisesScreen(
                        programViewModel = programViewModel,
                        exerciseDao = db.exerciseDao,
                        dayIndex = dayIndex,
                        snackbarHostState = snackbarHostState
                    )
                }
            }

            composable<Destinations.Settings> {
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
    override fun get(bundle: Bundle, key: String) = bundle.getString(key)?.let<String, T>(json::decodeFromString)

    override fun parseValue(value: String): T = json.decodeFromString(value)

    override fun serializeAsValue(value: T): String = json.encodeToString(value)

    override fun put(bundle: Bundle, key: String, value: T) {
        bundle.putString(key, json.encodeToString(value))
    }
}