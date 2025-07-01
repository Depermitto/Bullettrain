package org.depermitto

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.depermitto.database.GymDatabase
import org.depermitto.ui.*
import java.io.File

const val DB_FILENAME = "firetent.sqlite"

@Composable
fun App(db: GymDatabase, dbFile: File, fallbackBytes: ByteArray) = MaterialTheme {
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val exerciseDao = db.getExerciseDao()

    var tabIndex by remember { mutableIntStateOf(1) }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    modifier = Modifier.align(Alignment.TopStart),
                    onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
                IconButton(
                    modifier = Modifier.align(Alignment.TopEnd),
                    onClick = { navController.navigate(Screen.SettingsScreen.route) }) {
                    Icon(Icons.Filled.Settings, contentDescription = null)
                }
            }
        },
        bottomBar = {
            val tabs = listOf(
                Screen.HistoryScreen,
                Screen.TrainScreen,
                Screen.PlansScreen,
            )
            TabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { i, text->
                    Tab(
                        text = { Text(text.route[0].uppercase() + text.route.substring(1)) },
//                        icon = { Icon(icon, contentDescription = null) },
                        selected = tabIndex == i,
                        onClick = {
                            tabIndex = i
                            navController.navigate(text.route)
                        }
                    )
                }
                listOf(
                    Screen.HistoryScreen.route,
                    Screen.TrainScreen.route,
                    Screen.PlansScreen.route
                ).forEachIndexed { i, tab ->
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            modifier = Modifier.padding(paddingValues),
            navController = navController,
            startDestination = Screen.ExercisesScreen.route
        ) {
            composable(Screen.TrainScreen.route) {

            }

            composable(Screen.HistoryScreen.route) {

            }

            composable(Screen.PlansScreen.route) {
                PlansScreen(programDao = db.getProgramDao(), navController = navController)
            }

            composable(Screen.PlansCreationScreen.route) {
                PlansCreationScreen(programDao = db.getProgramDao())
            }

            composable(Screen.ExercisesScreen.route) {
                ExercisesScreen(exerciseDao = exerciseDao, navController = navController)
            }

            composable(Screen.ExercisesCreationScreen.route) {
                ExercisesCreationScreen(
                    modifier = Modifier.padding(paddingValues),
                    exerciseDao = exerciseDao,
                    navController = navController
                )
            }

            composable(Screen.SettingsScreen.route) {
                SettingsScreen(
                    db = db,
                    dbFile = dbFile,
                    fallbackBytes = fallbackBytes,
                    scope = scope,
                    navController = navController
                )
            }
        }
    }
}
