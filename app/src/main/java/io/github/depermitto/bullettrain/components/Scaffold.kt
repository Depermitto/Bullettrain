package io.github.depermitto.bullettrain.components

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destinations

@Composable
fun Scaffold(
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
fun HeaderWithSettingsButton(
    navController: NavController,
    title: String,
) = Row(verticalAlignment = Alignment.CenterVertically) {
    Text(
        modifier = Modifier
            .padding(start = 16.dp)
            .widthIn(max = 300.dp),
        text = title,
        style = MaterialTheme.typography.titleLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
    Spacer(Modifier.weight(1f))
    IconButton(
        onClick = { navController.navigate(Destinations.Settings) }) {
        Icon(Icons.Filled.Settings, contentDescription = "Settings")
    }
}


@Composable
fun HeaderWithBackButton(
    navController: NavController,
    topEndContent: (@Composable () -> Unit)? = null,
    title: String? = null,
) = Row(verticalAlignment = Alignment.CenterVertically) {
    val onBackPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current
    IconButton(
        onClick = { onBackPressedDispatcherOwner?.onBackPressedDispatcher?.onBackPressed() ?: navController.navigateUp() }) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back Button")
    }
    if (title != null) Text(
        modifier = Modifier.widthIn(max = 300.dp),
        text = title,
        style = MaterialTheme.typography.titleLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
    if (topEndContent != null) {
        Spacer(Modifier.weight(1f))
        topEndContent()
    }
}