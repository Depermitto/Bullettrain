package org.depermitto.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import org.depermitto.database.ProgramDao


@Composable
fun MainScreen(programDao: ProgramDao, navController: NavController) {
    var activeTabIndex by remember { mutableIntStateOf(Screen.MainScreen.Tabs.Programs.ordinal) }

    Scaffold(bottomBar = {
        TabRow(selectedTabIndex = activeTabIndex) {
            Screen.MainScreen.Tabs.entries.forEach { tab ->
                Tab(text = { Text(text = tab.name) }, selected = activeTabIndex == tab.ordinal, onClick = {
                    activeTabIndex = tab.ordinal
                })
            }
        }
    }) { paddingValues ->
        when (Screen.MainScreen.Tabs.entries[activeTabIndex]) {
            Screen.MainScreen.Tabs.Programs -> ProgramsScreen(
                modifier = Modifier.padding(paddingValues), programDao = programDao, navController = navController
            )

            else -> Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                Text(modifier = Modifier.align(Alignment.Center), text = "Currently Empty")
            }
        }
    }
}