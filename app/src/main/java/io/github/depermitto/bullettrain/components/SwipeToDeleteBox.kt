package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.depermitto.bullettrain.theme.ItemPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteBox(
    modifier: Modifier = Modifier, threshold: Float = 0.5f, onDelete: () -> Unit, content: @Composable RowScope.() -> Unit
) {
    val swipeState = rememberSwipeToDismissBoxState(positionalThreshold = { it * threshold })

    if (swipeState.currentValue == SwipeToDismissBoxValue.EndToStart) {
        LaunchedEffect(swipeState) {
            onDelete()
            swipeState.snapTo(SwipeToDismissBoxValue.Settled)
        }
        return
    }

    SwipeToDismissBox(
        modifier = modifier, state = swipeState, enableDismissFromStartToEnd = false, backgroundContent = {
            Box(contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer),
                content = {
                    Text(
                        modifier = Modifier.padding(horizontal = ItemPadding),
                        text = "Delete",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                })
        }, content = content
    )
}