package io.github.depermitto.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import io.github.depermitto.components.Ribbon
import io.github.depermitto.components.RibbonScaffold
import io.github.depermitto.data.ExerciseDao
import io.github.depermitto.data.ProgramDao
import io.github.depermitto.presentation.SettingsViewModel
import io.github.depermitto.presentation.TrainViewModel
import io.github.depermitto.screens.Screen.MainScreen
import io.github.depermitto.screens.programs.ProgramsScreen
import io.github.depermitto.screens.train.TrainScreen
import io.github.depermitto.theme.adaptiveIconTint
import io.github.depermitto.theme.filledContainerColor

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    trainViewModel: TrainViewModel,
    settingsViewModel: SettingsViewModel,
    programDao: ProgramDao,
    exerciseDao: ExerciseDao,
    navController: NavController,
) {
    var activeBar by remember { mutableStateOf(MainScreen.Tabs.Train) }

    Scaffold(bottomBar = {
        NavigationBar(containerColor = filledContainerColor()) {
            MainScreen.Tabs.entries.forEach { tab ->
                NavigationBarItem(selected = activeBar == tab, onClick = { activeBar = tab }, icon = {
                    Image(
                        painterResource(id = tab.icon),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(adaptiveIconTint())
                    )
                }, label = { Text(text = tab.name) })
            }
        }
    }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (activeBar) {
                MainScreen.Tabs.Programs -> {
                    RibbonScaffold(ribbon = { Ribbon(navController = navController, backButton = false) }) {
                        ProgramsScreen(programDao = programDao, navController = navController)
                    }
                }

                MainScreen.Tabs.History -> HistoryScreen()
                MainScreen.Tabs.Train -> TrainScreen(
                    trainViewModel = trainViewModel,
                    settingsViewModel = settingsViewModel,
                    exerciseDao = exerciseDao
                )
            }
        }
    }
}