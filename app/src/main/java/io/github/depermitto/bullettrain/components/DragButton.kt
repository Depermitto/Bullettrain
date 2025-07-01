package io.github.depermitto.bullettrain.components

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.depermitto.bullettrain.theme.DragHandleIcon
import io.github.depermitto.bullettrain.theme.SqueezableIconSize
import sh.calvin.reorderable.ReorderableScope

@Composable
fun DragButton(receiver: ReorderableScope, view: View, modifier: Modifier = Modifier) =
  IconButton(
    modifier =
      with(receiver = receiver) {
          modifier.draggableHandle(
            onDragStarted = {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                view.performHapticFeedback(HapticFeedbackConstants.DRAG_START)
              }
            },
            onDragStopped = {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
              }
            },
          )
        }
        .size(SqueezableIconSize),
    onClick = {},
  ) {
    DragHandleIcon()
  }
