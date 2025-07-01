package io.github.depermitto.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import io.github.depermitto.Screen

@Composable
fun BoxScope.SettingsGear(modifier: Modifier = Modifier, navController: NavController) {
    IconButton(
        modifier = modifier.align(Alignment.TopEnd),
        onClick = { navController.navigate(Screen.SettingsScreen.route) }) {
        Icon(Icons.Filled.Settings, contentDescription = null)
    }
}

@Composable
fun BoxScope.BackButton(navController: NavController, modifier: Modifier = Modifier) {
    IconButton(modifier = modifier.align(Alignment.TopStart), onClick = { navController.navigateUp() }) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
    }
}

@Composable
fun RibbonScaffold(
    modifier: Modifier = Modifier,
    ribbon: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Scaffold(topBar = {
        Box(modifier = Modifier.fillMaxWidth()) {
            ribbon()
        }
    }) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            content()
        }
    }
}

@Composable
fun BoxScope.Ribbon(
    navController: NavController,
    backButton: Boolean = true,
    settingsGear: Boolean = true,
    title: String? = null,
) {
    if (backButton) BackButton(modifier = Modifier.align(Alignment.CenterStart), navController = navController)
    if (title != null) Text(
        modifier = Modifier.align(Alignment.Center),
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    )
    if (settingsGear) SettingsGear(modifier = Modifier.align(Alignment.CenterEnd), navController = navController)
}