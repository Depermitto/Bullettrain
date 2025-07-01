package io.github.depermitto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import io.github.depermitto.Screen.*
import io.github.depermitto.Screen.MainScreen
import io.github.depermitto.Screen.MainScreen.Tabs
import io.github.depermitto.Screen.TrainingScreen
import io.github.depermitto.components.AnchoredFloatingActionButton
import io.github.depermitto.components.Ribbon
import io.github.depermitto.components.RibbonScaffold
import io.github.depermitto.data.entities.Program
import io.github.depermitto.database.Database
import io.github.depermitto.database.Exercise
import io.github.depermitto.programs.Program
import io.github.depermitto.programs.ProgramCreation
import io.github.depermitto.programs.ProgramViewModel
import io.github.depermitto.settings.PersistentData
import io.github.depermitto.settings.SettingsScreen
import io.github.depermitto.settings.SettingsViewModel
import io.github.depermitto.train.TrainViewModel
import io.github.depermitto.train.TrainingScreen
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FileKit.init(this)

//        setContent {
//            GymAppTheme {
//                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
//                    val dbFile = application.getDatabasePath(DB_FILENAME)
//
//                    val settingsFile = File(application.filesDir, SETTINGS_FILENAME)
//                    if (!settingsFile.exists()) {
//                        application.openFileOutput(SETTINGS_FILENAME, MODE_PRIVATE).use {
//                            it.write(Json.encodeToString(Settings()).encodeToByteArray())
//                        }
//                    }
//
//                    val persistentData = PersistentData(
//                        dbFile = dbFile,
//                        fallbackBytes = resources.openRawResource(R.raw.firetent).readBytes(),
//                        db = Room.databaseBuilder<GymDatabase>(context = applicationContext, name = dbFile.absolutePath)
//                            .openHelperFactory(FrameworkSQLiteOpenHelperFactory()).fallbackToDestructiveMigration(true)
//                            .setJournalMode(JournalMode.TRUNCATE).build(),
//                        settingsFile = application.getFileStreamPath(SETTINGS_FILENAME),
//                    )
//
//                    App(persistentData)
//                }
//            }
//        }

        setContent {
            val scope = rememberCoroutineScope()
            val db = Database(application.filesDir)

            val exercises by db.exerciseDao.getAll.collectAsStateWithLifecycle()

            LazyColumn {
                items(exercises) { exercise ->
                    Text((exercise.exerciseId to exercise.name).toString())
                }

                item {
                    Button(onClick = { println(db.exerciseDao.insert(Exercise(name = "Bench"))) }) { Text("Upsert") }
                }

                item {
                    Button(onClick = { db.exerciseDao.delete(exercises.lastOrNull() ?: return@Button) }) { Text("Pop") }
                }

                item {
                    Button(onClick = {
                        db.exerciseDao.update(
                            exercises.component3().copy(name = exercises.component3().name + " 1 ")
                        ).let(::println)
                    }) { Text("Edit") }
                }

                item {
                    Button(onClick = { scope.launch { db.exportDatabase() } }) { Text("Export") }
                }

                item {
                    Button(onClick = { scope.launch { db.importDatabase() } }) { Text("Import") }
                }
            }
        }
    }
}

// TODO P3 make tests and benchmarks, mostly for backend probably
@Composable
fun App(persistentData: PersistentData) = MaterialTheme {
    val programDao = persistentData.db.getProgramDao()
    val historyDao = persistentData.db.getHistoryDao()
    val exerciseDao = persistentData.db.getExerciseDao()

    val navController = rememberNavController()

    val programViewModel = viewModel<ProgramViewModel>(factory = ProgramViewModel.Factory(Program(), programDao))
    val settingsViewModel = viewModel<SettingsViewModel>(factory = SettingsViewModel.Factory(persistentData))
    val trainViewModel =
        viewModel<TrainViewModel>(factory = TrainViewModel.Factory(historyDao, programDao, navController))

    NavHost(
        navController = navController,
        startDestination = if (runBlocking { trainViewModel.restoreWorkout() }) TrainingScreen.route else MainScreen.route
    ) {
        composable(MainScreen.route) { navBackStackEntry ->
            val activeTab = Tabs.valueOf(navBackStackEntry.arguments?.getString("tab") ?: Tabs.Train.name)

            MainScreen(
                trainViewModel = trainViewModel,
                settingsViewModel = settingsViewModel,
                programDao = programDao,
                historyDao = historyDao,
                navController = navController,
                activeTab = activeTab
            )
        }

        composable(TrainingScreen.route) {
            TrainingScreen(
                trainViewModel = trainViewModel,
                settingsViewModel = settingsViewModel,
                exerciseDao = exerciseDao,
            )
        }

        composable(ProgramCreationScreen.route) {
            RibbonScaffold(ribbon = { Ribbon(navController = navController, title = "New Program") }) {
                ProgramCreation(
                    programViewModel = programViewModel, exerciseDao = exerciseDao, navController = navController
                )
            }
        }

        composable(ProgramScreen.route) { navBackStackEntry ->
            val program by programDao.whereIdFlow(
                (navBackStackEntry.arguments?.getString("programId") ?: return@composable).toLong()
            ).collectAsStateWithLifecycle(initialValue = null)

            program?.let {
                val programViewModel = viewModel<ProgramViewModel>(factory = ProgramViewModel.Factory(it, programDao))

                RibbonScaffold(ribbon = { Ribbon(navController, title = programViewModel.programName) }) {
                    Program(programViewModel, exerciseDao = exerciseDao)
                    if (programViewModel.days.toList() != it.days.toList()) {
                        AnchoredFloatingActionButton(text = { Text("Finish Edit") }, onClick = {
                            programViewModel.upsert()
                            navController.popBackStack(MainScreen.route, inclusive = false)
                        })
                    }
                }
            }
        }

        composable(SettingsScreen.route) {
            RibbonScaffold(ribbon = { Ribbon(navController, settingsGear = false, title = "Settings") }) {
                SettingsScreen(settingsViewModel = settingsViewModel)
            }
        }
    }
}
