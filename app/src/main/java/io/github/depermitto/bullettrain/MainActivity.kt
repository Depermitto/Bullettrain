package io.github.depermitto.bullettrain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.depermitto.bullettrain.Destination.Home.Tab
import io.github.depermitto.bullettrain.components.DiscardConfirmationAlertDialog
import io.github.depermitto.bullettrain.components.DropdownButton
import io.github.depermitto.bullettrain.components.TextFieldAlertDialog
import io.github.depermitto.bullettrain.components.TopBarWithBackButton
import io.github.depermitto.bullettrain.components.TopBarWithSettingsButton
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
import io.github.depermitto.bullettrain.theme.BigSpacing
import io.github.depermitto.bullettrain.theme.BullettrainTheme
import io.github.depermitto.bullettrain.theme.RegularPadding
import io.github.depermitto.bullettrain.theme.ScaleTransitionDirection
import io.github.depermitto.bullettrain.theme.scaleIntoContainer
import io.github.depermitto.bullettrain.theme.scaleOutOfContainer
import io.github.depermitto.bullettrain.train.TrainViewModel
import io.github.depermitto.bullettrain.train.TrainingScreen
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        FileKit.init(this)

        setContent {
            BullettrainTheme(dynamicColor = false) {
                // this is for color flashing during navigating
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(db: Database) = MaterialTheme {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val homeViewModel = viewModel<HomeViewModel>(factory = HomeViewModel.Factory())
    val homeScreenPager = rememberPagerState(initialPage = Tab.Train.ordinal) { Tab.entries.size }
    val trainViewModel = viewModel<TrainViewModel>(factory = TrainViewModel.Factory(db.historyDao, db.programDao, navController))
    var programViewModel = viewModel<ProgramViewModel>(factory = ProgramViewModel.Factory(Program()))

    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    var showFinishDialog by rememberSaveable { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    NavHost(navController = navController,
        startDestination = if (trainViewModel.restoreWorkout()) Destination.Training else Destination.Home,
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
            })
        },
        enterTransition = { scaleIntoContainer() },
        exitTransition = { scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS) },
        popEnterTransition = { scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS) },
        popExitTransition = { scaleOutOfContainer() }) {
        composable<Destination.Home> {
            Scaffold(topBar = { TopBarWithSettingsButton(navController = navController, title = "Home") }, bottomBar = {
                NavigationBar(tonalElevation = 8.dp) {
                    Tab.entries.forEachIndexed { tabIndex, tab ->
                        val isSelected = homeScreenPager.currentPage == tabIndex
                        NavigationBarItem(selected = isSelected, onClick = {
                            if (!isSelected) {
                                scope.launch {
                                    homeScreenPager.animateScrollToPage(tabIndex)
                                }
                            }
                        }, icon = {
                            Icon(painter = painterResource(id = tab.icon), contentDescription = tab.name)
                        }, label = {
                            Text(text = tab.name)
                        }, alwaysShowLabel = false)
                    }
                }
            }) { paddingValues ->
                HomeScreen(
                    modifier = Modifier
                        .consumeWindowInsets(paddingValues)
                        .padding(paddingValues),
                    homeViewModel = homeViewModel,
                    trainViewModel = trainViewModel,
                    programViewModel = programViewModel,
                    exerciseDao = db.exerciseDao,
                    programDao = db.programDao,
                    historyDao = db.historyDao,
                    settingsDao = db.settingsDao,
                    pagerState = homeScreenPager,
                    navController = navController
                )
            }
        }

        composable<Destination.Training> {
            if (!trainViewModel.isWorkoutRunning()) return@composable

            Scaffold(topBar = {
                OutlinedCard(
                    modifier = Modifier
                        .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                        .padding(start = RegularPadding, end = RegularPadding, bottom = BigSpacing)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp, horizontal = 4.dp)
                    ) {
                        TextButton(
                            modifier = Modifier.align(Alignment.CenterStart),
                            onClick = { showDiscardDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        ) {
                            Icon(Icons.Filled.Close, "Cancel Workout", Modifier.size(ButtonDefaults.IconSize))
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Discard")
                        }
                        if (!trainViewModel.isWorkoutEditing()) Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = trainViewModel.elapsedSince(),
                            style = MaterialTheme.typography.titleMedium
                        )
                        TextButton(
                            modifier = Modifier.align(Alignment.CenterEnd), onClick = {
                                if (trainViewModel.isWorkoutEditing()) {
                                    trainViewModel.completeWorkout()
                                } else {
                                    showFinishDialog = true
                                }
                            }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Finish")
                        }
                    }
                }
            }, snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
                TrainingScreen(
                    modifier = Modifier
                        .consumeWindowInsets(paddingValues)
                        .padding(paddingValues),
                    trainViewModel = trainViewModel,
                    settingsDao = db.settingsDao,
                    exerciseDao = db.exerciseDao,
                    historyDao = db.historyDao,
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
            Scaffold(topBar = {
                TopBarWithBackButton(
                    navController = navController,
                    title = if (programViewModel.programName.isBlank()) "New Program" else programViewModel.programName,
                    topEndContent = {
                        TextButton(onClick = {
                            val program = programViewModel.getProgram()
                            if (program.name.isBlank()) {
                                BackgroundSlave.enqueue {
                                    snackbarHostState.showSnackbar("Blank Program Name", withDismissAction = true)
                                }
                                return@TextButton
                            }

                            navController.navigateUp()
                            db.programDao.insert(program)
                            programViewModel.revertToDefault()
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
            }, snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
                ProgramCreationScreen(
                    modifier = Modifier
                        .consumeWindowInsets(paddingValues)
                        .padding(paddingValues),
                    programViewModel = programViewModel,
                    navController = navController
                )
            }

            if (showDiscardDialog) DiscardConfirmationAlertDialog(onDismissRequest = { showDiscardDialog = false },
                text = "Do you want to discard ${programViewModel.programName.ifBlank { "your new creation" }}?",
                onConfirm = { navController.navigateUp(); programViewModel.revertToDefault() })

            BackHandler(enabled = programViewModel.hasContent(ignoreDay1 = false)) { showDiscardDialog = true }
        }

        composable<Destination.Day> { navBackStackEntry ->
            val dayIndex = navBackStackEntry.toRoute<Destination.Day>().dayIndex
            val day = programViewModel.getDay(dayIndex)

            Scaffold(topBar = { TopBarWithBackButton(navController = navController, title = day.name) },
                snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
                DayScreen(
                    modifier = Modifier
                        .consumeWindowInsets(paddingValues)
                        .padding(paddingValues),
                    programViewModel = programViewModel,
                    exerciseDao = db.exerciseDao,
                    historyDao = db.historyDao,
                    dayIndex = dayIndex,
                    navController = navController,
                    snackbarHostState = snackbarHostState
                )
            }
        }

        composable<Destination.Program> { navBackStackEntry ->
            val program = db.programDao.where(navBackStackEntry.toRoute<Destination.Program>().programId)
            programViewModel = viewModel(factory = ProgramViewModel.Factory(program))

            Scaffold(topBar = {
                TopBarWithBackButton(navController = navController, title = programViewModel.programName, topEndContent = {
                    if (programViewModel.hasChanged) TextButton(onClick = {
                        db.programDao.update(programViewModel.getProgram())
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
            }) { paddingValues ->
                ProgramScreen(
                    modifier = Modifier
                        .consumeWindowInsets(paddingValues)
                        .padding(paddingValues),
                    programViewModel = programViewModel,
                    navController = navController
                )
            }

            if (showDiscardDialog) DiscardConfirmationAlertDialog(onDismissRequest = { showDiscardDialog = false },
                text = "Do you want to discard changes made to ${programViewModel.programName}?",
                onConfirm = { navController.navigateUp() })

            BackHandler(enabled = programViewModel.hasChanged) { showDiscardDialog = true }
        }

        composable<Destination.Exercise> { navBackStackEntry ->
            val exerciseDescriptor = db.exerciseDao.where(navBackStackEntry.toRoute<Destination.Exercise>().exerciseId)
            var showDropdown by remember { mutableStateOf(false) }
            var showRenameDialog by remember { mutableStateOf(false) }
            Scaffold(topBar = {
                TopBarWithBackButton(navController = navController,
                    title = exerciseDescriptor.name.run { if (exerciseDescriptor.obsolete) "$this [Archived]" else this },
                    topEndContent = {
                        DropdownButton(showDropdown, { showDropdown = it }) {
                            DropdownMenuItem(text = { Text("Rename") },
                                leadingIcon = { Icon(Icons.Filled.Edit, "Rename Exercise") },
                                onClick = { showDropdown = false; showRenameDialog = true })
                            if (!exerciseDescriptor.obsolete) DropdownMenuItem(text = { Text("Delete") },
                                leadingIcon = { Icon(Icons.Filled.Delete, "Delete Exercise") },
                                onClick = { showDropdown = false; showDiscardDialog = true })
                        }
                    })
            }) { paddingValues ->
                ExerciseScreen(
                    modifier = Modifier
                        .consumeWindowInsets(paddingValues)
                        .padding(paddingValues),
                    historyDao = db.historyDao,
                    settingsDao = db.settingsDao,
                    exerciseDescriptor = exerciseDescriptor
                )
            }

            // This is a essentially copy from ExercisesListScreen.kt
            if (showRenameDialog) {
                var errorMessage by rememberSaveable { mutableStateOf("") }
                TextFieldAlertDialog(
                    onDismissRequest = { showRenameDialog = false },
                    startingText = exerciseDescriptor.name,
                    label = { Text("Exercise Name") },
                    dismissButton = { TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") } },
                    confirmButton = { name ->
                        TextButton(onClick = {
                            errorMessage = db.exerciseDao.validateName(name) ?: "".also {
                                showRenameDialog = false
                                db.exerciseDao.update(exerciseDescriptor.copy(name = name))
                            }
                        }) {
                            Text("Confirm")
                        }
                    },
                    errorMessage = errorMessage,
                    isError = errorMessage.isNotBlank()
                )
            }

            if (showDiscardDialog) DiscardConfirmationAlertDialog(onDismissRequest = { showDiscardDialog = false }, text = """
                Data associated with this exercise will not be lost. Even if you delete it, you can still perform the exercise if it appears in a program, and you can also rename it. However, you will not be able to include the exercise in new Programs.

                Do you really want to remove '${exerciseDescriptor.name}'?
            """.trimIndent(), onConfirm = { db.exerciseDao.delete(exerciseDescriptor); navController.navigateUp() })
        }

        composable<Destination.Settings> {
            Scaffold(topBar = { TopBarWithBackButton(navController = navController, title = "Settings") },
                snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
                SettingsScreen(
                    modifier = Modifier
                        .consumeWindowInsets(paddingValues)
                        .padding(paddingValues),
                    db = db,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}
