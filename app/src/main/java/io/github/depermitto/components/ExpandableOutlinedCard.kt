package io.github.depermitto.components

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
import androidx.compose.ui.unit.dp
import io.github.depermitto.theme.SqueezableIconSize

@Composable
fun ExpandableOutlinedCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    title: @Composable RowScope.() -> Unit,
    dropdownItems: (@Composable () -> Unit)? = null,
    startExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by remember { mutableStateOf(startExpanded) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f)
    OutlinedCard(
        modifier = modifier.animateContentSize(animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                title()
                Spacer(modifier = Modifier.weight(1f))
                IconButton(modifier = Modifier
                    .size(SqueezableIconSize)
                    .alpha(0.5f)
                    .rotate(rotationState),
                    onClick = { expanded = !expanded }) {
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Toggle Expandable Card")
                }
                if (dropdownItems != null) {
                    var showDropdownMenu by remember { mutableStateOf(false) }
                    IconButton(modifier = Modifier.size(SqueezableIconSize), onClick = { showDropdownMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = null)

                        DropdownMenu(expanded = showDropdownMenu, onDismissRequest = { showDropdownMenu = false }) {
                            dropdownItems()
                        }
                    }
                }
            }
            if (expanded) {
                Column(modifier = Modifier.padding(contentPadding), horizontalAlignment = Alignment.CenterHorizontally) {
                    content()
                }
            }
        }
    }
}