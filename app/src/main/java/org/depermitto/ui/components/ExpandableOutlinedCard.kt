package org.depermitto.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
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
import org.depermitto.ui.theme.horizontalDp

@Composable
fun ExpandableOutlinedCard(
    title: @Composable () -> Unit,
    dropdownItems: (@Composable () -> Unit)? = null,
    startExpanded: Boolean = false,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(startExpanded) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "")
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(6f)
                        .padding(horizontal = horizontalDp)
                ) {
                    title()
                }
                IconButton(modifier = Modifier
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