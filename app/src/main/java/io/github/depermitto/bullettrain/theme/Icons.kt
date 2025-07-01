package io.github.depermitto.bullettrain.theme

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import io.github.depermitto.bullettrain.R

val SwapIcon: @Composable () -> Unit = {
  Icon(painterResource(id = R.drawable.swap_horizontal), contentDescription = "Swap horizontal")
}

val DuplicateIcon: @Composable () -> Unit = {
  Icon(painterResource(id = R.drawable.duplicate), contentDescription = "Duplicate")
}

val DragHandleIcon: @Composable () -> Unit = {
  Icon(painterResource(id = R.drawable.drag_horizontal_variant), contentDescription = "Drag handle")
}

val NumberedListIcon: @Composable () -> Unit = {
  Icon(painterResource(id = R.drawable.format_list_numbered), contentDescription = "Numbered list")
}

val HeartPlusIcon: @Composable () -> Unit = {
  Icon(painterResource(id = R.drawable.heart_plus), contentDescription = "Heart plus")
}

val HeartRemoveIcon: @Composable () -> Unit = {
  Icon(painterResource(id = R.drawable.heart_remove), contentDescription = "Heart remove")
}

val MergeIcon: @Composable () -> Unit = {
  Icon(painterResource(id = R.drawable.merge), contentDescription = "Merge")
}

val SplitIcon: @Composable () -> Unit = {
  Icon(painterResource(id = R.drawable.split), contentDescription = "Split")
}
