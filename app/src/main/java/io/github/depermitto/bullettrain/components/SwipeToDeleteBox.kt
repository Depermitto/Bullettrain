package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.depermitto.bullettrain.theme.Medium

@Composable
fun SwipeToDeleteBox(
  modifier: Modifier = Modifier,
  threshold: Float = 0.5F,
  onDelete: () -> Unit,
  text: String = "Delete",
  shadowElevation: Dp = 0.dp,
  shape: Shape = RectangleShape,
  content: @Composable RowScope.() -> Unit,
) {
  val swipeState = rememberSwipeToDismissBoxState(positionalThreshold = { it * threshold })

  if (swipeState.currentValue == SwipeToDismissBoxValue.EndToStart) {
    LaunchedEffect(swipeState) {
      onDelete()
      swipeState.snapTo(SwipeToDismissBoxValue.Settled)
    }
    return
  }

  Surface(shadowElevation = shadowElevation, shape = shape) {
    SwipeToDismissBox(
      modifier = modifier.clip(shape),
      state = swipeState,
      enableDismissFromStartToEnd = false,
      backgroundContent = {
        Box(
          contentAlignment = Alignment.CenterEnd,
          modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.errorContainer),
          content = {
            Text(
              modifier = Modifier.padding(horizontal = Dp.Medium),
              text = text,
              color = MaterialTheme.colorScheme.onErrorContainer,
            )
          },
        )
      },
      content = content,
    )
  }
}
