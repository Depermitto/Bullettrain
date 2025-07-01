package io.github.depermitto.bullettrain.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destinations
import io.github.depermitto.bullettrain.database.HistoryDao
import io.github.depermitto.bullettrain.database.ProgramDao
import io.github.depermitto.bullettrain.database.SettingsDao
import io.github.depermitto.bullettrain.history.HistoryTab
import io.github.depermitto.bullettrain.programs.ProgramViewModel
import io.github.depermitto.bullettrain.programs.ProgramsTab
import io.github.depermitto.bullettrain.theme.adaptiveIconTint
import io.github.depermitto.bullettrain.theme.filledContainerColor
import io.github.depermitto.bullettrain.train.TrainTab
import io.github.depermitto.bullettrain.train.TrainViewModel

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    trainViewModel: TrainViewModel,
    programViewModel: ProgramViewModel,
    settingsDao: SettingsDao,
    programDao: ProgramDao,
    historyDao: HistoryDao,
    navController: NavController,
) {
    var dragDirection by remember { mutableFloatStateOf(0f) }
    Scaffold(modifier = Modifier.pointerInput(Unit) {
        detectDragGestures(onDragEnd = {
            when {
                dragDirection > 0 -> homeViewModel.switchTab(
                    tab = when (homeViewModel.activeBar) {
                        Destinations.Home.Tabs.History -> Destinations.Home.Tabs.History
                        Destinations.Home.Tabs.Train -> Destinations.Home.Tabs.History
                        Destinations.Home.Tabs.Programs -> Destinations.Home.Tabs.Train
                    }
                )

                dragDirection < 0 -> homeViewModel.switchTab(
                    tab = when (homeViewModel.activeBar) {
                        Destinations.Home.Tabs.History -> Destinations.Home.Tabs.Train
                        Destinations.Home.Tabs.Train -> Destinations.Home.Tabs.Programs
                        Destinations.Home.Tabs.Programs -> Destinations.Home.Tabs.Programs
                    }
                )
            }
        }, onDrag = { _, dragAmount -> dragDirection = dragAmount.x })
    }, bottomBar = {
        NavigationBar(containerColor = filledContainerColor()) {
            Destinations.Home.Tabs.entries.forEach { tab ->
                NavigationBarItem(selected = homeViewModel.activeBar == tab, onClick = { homeViewModel.switchTab(tab) }, icon = {
                    Image(
                        painterResource(id = tab.icon),
                        contentDescription = tab.name,
                        colorFilter = ColorFilter.tint(adaptiveIconTint())
                    )
                }, label = { Text(text = tab.name) })
            }
        }
    }) { paddingValues ->
        when (homeViewModel.activeBar) {
            Destinations.Home.Tabs.Programs -> ProgramsTab(
                modifier = Modifier.padding(paddingValues),
                programViewModel = programViewModel,
                programDao = programDao,
                navController = navController,
            )

            Destinations.Home.Tabs.History -> HistoryTab(
                modifier = Modifier.padding(paddingValues),
                homeViewModel = homeViewModel,
                settingsDao = settingsDao,
                historyDao = historyDao,
            )

            Destinations.Home.Tabs.Train -> TrainTab(
                modifier = Modifier.padding(paddingValues), trainViewModel = trainViewModel, programDao = programDao
            )
        }
    }
}