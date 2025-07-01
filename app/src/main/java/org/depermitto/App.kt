package org.depermitto

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.depermitto.database.GymDatabase
import org.depermitto.ui.Exercises
import org.depermitto.ui.Screen
import org.depermitto.ui.Settings
import org.depermitto.ui.creation.ExerciseCreationWizard
import java.io.File

const val DB_FILENAME = "firetent.sqlite"

@Composable
fun App(db: GymDatabase, dbFile: File, fallbackBytes: ByteArray) = MaterialTheme {
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val exerciseDao = db.getExerciseDao()
    
    NavHost(navController = navController, startDestination = Screen.ExercisesScreen.route) {
        composable(Screen.ExercisesScreen.route) {
            Exercises(exerciseDao = exerciseDao, navController = navController)
        }

        composable(Screen.CreateExerciseScreen.route) {
            Scaffold(topBar = {
                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                    Text(text = "Create a new exercise")
                }
            }) { paddingValues ->
                ExerciseCreationWizard(
                    modifier = Modifier.padding(paddingValues),
                    exerciseDao = exerciseDao,
                    navController = navController
                )
            }
        }

        composable(Screen.SettingsScreen.route) {
            Settings(
                db = db,
                dbFile = dbFile,
                fallbackBytes = fallbackBytes,
                scope = scope,
                navController = navController
            )
        }
    }
}
//    var pageIndex by remember { mutableStateOf(Page.Plans.ordinal) }
//    Scaffold(bottomBar = {
//        TabRow(selectedTabIndex = pageIndex) {
//            Page.entries.forEachIndexed { i, title ->
//                Tab(text = { Text(title.toString()) }, selected = pageIndex == i, onClick = { pageIndex = i })
//            }
//        }
//    }) {
//        when (Page.entries[pageIndex]) {
//            Page.Plans -> Plans(db)
//            Page.Settings -> 
