package io.github.depermitto.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
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
import io.github.depermitto.data.ExerciseDao
import io.github.depermitto.data.HistoryDao
import io.github.depermitto.data.ProgramDao
import io.github.depermitto.history.HistoryTab
import io.github.depermitto.main.Screen.MainScreen
import io.github.depermitto.programs.ProgramsTab
import io.github.depermitto.settings.SettingsViewModel
import io.github.depermitto.theme.adaptiveIconTint
import io.github.depermitto.theme.filledContainerColor
import io.github.depermitto.train.TrainTab

@Composable
fun MainScreen(
    settingsViewModel: SettingsViewModel,
    programDao: ProgramDao,
    historyDao: HistoryDao,
    exerciseDao: ExerciseDao,
    navController: NavController,
    activeTab: MainScreen.Tabs,
) {
    var activeBar by remember { mutableStateOf(activeTab) }

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
    }, topBar = {
        Box(modifier = Modifier.fillMaxWidth()) {
            Ribbon(navController = navController, title = activeBar.name, backButton = false)
        }
    }) { paddingValues ->
        when (activeBar) {
            MainScreen.Tabs.Programs -> ProgramsTab(
                modifier = Modifier.padding(paddingValues), programDao = programDao, navController = navController
            )

            MainScreen.Tabs.History -> HistoryTab(
                modifier = Modifier.padding(paddingValues), historyDao = historyDao
            )

            MainScreen.Tabs.Train -> TrainTab(
                modifier = Modifier.padding(paddingValues),
                settingsViewModel = settingsViewModel,
                historyDao = historyDao,
                programDao = programDao,
                exerciseDao = exerciseDao
            )
        }
    }
}