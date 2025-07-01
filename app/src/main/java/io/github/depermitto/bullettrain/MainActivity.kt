package io.github.depermitto.bullettrain

import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.depermitto.bullettrain.Destination.Home.Tab
import io.github.depermitto.bullettrain.components.ConfirmationAlertDialog
import io.github.depermitto.bullettrain.components.DropdownButton
import io.github.depermitto.bullettrain.components.TextFieldAlertDialog
import io.github.depermitto.bullettrain.components.TopBarWithBackButton
import io.github.depermitto.bullettrain.components.TopBarWithSettingsButton
import io.github.depermitto.bullettrain.db.Db
import io.github.depermitto.bullettrain.exercises.ExercisesSetsListings
import io.github.depermitto.bullettrain.home.HomeScreen
import io.github.depermitto.bullettrain.home.HomeViewModel
import io.github.depermitto.bullettrain.programs.DayScreen
import io.github.depermitto.bullettrain.programs.ProgramCreationScreen
import io.github.depermitto.bullettrain.programs.ProgramScreen
import io.github.depermitto.bullettrain.programs.ProgramViewModel
import io.github.depermitto.bullettrain.protos.ProgramsProto.*
import io.github.depermitto.bullettrain.settings.SettingsScreen
import io.github.depermitto.bullettrain.theme.BullettrainTheme
import io.github.depermitto.bullettrain.theme.Medium
import io.github.depermitto.bullettrain.theme.ScaleTransitionDirection
import io.github.depermitto.bullettrain.theme.scaleIntoContainer
import io.github.depermitto.bullettrain.theme.scaleOutOfContainer
import io.github.depermitto.bullettrain.train.TrainViewModel
import io.github.depermitto.bullettrain.train.TrainingScreen
import io.github.depermitto.bullettrain.util.DateFormatters
import io.github.depermitto.bullettrain.util.getDate
import io.github.depermitto.bullettrain.util.getLastCompletedSet
import io.github.depermitto.bullettrain.util.toLocalDate
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    FileKit.init(this)

    val db = Db(application.filesDir, applicationContext)
    setContent {
      val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)
      DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
          if (event == Lifecycle.Event.ON_STOP) {
            Log.i("Lifecycle", "Raised $event.")
            db.exportDatabase()
            Log.i("DB", "Saved app data to persistent storage.")
          }
        }
        lifecycleOwner.value.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.value.lifecycle.removeObserver(observer) }
      }

      val settings by db.settingsDao.get.collectAsStateWithLifecycle()
      BullettrainTheme(settings) {
        // this is for color flashing during navigating
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          App(db)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(db: Db) = MaterialTheme {
  // Global for every screen
  val navController = rememberNavController()
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  val homeViewModel = viewModel<HomeViewModel>(factory = HomeViewModel.Factory())
  val homeScreenPager = rememberPagerState(initialPage = Tab.Train.ordinal) { Tab.entries.size }
  val settings by db.settingsDao.get.collectAsStateWithLifecycle()

  val trainViewModel =
    viewModel<TrainViewModel>(
      factory = TrainViewModel.Factory(db.historyDao, db.programDao, navController)
    )
  var programViewModel =
    viewModel<ProgramViewModel>(factory = ProgramViewModel.Factory(Program.getDefaultInstance()))

  // Used across every NavHost.composable
  var showDiscardOrDeleteDialog by rememberSaveable { mutableStateOf(false) }
  var showFinishDialog by rememberSaveable { mutableStateOf(false) }

  val focusManager = LocalFocusManager.current
  NavHost(
    navController = navController,
    startDestination =
      if (trainViewModel.restoreWorkout()) Destination.Training else Destination.Home,
    modifier =
      Modifier.pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
    enterTransition = { scaleIntoContainer() },
    exitTransition = { scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS) },
    popEnterTransition = { scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS) },
    popExitTransition = { scaleOutOfContainer() },
  ) {
    composable<Destination.Home> {
      Scaffold(
        topBar = { TopBarWithSettingsButton(navController = navController, title = "Home") },
        bottomBar = {
          NavigationBar(tonalElevation = 8.dp) {
            Tab.entries.forEachIndexed { tabIndex, tab ->
              val isSelected = homeScreenPager.currentPage == tabIndex
              NavigationBarItem(
                selected = isSelected,
                onClick = {
                  if (!isSelected) {
                    scope.launch { homeScreenPager.animateScrollToPage(tabIndex) }
                  }
                },
                icon = {
                  Icon(painter = painterResource(id = tab.icon), contentDescription = tab.name)
                },
                label = { Text(text = tab.name) },
                alwaysShowLabel = false,
              )
            }
          }
        },
      ) { paddingValues ->
        HomeScreen(
          modifier = Modifier.consumeWindowInsets(paddingValues).padding(paddingValues),
          homeViewModel = homeViewModel,
          trainViewModel = trainViewModel,
          programViewModel = programViewModel,
          exerciseDao = db.exerciseDao,
          programDao = db.programDao,
          historyDao = db.historyDao,
          settings = settings,
          pagerState = homeScreenPager,
          navController = navController,
        )
      }
    }

    composable<Destination.Training> {
      if (!trainViewModel.isWorkoutRunning()) return@composable

      Scaffold(
        topBar = {
          OutlinedCard(
            modifier =
              Modifier.windowInsetsPadding(TopAppBarDefaults.windowInsets)
                .padding(start = Dp.Medium, end = Dp.Medium, bottom = Dp.Medium)
          ) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp, horizontal = 4.dp)) {
              TextButton(
                modifier = Modifier.align(Alignment.CenterStart),
                onClick = { showDiscardOrDeleteDialog = true },
                colors =
                  ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
              ) {
                Icon(Icons.Filled.Close, "Cancel Workout", Modifier.size(ButtonDefaults.IconSize))
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text("Discard")
              }
              Text(
                modifier = Modifier.align(Alignment.Center),
                text = trainViewModel.elapsed(),
                style = MaterialTheme.typography.titleMedium,
              )
              TextButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = { showFinishDialog = true },
                colors =
                  ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                  ),
              ) {
                Text("Finish")
              }
            }
          }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
      ) { paddingValues ->
        TrainingScreen(
          modifier = Modifier.consumeWindowInsets(paddingValues).padding(paddingValues),
          trainViewModel = trainViewModel,
          exerciseDao = db.exerciseDao,
          historyDao = db.historyDao,
          settings = settings,
          navController = navController,
          snackbarHostState = snackbarHostState,
        )
      }

      BackHandler { showDiscardOrDeleteDialog = true }

      if (showDiscardOrDeleteDialog)
        ConfirmationAlertDialog(
          onDismissRequest = { showDiscardOrDeleteDialog = false },
          text = "All sets will be lost forever. Do you definitely want to drop the workout?",
          onConfirm = {
            trainViewModel.cancelWorkout()
            showDiscardOrDeleteDialog = false
          },
        )

      if (showFinishDialog)
        AlertDialog(
          text = { Text("Do you truly want to conclude the workout?") },
          onDismissRequest = { showFinishDialog = false },
          dismissButton = {
            TextButton(onClick = { showFinishDialog = false }) {
              Text("No, One More Set \uD83D\uDCAA")
            }
          },
          confirmButton = {
            TextButton(
              onClick = {
                trainViewModel.completeWorkout()
                showFinishDialog = false
              }
            ) {
              Text("Conclude")
            }
          },
        )
    }

    composable<Destination.ProgramCreation> {
      Scaffold(
        topBar = {
          TopBarWithBackButton(
            navController = navController,
            title = programViewModel.programName.ifBlank { "New Program" },
            topEndContent = {
              TextButton(
                onClick = {
                  val program = programViewModel.getProgram()
                  if (program.name.isBlank()) {
                    scope.launch {
                      snackbarHostState.showSnackbar("Blank Program Name", withDismissAction = true)
                    }
                    return@TextButton
                  }

                  navController.navigateUp()
                  db.programDao.insert(program)
                  programViewModel.revertToDefault()
                }
              ) {
                Icon(
                  modifier = Modifier.size(ButtonDefaults.IconSize),
                  imageVector = Icons.Filled.Check,
                  contentDescription = "Finish Program Creation",
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text("Complete")
              }
            },
          )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
      ) { paddingValues ->
        ProgramCreationScreen(
          modifier = Modifier.consumeWindowInsets(paddingValues).padding(paddingValues),
          programViewModel = programViewModel,
          settings = settings,
          navController = navController,
        )
      }

      if (showDiscardOrDeleteDialog)
        ConfirmationAlertDialog(
          onDismissRequest = { showDiscardOrDeleteDialog = false },
          text =
            "Do you definitely want to discard ${programViewModel.programName.ifBlank { "your new creation" }}?",
          onConfirm = {
            navController.navigateUp()
            programViewModel.revertToDefault()
          },
        )

      BackHandler(enabled = programViewModel.hasContent(ignoreDay1 = false)) {
        showDiscardOrDeleteDialog = true
      }
    }

    composable<Destination.Day> { navBackStackEntry ->
      val dayIndex = navBackStackEntry.toRoute<Destination.Day>().dayIndex
      val day = programViewModel.getDay(dayIndex)

      Scaffold(
        topBar = { TopBarWithBackButton(navController = navController, title = day.name) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
      ) { paddingValues ->
        DayScreen(
          modifier = Modifier.consumeWindowInsets(paddingValues).padding(paddingValues),
          programViewModel = programViewModel,
          exerciseDao = db.exerciseDao,
          historyDao = db.historyDao,
          dayIndex = dayIndex,
          navController = navController,
          snackbarHostState = snackbarHostState,
          settings = settings,
        )
      }
    }

    composable<Destination.Program> { navBackStackEntry ->
      val program = db.programDao.where(navBackStackEntry.toRoute<Destination.Program>().programId)
      programViewModel = viewModel(factory = ProgramViewModel.Factory(program))
      val hasChanged = program.workoutsList != programViewModel.getDays()

      Scaffold(
        topBar = {
          TopBarWithBackButton(
            navController = navController,
            title = programViewModel.programName,
            topEndContent = {
              if (hasChanged)
                TextButton(
                  onClick = {
                    db.programDao.update(programViewModel.getProgram())
                    navController.popBackStack()
                  }
                ) {
                  Icon(
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Complete Program Edit",
                  )
                  Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                  Text("Finish Edit")
                }
            },
          )
        }
      ) { paddingValues ->
        ProgramScreen(
          modifier = Modifier.consumeWindowInsets(paddingValues).padding(paddingValues),
          programViewModel = programViewModel,
          settings = settings,
          navController = navController,
        )
      }

      if (showDiscardOrDeleteDialog)
        ConfirmationAlertDialog(
          onDismissRequest = { showDiscardOrDeleteDialog = false },
          text = "Do you want to discard changes made to ${programViewModel.programName}?",
          onConfirm = { navController.navigateUp() },
        )

      BackHandler(enabled = hasChanged) { showDiscardOrDeleteDialog = true }
    }

    composable<Destination.Exercise> { navBackStackEntry ->
      val exerciseDescriptor =
        db.exerciseDao.where(navBackStackEntry.toRoute<Destination.Exercise>().exerciseId)
      var showDropdown by remember { mutableStateOf(false) }
      var showRenameDialog by rememberSaveable { mutableStateOf(false) }
      Scaffold(
        topBar = {
          TopBarWithBackButton(
            navController = navController,
            title =
              exerciseDescriptor.name.run {
                if (exerciseDescriptor.obsolete) "$this [Archived]" else this
              },
            topEndContent = {
              DropdownButton(showDropdown, { showDropdown = it }) {
                DropdownMenuItem(
                  text = { Text("Rename") },
                  leadingIcon = { Icon(Icons.Filled.Edit, "Rename Exercise") },
                  onClick = {
                    showDropdown = false
                    showRenameDialog = true
                  },
                )
                if (!exerciseDescriptor.obsolete)
                  DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = { Icon(Icons.Filled.Delete, "Delete Exercise") },
                    onClick = {
                      showDropdown = false
                      showDiscardOrDeleteDialog = true
                    },
                  )
              }
            },
          )
        }
      ) { paddingValues ->
        val exercises by
          db.historyDao
            .where(exerciseDescriptor)
            .collectAsStateWithLifecycle(initialValue = emptyList())

        ExercisesSetsListings(
          modifier = Modifier.consumeWindowInsets(paddingValues).padding(paddingValues),
          exercises = exercises,
          exerciseHeadline = { exercise ->
            val doneDate = exercise.getLastCompletedSet()!!.doneTs.toLocalDate()
            Text(doneDate.format(DateFormatters.EEEE_MMM_dd_yyyy))
          },
          settings = settings,
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
            TextButton(
              onClick = {
                errorMessage =
                  db.exerciseDao
                    .validateName(name)
                    .fold(
                      onSuccess = {
                        showRenameDialog = false
                        db.exerciseDao.update(exerciseDescriptor.toBuilder().setName(name).build())
                        ""
                      },
                      onFailure = { throwable -> throwable.message ?: "Invalid Exercise Name" },
                    )
              }
            ) {
              Text("Confirm")
            }
          },
          errorMessage = errorMessage,
          isError = errorMessage.isNotBlank(),
        )
      }

      if (showDiscardOrDeleteDialog)
        ConfirmationAlertDialog(
          onDismissRequest = { showDiscardOrDeleteDialog = false },
          text =
            """
                Data associated with this exercise will not be lost. Even if you delete it, you can still perform the exercise if it appears in a program, and you can also rename it. However, you will not be able to include the exercise in new Programs.

                Do you really want to remove '${exerciseDescriptor.name}'?
            """
              .trimIndent(),
          onConfirm = {
            db.exerciseDao.delete(exerciseDescriptor)
            navController.navigateUp()
          },
        )
    }

    composable<Destination.Workout> { navBackStackEntry ->
      val record = db.historyDao.where(navBackStackEntry.toRoute<Destination.Workout>().recordId)
      Scaffold(
        topBar = {
          TopBarWithBackButton(
            navController = navController,
            title = "${record.workout.name}, ${record.getDate().format(DateFormatters.MMM_dd_yyyy)}",
          )
        }
      ) { paddingValues ->
        ExercisesSetsListings(
          modifier = Modifier.consumeWindowInsets(paddingValues).padding(paddingValues),
          exercises = record.workout.exercisesList,
          exerciseHeadline = { exercise ->
            val exerciseDescriptor = db.exerciseDao.where(exercise.descriptorId)
            Text(text = exerciseDescriptor.name)
          },
          settings = settings,
        )
      }
    }

    composable<Destination.HistoryRecord> { navBackStackEntry ->
      val record = db.historyDao.where(navBackStackEntry.toRoute<Destination.Workout>().recordId)
      val hasChanged = record.workout != trainViewModel.getRecord().workout
      Scaffold(
        topBar = {
          TopBarWithBackButton(
            navController = navController,
            title = record.workout.name,
            topEndContent = {
              if (hasChanged)
                TextButton(
                  onClick = {
                    db.historyDao.update(trainViewModel.getRecord())
                    navController.popBackStack()
                  }
                ) {
                  Icon(
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Finish Workout Edit",
                  )
                  Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                  Text("Finish Edit")
                }
            },
          )
        }
      ) { paddingValues ->
        TrainingScreen(
          modifier = Modifier.consumeWindowInsets(paddingValues).padding(paddingValues),
          trainViewModel = trainViewModel,
          exerciseDao = db.exerciseDao,
          historyDao = db.historyDao,
          settings = settings,
          navController = navController,
          snackbarHostState = snackbarHostState,
        )

        if (showDiscardOrDeleteDialog)
          ConfirmationAlertDialog(
            onDismissRequest = { showDiscardOrDeleteDialog = false },
            text = "Do you want to discard changes made to ${trainViewModel.getWorkoutName()}?",
            onConfirm = { navController.navigateUp() },
          )

        BackHandler(enabled = hasChanged) { showDiscardOrDeleteDialog = true }
      }
    }

    composable<Destination.Settings> {
      Scaffold(
        topBar = { TopBarWithBackButton(navController = navController, title = "Settings") },
        snackbarHost = { SnackbarHost(snackbarHostState) },
      ) { paddingValues ->
        SettingsScreen(
          modifier = Modifier.consumeWindowInsets(paddingValues).padding(paddingValues),
          db = db,
          scope = scope,
          snackbarHostState = snackbarHostState,
        )
      }
    }
  }
}
