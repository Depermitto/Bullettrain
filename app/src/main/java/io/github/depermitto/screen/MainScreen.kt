package io.github.depermitto.screen

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
import io.github.depermitto.history.HistoryTab
import io.github.depermitto.programs.ProgramsTab
import io.github.depermitto.screen.Screen.MainScreen
import io.github.depermitto.settings.SettingsViewModel
import io.github.depermitto.theme.adaptiveIconTint
import io.github.depermitto.theme.filledContainerColor
import io.github.depermitto.train.TrainTab
import io.github.depermitto.train.TrainViewModel

@Composable
fun MainScreen(
    trainViewModel: TrainViewModel,
    settingsViewModel: SettingsViewModel,
    programDao: ProgramDao,
    exerciseDao: ExerciseDao,
    navController: NavController,
) {
    var activeBar by remember { mutableStateOf(MainScreen.Tabs.Programs) }

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
                        ProgramsTab(programDao = programDao, navController = navController)
                    }
                }

                MainScreen.Tabs.History -> HistoryTab()
                MainScreen.Tabs.Train -> TrainTab(
                    trainViewModel = trainViewModel,
                    settingsViewModel = settingsViewModel,
                    exerciseDao = exerciseDao
                )
            }
        }
    }
}