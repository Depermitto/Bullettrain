package org.depermitto.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate

@Composable
fun ExpandableOutlinedCard(
    title: @Composable () -> Unit,
    dropdownItems: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(true) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "")
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                title()
                IconButton(
                    modifier = Modifier
                        .alpha(0.5f)
                        .weight(1f)
                        .rotate(rotationState),
                    onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown, contentDescription = "Drop-Down Arrow"
                    )
                }
                if (dropdownItems != null) {
                    var showDropdownMenu by remember { mutableStateOf(false) }
                    IconButton(modifier = Modifier.weight(1f), onClick = { showDropdownMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = null)

                        DropdownMenu(expanded = showDropdownMenu, onDismissRequest = { showDropdownMenu = false }) {
                            dropdownItems()
                        }
                    }
                }
            }
            if (expanded) {
                content()
            }
        }
    }
}