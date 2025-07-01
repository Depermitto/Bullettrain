package io.github.depermitto.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.times
import io.github.depermitto.theme.ItemPadding

@Composable
fun BoxScope.AnchoredFloatingActionButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: (@Composable () -> Unit)? = { Icon(Icons.Filled.Edit, contentDescription = null) },
    text: (@Composable () -> Unit)? = null,
) {
    @Suppress("NAME_SHADOWING") val modifier = modifier
        .align(Alignment.BottomEnd)
        .padding(bottom = 2 * ItemPadding, end = 2 * ItemPadding)

    if (icon != null && text != null) {
        ExtendedFloatingActionButton(modifier = modifier, onClick = onClick, text = text, icon = icon)
    } else {
        FloatingActionButton(modifier = modifier, onClick = onClick) {
            if (icon != null) icon() else text?.invoke() ?: return@FloatingActionButton
        }
    }
}
