package io.github.depermitto.bullettrain.components

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.depermitto.bullettrain.protos.ExercisesProto.Exercise
import io.github.depermitto.bullettrain.theme.DragHandleIcon
import io.github.depermitto.bullettrain.theme.ExtraLarge
import io.github.depermitto.bullettrain.theme.Large
import io.github.depermitto.bullettrain.theme.Medium
import io.github.depermitto.bullettrain.theme.Small
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReorderingAlertDialog(
  title: String,
  modifier: Modifier = Modifier,
  onDismissRequest: () -> Unit,
  dismissButton: @Composable () -> Unit,
  onSettle: (Int, Int) -> Unit,
  exercises: List<Exercise>,
  content: @Composable (Int, Exercise) -> Unit,
) {
  BasicAlertDialog(onDismissRequest = onDismissRequest) {
    Card(modifier.clip(MaterialTheme.shapes.extraLarge)) {
      Text(
        title,
        Modifier.padding(Dp.ExtraLarge),
        style = MaterialTheme.typography.titleLarge,
        maxLines = 1,
      )

      val view = LocalView.current
      val lazyListState = rememberLazyListState()
      val reorderableLazyListState =
        rememberReorderableLazyListState(lazyListState) { from, to ->
          onSettle(from.index, to.index)
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
          }
        }
      LazyColumn(
        modifier = Modifier.heightIn(0.dp, 400.dp),
        contentPadding = PaddingValues(horizontal = Dp.Medium),
        verticalArrangement = Arrangement.spacedBy(Dp.Small),
        state = lazyListState,
      ) {
        itemsIndexed(exercises, key = { _, it -> it.descriptorId }) { exerciseIndex, exercise ->
          ReorderableItem(reorderableLazyListState, key = exercise.descriptorId) { isDragging ->
            val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
            Surface(shadowElevation = elevation, shape = MaterialTheme.shapes.medium) {
              ExtendedListItem(
                headlineContent = { content(exerciseIndex, exercise) },
                trailingContent = {
                  IconButton(
                    modifier =
                      Modifier.draggableHandle(
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
                      ),
                    onClick = {},
                    content = DragHandleIcon,
                  )
                },
                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp),
              )
            }
          }
        }
      }

      Spacer(Modifier.height(12.dp))

      Box(
        Modifier.fillMaxWidth().padding(bottom = Dp.Large, end = Dp.Large),
        contentAlignment = Alignment.BottomEnd,
      ) {
        dismissButton()
      }
    }
  }
}
