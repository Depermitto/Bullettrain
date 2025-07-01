package org.depermitto.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import org.depermitto.ui.screens.Screen


@Composable
fun BoxScope.SettingsGear(modifier: Modifier = Modifier, navController: NavController) {
    IconButton(
        modifier = modifier.align(Alignment.TopEnd),
        onClick = { navController.navigate(Screen.SettingsScreen.route) }) {
        Icon(Icons.Filled.Settings, contentDescription = null)
    }
}

@Composable
fun BoxScope.BackButton(modifier: Modifier = Modifier, navController: NavController) {
    IconButton(modifier = modifier.align(Alignment.TopStart), onClick = { navController.navigateUp() }) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
    }
}

// TODO Is this necessary? Maybe should somehow control 
//  the state of application instead of just deciding 
//  whether to render or not the scaffold
@Composable
fun QuickScaffoldWithTopBar(
    navController: NavController? = null,
    topBar: @Composable BoxScope.() -> Unit = {
        if (navController != null) {
            BackButton(navController = navController)
            SettingsGear(navController = navController)
        }
    },
    content: @Composable () -> Unit,
) {
    Scaffold(topBar = {
        Box(modifier = Modifier.fillMaxWidth()) {
            topBar()
        }
    }) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            content()
        }
    }
}