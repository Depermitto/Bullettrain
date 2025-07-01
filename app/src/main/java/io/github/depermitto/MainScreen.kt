package io.github.depermitto

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import io.github.depermitto.components.Ribbon
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
fun MainScreen(
    trainViewModel: TrainViewModel,
    settingsDao: SettingsDao,
    programDao: ProgramDao,
    historyDao: HistoryDao,
    navController: NavController,
    activeTab: Screen.MainScreen.Tabs,
) {
    var activeBar by rememberSaveable { mutableStateOf(activeTab) }

    Scaffold(bottomBar = {
        NavigationBar(containerColor = filledContainerColor()) {
            Screen.MainScreen.Tabs.entries.forEach { tab ->
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
        if (activeBar != Screen.MainScreen.Tabs.History) Box(modifier = Modifier.fillMaxWidth()) {
            Ribbon(navController = navController, title = activeBar.name, backButton = false)
        }
    }) { paddingValues ->
        when (activeBar) {
            Screen.MainScreen.Tabs.Programs -> ProgramsTab(
                modifier = Modifier.padding(paddingValues),
                programDao = programDao,
                navController = navController,
            )

            Screen.MainScreen.Tabs.History -> HistoryTab(
                modifier = Modifier.padding(paddingValues),
                settingsDao = settingsDao,
                historyDao = historyDao,
            )

            Screen.MainScreen.Tabs.Train -> TrainTab(
                modifier = Modifier.padding(paddingValues),
                trainViewModel = trainViewModel,
                programDao = programDao
            )
        }
    }
}