package io.github.depermitto.home

import androidx.compose.foundation.Image
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
import io.github.depermitto.database.HistoryDao
import io.github.depermitto.database.ProgramDao
import io.github.depermitto.database.SettingsDao
import io.github.depermitto.history.HistoryTab
import io.github.depermitto.programs.ProgramsTab
import io.github.depermitto.theme.adaptiveIconTint
import io.github.depermitto.theme.filledContainerColor
import io.github.depermitto.train.TrainTab
import io.github.depermitto.train.TrainViewModel

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    trainViewModel: TrainViewModel,
    settingsDao: SettingsDao,
    programDao: ProgramDao,
    historyDao: HistoryDao,
    navController: NavController,
) = Scaffold(bottomBar = {
    NavigationBar(containerColor = filledContainerColor()) {
        Screen.HomeScreen.Tabs.entries.forEach { tab ->
            NavigationBarItem(selected = homeViewModel.activeBar == tab, onClick = { homeViewModel.activeBar = tab }, icon = {
                Image(
                    painterResource(id = tab.icon), contentDescription = null, colorFilter = ColorFilter.tint(adaptiveIconTint())
                )
            }, label = { Text(text = tab.name) })
        }
    }
}) { paddingValues ->
    when (homeViewModel.activeBar) {
        Screen.HomeScreen.Tabs.Programs -> ProgramsTab(
            modifier = Modifier.padding(paddingValues),
            programDao = programDao,
            navController = navController,
        )

        Screen.HomeScreen.Tabs.History -> HistoryTab(
            modifier = Modifier.padding(paddingValues),
            settingsDao = settingsDao,
            historyDao = historyDao,
        )

        Screen.HomeScreen.Tabs.Train -> TrainTab(
            modifier = Modifier.padding(paddingValues), trainViewModel = trainViewModel, programDao = programDao
        )
    }
}