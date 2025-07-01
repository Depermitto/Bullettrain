package io.github.depermitto.bullettrain

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
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
import io.github.depermitto.bullettrain.components.ExtendedListItem
import io.github.depermitto.bullettrain.components.HomeTopBar
import io.github.depermitto.bullettrain.components.TextFieldAlertDialog
import io.github.depermitto.bullettrain.components.Toolbar
import io.github.depermitto.bullettrain.db.Db
import io.github.depermitto.bullettrain.exercises.ExerciseScreen
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
import io.github.depermitto.bullettrain.theme.ScaleTransitionDirection
import io.github.depermitto.bullettrain.theme.scaleIntoContainer
import io.github.depermitto.bullettrain.theme.scaleOutOfContainer
import io.github.depermitto.bullettrain.train.TrainViewModel
import io.github.depermitto.bullettrain.train.TrainingScreen
import io.github.depermitto.bullettrain.util.DateFormatters
import io.github.depermitto.bullettrain.util.capwords
import io.github.depermitto.bullettrain.util.date
import io.github.depermitto.bullettrain.util.toZonedDateTime
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

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

  val homeViewModel =
    viewModel<HomeViewModel>(
      factory = HomeViewModel.Factory(initialPage = Tab.Train, historyDao = db.historyDao)
    )
  val settings by db.settingsDao.get.collectAsStateWithLifecycle()
  val trainViewModel =
    viewModel<TrainViewModel>(factory = TrainViewModel.Factory(db.historyDao, db.programDao))
  var programViewModel =
    viewModel<ProgramViewModel>(factory = ProgramViewModel.Factory(Program.getDefaultInstance()))

  // Used across every NavHost.composable
  var showDiscardOrDeleteDialog by rememberSaveable { mutableStateOf(false) }
  var showFinishDialog by rememberSaveable { mutableStateOf(false) }

  val focusManager = LocalFocusManager.current
  NavHost(
    navController = navController,
    startDestination =
      rememberSaveable(
        saver =
          Saver(
            save = { original -> Json.encodeToString(original) },
            restore = { saveable -> Json.decodeFromString(saveable) },
          )
      ) {
        if (trainViewModel.restoreWorkout()) Destination.Training else Destination.Home
      },
    modifier =
      Modifier.pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
    enterTransition = { scaleIntoContainer() },
    exitTransition = { scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS) },
    popEnterTransition = { scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS) },
    popExitTransition = { scaleOutOfContainer() },
  ) {
    composable<Destination.Home> {
      val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
      Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { HomeTopBar(navController = navController, scrollBehavior = scrollBehavior) },
        bottomBar = {
          NavigationBar(tonalElevation = 8.dp) {
            Tab.entries.forEachIndexed { tabIndex, tab ->
              val isSelected = homeViewModel.screenPager.currentPage == tabIndex
              NavigationBarItem(
                selected = isSelected,
                onClick = {
                  if (!isSelected) {
                    scope.launch { homeViewModel.screenPager.animateScrollToPage(tabIndex) }
                  }
                },
                icon = { Icon(painterResource(id = tab.icon), tab.name) },
                label = { Text(tab.name) },
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
          navController = navController,
        )
      }
    }

    composable<Destination.Training> {
      if (!trainViewModel.isWorkoutRunning()) return@composable

      Scaffold(
        topBar = {
          CenterAlignedTopAppBar(
            title = {
              Text(
                trainViewModel.elapsed(),
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
              )
            },
            navigationIcon = {
              TextButton(
                onClick = { showDiscardOrDeleteDialog = true },
                colors =
                  ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
              ) {
                Icon(Icons.Filled.Close, "Cancel workout", Modifier.size(ButtonDefaults.IconSize))
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text("Drop")
              }
            },
            actions = { TextButton(onClick = { showFinishDialog = true }) { Text("Conclude") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
          )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
      ) { paddingValues ->
        TrainingScreen(
          trainViewModel = trainViewModel,
          exerciseDao = db.exerciseDao,
          historyDao = db.historyDao,
          settings = settings,
          modifier = Modifier.consumeWindowInsets(paddingValues).padding(paddingValues),
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
            trainViewModel.cancelWorkout(navController)
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
                trainViewModel.completeWorkout(navController)
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
          Toolbar(
            title = programViewModel.programName.ifBlank { "New program" },
            navController = navController,
            endContent = {
              TextButton(
                onClick = {
                  val program = programViewModel.getProgram()
                  if (program.name.isBlank()) {
                    scope.launch {
                      snackbarHostState.showSnackbar("Blank program name", withDismissAction = true)
                    }
                    return@TextButton
                  }
                  if (program.workoutsCount == 0) {
                    scope.launch {
                      snackbarHostState.showSnackbar("No workout created", withDismissAction = true)
                    }
                    return@TextButton
                  }

                  navController.navigateUp()
                  db.programDao.insert(program)
                  programViewModel.revertToDefault()
                }
              ) {
                Icon(
                  Icons.Filled.Check,
                  "Finish program creation",
                  modifier = Modifier.size(ButtonDefaults.IconSize),
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

    composable<Destination.DirectDay> { navBackStackEntry ->
      val route = navBackStackEntry.toRoute<Destination.DirectDay>()
      val program by db.programDao.whereAsState(route.programId)
      val day = program.getWorkouts(route.dayIndex)

      programViewModel = viewModel(factory = ProgramViewModel.Factory(program))
      val hasChanged = day != programViewModel.getDay(route.dayIndex)

      Scaffold(
        topBar = {
          Toolbar(
            title = day.name,
            navController = navController,
            endContent = {
              if (hasChanged)
                TextButton(
                  onClick = {
                    db.programDao.update(programViewModel.getProgram())
                    navController.popBackStack()
                  }
                ) {
                  Icon(
                    Icons.Filled.Check,
                    "Complete workout edit",
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                  )
                  Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                  Text("Finish Edit")
                }
            },
          )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
      ) { paddingValues ->
        DayScreen(
          modifier = Modifier.consumeWindowInsets(paddingValues).padding(paddingValues),
          programViewModel = programViewModel,
          dayIndex = route.dayIndex,
          exerciseDao = db.exerciseDao,
          historyDao = db.historyDao,
          navController = navController,
          snackbarHostState = snackbarHostState,
        )
      }

      if (showDiscardOrDeleteDialog)
        ConfirmationAlertDialog(
          onDismissRequest = { showDiscardOrDeleteDialog = false },
          text = "Do you want to discard changes made to ${day.name}?",
          onConfirm = { navController.navigateUp() },
        )

      BackHandler(enabled = hasChanged) { showDiscardOrDeleteDialog = true }
    }

    composable<Destination.Day> { navBackStackEntry ->
      val dayIndex = navBackStackEntry.toRoute<Destination.Day>().dayIndex
      val day = programViewModel.getDay(dayIndex)

      Scaffold(
        topBar = { Toolbar(navController = navController, title = day.name) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
      ) { paddingValues ->
        DayScreen(
          modifier = Modifier.consumeWindowInsets(paddingValues).padding(paddingValues),
          programViewModel = programViewModel,
          dayIndex = dayIndex,
          exerciseDao = db.exerciseDao,
          historyDao = db.historyDao,
          navController = navController,
          snackbarHostState = snackbarHostState,
        )
      }
    }

    composable<Destination.Program> { navBackStackEntry ->
      val program by
        db.programDao.whereAsState(navBackStackEntry.toRoute<Destination.Program>().programId)

      programViewModel = viewModel(factory = ProgramViewModel.Factory(program))
      val hasChanged = program.workoutsList != programViewModel.getDays()

      Scaffold(
        topBar = {
          Toolbar(
            title = programViewModel.programName,
            navController = navController,
            endContent = {
              if (hasChanged)
                TextButton(
                  onClick = {
                    db.programDao.update(programViewModel.getProgram())
                    navController.popBackStack()
                  }
                ) {
                  Icon(
                    Icons.Filled.Check,
                    "Complete program edit",
                    modifier = Modifier.size(ButtonDefaults.IconSize),
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
      val descriptor by
        db.exerciseDao.whereAsState(navBackStackEntry.toRoute<Destination.Exercise>().descriptorId)

      var showDropdown by remember { mutableStateOf(false) }
      var showRenameDialog by rememberSaveable { mutableStateOf(false) }
      Scaffold(
        topBar = {
          Toolbar(
            navController = navController,
            title = if (descriptor.obsolete) "${descriptor.name} [Archived]" else descriptor.name,
            endContent = {
              DropdownButton(showDropdown, { showDropdown = it }) {
                DropdownMenuItem(
                  text = { Text("Rename") },
                  leadingIcon = { Icon(Icons.Filled.Edit, "Rename exercise") },
                  onClick = {
                    showDropdown = false
                    showRenameDialog = true
                  },
                )
                if (!descriptor.obsolete)
                  DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = { Icon(Icons.Filled.Delete, "Delete exercise") },
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
        ExerciseScreen(
          modifier = Modifier.consumeWindowInsets(paddingValues).padding(paddingValues),
          historyDao = db.historyDao,
          descriptor = descriptor,
          settings = settings,
        )

        // This is a essentially copy from ExercisesListScreen.kt
        if (showRenameDialog) {
          var errorMessage by rememberSaveable { mutableStateOf("") }
          TextFieldAlertDialog(
            startingText = descriptor.name,
            onDismissRequest = { showRenameDialog = false },
            label = { Text("Exercise Name") },
            dismissButton = {
              TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            },
            confirmButton = { name ->
              TextButton(
                onClick = {
                  scope.launch {
                    errorMessage =
                      if (name.isBlank()) {
                        "Empty exercise name"
                      } else if (
                        db.exerciseDao.getVisible.first().any { d -> d.name == name.capwords() }
                      ) {
                        "Duplicate exercise name"
                      } else {
                        showRenameDialog = false
                        db.exerciseDao.update(descriptor.toBuilder().setName(name).build())
                        ""
                      }
                  }
                }
              ) {
                Text("Confirm")
              }
            },
            errorMessage = errorMessage,
            isError = errorMessage.isNotBlank(),
          )
        }
      }

      if (showDiscardOrDeleteDialog)
        ConfirmationAlertDialog(
          onDismissRequest = { showDiscardOrDeleteDialog = false },
          text =
            """
                Data associated with this exercise will not be lost. Even if you delete it, you can still perform the exercise if it appears in a program, and you can also rename it. However, you will not be able to include the exercise in new Programs.

                Do you really want to remove '${descriptor.name}'?
            """
              .trimIndent(),
          onConfirm = {
            db.exerciseDao.delete(descriptor)
            navController.navigateUp()
          },
        )
    }

    composable<Destination.Workout> { navBackStackEntry ->
      val record by
        db.historyDao.whereAsState(navBackStackEntry.toRoute<Destination.Workout>().recordId)
      Scaffold(
        topBar = {
          val onBackPressedDispatcher =
            LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
          TopAppBar(
            title = {
              if (record.hasRelatedProgramId()) {
                val relatedProgram by db.programDao.whereAsState(record.relatedProgramId)
                ExtendedListItem(
                  headlineContent = {
                    Text(relatedProgram.name, style = MaterialTheme.typography.titleLarge)
                  },
                  headlineTextStyle = MaterialTheme.typography.titleLarge,
                  supportingContent = { Text(record.workout.name) },
                )
              } else
                Text(
                  DateFormatters.MMM_dd.format(record.date) + " Workout",
                  style = MaterialTheme.typography.titleLarge,
                )
            },
            navigationIcon = {
              IconButton(
                onClick = { onBackPressedDispatcher?.onBackPressed() ?: navController.navigateUp() }
              ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back button")
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
          )
        }
      ) { paddingValues ->
        ExercisesSetsListings(
          modifier = Modifier.consumeWindowInsets(paddingValues).padding(paddingValues),
          exercises = record.workout.exercisesList,
          headline = { e ->
            val descriptor by db.exerciseDao.whereAsState(e.descriptorId)
            Text(descriptor.name)
          },
          supportingContent = { e ->
            val start = e.setsList.first().doneTs.toZonedDateTime()
            val startTimeText = DateFormatters.kk_mm.format(start.toOffsetDateTime())
            Text("${e.setsCount} sets, $startTimeText")
          },
          settings = settings,
        )
      }
    }

    composable<Destination.HistoryRecord> { navBackStackEntry ->
      val record by
        db.historyDao.whereAsState(navBackStackEntry.toRoute<Destination.Workout>().recordId)
      val modified = trainViewModel.getRecord()
      val hasChanged =
        record.workout.exercisesCount != modified.workout.exercisesCount ||
          record.workout.exercisesList.zip(modified.workout.exercisesList).any { (e1, e2) ->
            e1.descriptorId != e2.descriptorId ||
              e1.setsCount != e2.setsCount ||
              e1.setsList.zip(e2.setsList).any { (s1, s2) ->
                s1.weight != s2.weight || s1.actual != s2.actual
              }
          }
      Scaffold(
        topBar = {
          Toolbar(
            navController = navController,
            title = record.workout.name,
            endContent = {
              if (hasChanged)
                TextButton(
                  onClick = {
                    db.historyDao.update(trainViewModel.getRecord())
                    navController.popBackStack()
                  }
                ) {
                  Icon(
                    Icons.Filled.Check,
                    "Finish workout edit",
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                  )
                  Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                  Text("Finish Edit")
                }
            },
          )
        }
      ) { paddingValues ->
        TrainingScreen(
          trainViewModel = trainViewModel,
          exerciseDao = db.exerciseDao,
          historyDao = db.historyDao,
          settings = settings,
          modifier = Modifier.consumeWindowInsets(paddingValues).padding(paddingValues),
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
        topBar = { Toolbar(navController = navController, title = "Settings") },
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
