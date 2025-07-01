package org.depermitto.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import org.depermitto.data.ProgramDao
import org.depermitto.ui.screens.Screen.MainScreen.Tabs
import org.depermitto.ui.theme.adaptiveIconTint
import org.depermitto.ui.theme.filledContainerColor

@Composable
fun MainScreen(programDao: ProgramDao, navController: NavController) {
    var activeBar by remember { mutableStateOf(Tabs.Programs) }

    Scaffold(bottomBar = {
        NavigationBar(containerColor = filledContainerColor()) {
            Tabs.entries.forEach { tab ->
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
                Tabs.Programs -> ProgramsScreen(
                    programDao = programDao, navController = navController
                )

                else -> Text(modifier = Modifier.align(Alignment.Center), text = "Currently Empty")
            }
        }
    }
}