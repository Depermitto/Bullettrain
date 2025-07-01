package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun DropdownButton(
  show: Boolean,
  onShowChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  dropdownItems: @Composable () -> Unit,
) {
  Box(modifier = Modifier) {
    IconButton(modifier = modifier, onClick = { onShowChange(true) }) {
      Icon(Icons.Filled.MoreVert, contentDescription = "Options")
    }
    DropdownMenu(expanded = show, onDismissRequest = { onShowChange(false) }) { dropdownItems() }
  }
}
