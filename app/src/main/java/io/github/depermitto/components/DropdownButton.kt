package io.github.depermitto.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun DropdownButton(modifier: Modifier = Modifier, dropdownItems: @Composable () -> Unit) {
    var showDropdownMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier) {
        IconButton(modifier = modifier, onClick = { showDropdownMenu = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = null)
        }
        DropdownMenu(expanded = showDropdownMenu, onDismissRequest = { showDropdownMenu = false }) {
            dropdownItems()
        }
    }
}

@Composable
fun DropdownButton(
    modifier: Modifier = Modifier,
    show: Boolean,
    onShowChange: (Boolean) -> Unit,
    dropdownItems: @Composable () -> Unit,
) {
    Box(modifier = Modifier) {
        IconButton(modifier = modifier, onClick = { onShowChange(true) }) {
            Icon(Icons.Filled.MoreVert, contentDescription = null)
        }
        DropdownMenu(expanded = show, onDismissRequest = { onShowChange(false) }) {
            dropdownItems()
        }
    }
}
