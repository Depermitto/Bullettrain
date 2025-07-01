package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HoldToShowOptionsBox(
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  holdOptions: @Composable (closeDropdown: () -> Unit) -> Unit,
  content: @Composable BoxScope.() -> Unit,
) {
  var showDropdown by remember { mutableStateOf(false) }
  Box(
    modifier =
      modifier
        .clip(shape = MaterialTheme.shapes.medium)
        .combinedClickable(onClick = onClick, onLongClick = { showDropdown = true })
  ) {
    DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false }) {
      holdOptions { showDropdown = false }
    }

    content()
  }
}
