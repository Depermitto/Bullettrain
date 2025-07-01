package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Screen

@Composable
fun RibbonScaffold(
    modifier: Modifier = Modifier,
    ribbon: @Composable BoxScope.() -> Unit,
    floatingActionButton: (@Composable () -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Scaffold(floatingActionButton = floatingActionButton ?: {}, topBar = {
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
    if (backButton) {
        IconButton(modifier = Modifier.align(Alignment.TopStart), onClick = { navController.navigateUp() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back Button")
        }
    }
    if (title != null) Text(
        modifier = Modifier.align(Alignment.Center),
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    )
    if (settingsGear) {
        IconButton(modifier = Modifier.align(Alignment.TopEnd), onClick = { navController.navigate(Screen.SettingsScreen.route) }) {
            Icon(Icons.Filled.Settings, contentDescription = "Settings")
        }
    }
}