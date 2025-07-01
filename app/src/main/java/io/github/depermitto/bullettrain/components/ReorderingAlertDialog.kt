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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import io.github.depermitto.bullettrain.theme.ExtraLarge
import io.github.depermitto.bullettrain.theme.Large
import io.github.depermitto.bullettrain.theme.Medium
import sh.calvin.reorderable.ReorderableColumn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ReorderingAlertDialog(
  title: String,
  modifier: Modifier = Modifier,
  onDismissRequest: () -> Unit,
  dismissButton: @Composable () -> Unit,
  onSettle: (Int, Int) -> Unit,
  list: List<T>,
  content: @Composable (Int, T) -> Unit,
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
      ReorderableColumn(
        modifier = Modifier.padding(horizontal = Dp.Medium),
        list = list,
        verticalArrangement = Arrangement.spacedBy(Dp.Medium),
        onMove = {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
          }
        },
        onSettle = { from, to -> onSettle(from, to) },
      ) { index, item, isDragging ->
        val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
        Surface(shadowElevation = elevation, shape = MaterialTheme.shapes.medium) {
          ExtendedListItem(
            headlineContent = { content(index, item) },
            trailingContent = { DragButton(this@ReorderableColumn, view) },
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp),
          )
        }
      }

      Spacer(Modifier.height(24.dp))
      Box(
        Modifier.fillMaxWidth().padding(bottom = Dp.Large, end = Dp.Large),
        contentAlignment = Alignment.BottomEnd,
      ) {
        dismissButton()
      }
    }
  }
}
