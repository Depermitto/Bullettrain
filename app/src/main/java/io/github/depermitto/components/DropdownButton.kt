package io.github.depermitto.components

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
    IconButton(modifier = modifier, onClick = { showDropdownMenu = true }) {
        Icon(Icons.Filled.MoreVert, contentDescription = null)

        DropdownMenu(expanded = showDropdownMenu, onDismissRequest = { showDropdownMenu = false }) {
            dropdownItems()
        }
    }
}