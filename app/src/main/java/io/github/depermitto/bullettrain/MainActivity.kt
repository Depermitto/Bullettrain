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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.depermitto.bullettrain.Destination.Home.Tab
import io.github.depermitto.bullettrain.components.DiscardConfirmationAlertDialog
import io.github.depermitto.bullettrain.components.HeaderWithBackButton
import io.github.depermitto.bullettrain.components.HeaderWithSettingsButton
import io.github.depermitto.bullettrain.components.Scaffold
import io.github.depermitto.bullettrain.database.BackgroundSlave
import io.github.depermitto.bullettrain.database.Database
import io.github.depermitto.bullettrain.database.Program
import io.github.depermitto.bullettrain.exercises.ExerciseScreen
import io.github.depermitto.bullettrain.home.HomeScreen
import io.github.depermitto.bullettrain.home.HomeViewModel
import io.github.depermitto.bullettrain.programs.DayScreen
import io.github.depermitto.bullettrain.programs.ProgramCreationScreen
import io.github.depermitto.bullettrain.programs.ProgramScreen
import io.github.depermitto.bullettrain.programs.ProgramViewModel
import io.github.depermitto.bullettrain.settings.SettingsScreen
import io.github.depermitto.bullettrain.theme.GymAppTheme
import io.github.depermitto.bullettrain.theme.ItemPadding
import io.github.depermitto.bullettrain.theme.ScaleTransitionDirection
import io.github.depermitto.bullettrain.theme.scaleIntoContainer
import io.github.depermitto.bullettrain.theme.scaleOutOfContainer
import io.github.depermitto.bullettrain.train.TrainViewModel
import io.github.depermitto.bullettrain.train.TrainingScreen
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.runBlocking

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

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(modifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            focusManager.clearFocus()
        })
    }, snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { paddingValues ->
        var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
        var showFinishDialog by rememberSaveable { mutableStateOf(false) }
        NavHost(modifier = Modifier.padding(paddingValues),
            navController = navController,
            startDestination = if (runBlocking { trainViewModel.restoreWorkout() }) {
                Destination.Training
            } else {
                Destination.Home(Tab.Exercises)
            },
            enterTransition = { scaleIntoContainer() },
            exitTransition = { scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS) },
            popEnterTransition = { scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS) },
            popExitTransition = { scaleOutOfContainer() }) {
            composable<Destination.Home> { navBackStackEntry ->
                val homeViewModel = viewModel<HomeViewModel>(
                    factory = HomeViewModel.Factory(tab = navBackStackEntry.toRoute<Destination.Home>().tab)
                )

                Scaffold(ribbon = {
                    if (homeViewModel.activeTab == Tab.Train) HeaderWithSettingsButton(
                        navController = navController, title = "Home"
                    )
                }) {
                    HomeScreen(
                        homeViewModel = homeViewModel,
                        trainViewModel = trainViewModel,
                        programViewModel = programViewModel,
                        exerciseDao = db.exerciseDao,
                        programDao = db.programDao,
                        historyDao = db.historyDao,
                        settingsDao = db.settingsDao,
                        navController = navController
                    )
                }
            }

            composable<Destination.Training> {
                if (!trainViewModel.isWorkoutRunning()) return@composable

                Scaffold(ribbon = {
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
                        navController = navController,
                        snackbarHostState = snackbarHostState
                    )
                }

                if (showDiscardDialog) DiscardConfirmationAlertDialog(onDismissRequest = { showDiscardDialog = false },
                    text = if (trainViewModel.isWorkoutEditing()) {
                        "Do you definitely want to discard changes made during editing?"
                    } else {
                        "All sets will be lost forever. Do you definitely want to discard the workout?"
                    },
                    onConfirm = { trainViewModel.cancelWorkout(); showDiscardDialog = false })

                if (showFinishDialog) AlertDialog(text = { Text("Do you truly want to conclude the workout?") },
                    onDismissRequest = { showFinishDialog = false },
                    dismissButton = {
                        TextButton(onClick = { showFinishDialog = false }) { Text("No, One More Set \uD83D\uDCAA") }
                    },
                    confirmButton = {
                        TextButton(onClick = { trainViewModel.completeWorkout(); showFinishDialog = false }) { Text("Conclude") }
                    })
            }

            composable<Destination.ProgramCreation> {
                Scaffold(ribbon = {
                    HeaderWithBackButton(
                        navController = navController,
                        title = if (programViewModel.programName.isBlank()) "New Program" else programViewModel.programName,
                        topEndContent = {
                            TextButton(onClick = {
                                val program = programViewModel.constructProgram()
                                if (program.name.isBlank()) {
                                    BackgroundSlave.enqueue {
                                        snackbarHostState.showSnackbar("Blank Program Name", withDismissAction = true)
                                    }
                                    return@TextButton
                                }

                                navController.navigateUp()
                                db.programDao.insert(program)
                                programViewModel.clear()
                            }) {
                                Icon(
                                    modifier = Modifier.size(ButtonDefaults.IconSize),
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Complete Program Creation"
                                )
                                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                                Text("Complete")
                            }
                        },
                    )
                }) {
                    ProgramCreationScreen(
                        programViewModel = programViewModel, navController = navController
                    )
                }

                if (showDiscardDialog) DiscardConfirmationAlertDialog(onDismissRequest = { showDiscardDialog = false },
                    text = "Do you want to discard ${programViewModel.programName.ifBlank { "your new creation" }}?",
                    onConfirm = { navController.navigateUp(); programViewModel.clear() })

                BackHandler(enabled = programViewModel.hasContent(ignoreDay1 = false)) { showDiscardDialog = true }
            }

            composable<Destination.Day> { navBackStackEntry ->
                val dayIndex = navBackStackEntry.toRoute<Destination.Day>().dayIndex
                val day = programViewModel.getDay(dayIndex)

                Scaffold(ribbon = { HeaderWithBackButton(navController = navController, title = day.name) }) {
                    DayScreen(
                        programViewModel = programViewModel,
                        exerciseDao = db.exerciseDao,
                        dayIndex = dayIndex,
                        navController = navController,
                        snackbarHostState = snackbarHostState
                    )
                }
            }

            composable<Destination.Program> { navBackStackEntry ->
                val program by db.programDao.where(navBackStackEntry.toRoute<Destination.Program>().programId)
                    .collectAsStateWithLifecycle(null)
                program?.let { program ->
                    programViewModel = viewModel(factory = ProgramViewModel.Factory(program))

                    Scaffold(ribbon = {
                        HeaderWithBackButton(navController = navController,
                            title = programViewModel.programName,
                            topEndContent = {
                                if (!programViewModel.areDaysEqual(program)) TextButton(onClick = {
                                    db.programDao.update(programViewModel.constructProgram())
                                    navController.popBackStack()
                                }) {
                                    Icon(
                                        modifier = Modifier.size(ButtonDefaults.IconSize),
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Complete Program Edit"
                                    )
                                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                                    Text("Finish Edit")
                                }
                            })
                    }) {
                        ProgramScreen(programViewModel = programViewModel, navController = navController)
                    }

                    if (showDiscardDialog) DiscardConfirmationAlertDialog(onDismissRequest = { showDiscardDialog = false },
                        text = "Do you want to discard changes made to ${programViewModel.programName}?",
                        onConfirm = { navController.navigateUp() })

                    BackHandler(enabled = !programViewModel.areDaysEqual(program)) { showDiscardDialog = true }
                }
            }

            composable<Destination.Exercise> { navBackStackEntry ->
                val exercise by db.exerciseDao.where(navBackStackEntry.toRoute<Destination.Exercise>().exerciseId)
                    .collectAsStateWithLifecycle(null)
                exercise?.let { exercise ->
                    Scaffold(ribbon = { HeaderWithBackButton(navController = navController, title = exercise.name) }) {
                        ExerciseScreen(
                            exerciseDao = db.exerciseDao,
                            historyDao = db.historyDao,
                            settingsDao = db.settingsDao,
                            exercise = exercise
                        )
                    }
                }
            }

            composable<Destination.Settings> {
                Scaffold(ribbon = { HeaderWithBackButton(navController = navController, title = "Settings") }) {
                    SettingsScreen(db = db, snackbarHostState = snackbarHostState)
                }
            }
        }
    }
}
