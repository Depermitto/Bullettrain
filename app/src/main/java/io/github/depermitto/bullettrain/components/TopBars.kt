package io.github.depermitto.bullettrain.components

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenTopBar(navController: NavController, title: String) {
  TopAppBar(
    title = {
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    },
    actions = {
      IconButton(onClick = { navController.navigate(Destination.Settings) }) {
        Icon(Icons.Filled.Settings, contentDescription = "Settings")
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWithBackButton(
  modifier: Modifier = Modifier,
  navController: NavController,
  endContent: (@Composable () -> Unit)? = null,
  title: String,
) {
  val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
  TopAppBar(
    modifier = modifier,
    title = {
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
    },
    navigationIcon = {
      IconButton(
        onClick = { onBackPressedDispatcher?.onBackPressed() ?: navController.navigateUp() }
      ) {
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back Button")
      }
    },
    actions = { endContent?.invoke() },
    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
  )
}
