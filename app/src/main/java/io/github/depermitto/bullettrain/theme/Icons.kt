package io.github.depermitto.bullettrain.theme

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import io.github.depermitto.bullettrain.R

val SwapIcon: @Composable () -> Unit = {
  Icon(painterResource(id = R.drawable.swap_horizontal), contentDescription = "Swap Horizontal")
}

val DuplicateIcon: @Composable () -> Unit = {
  Icon(painterResource(id = R.drawable.duplicate), contentDescription = "Duplicate")
}

val DragHandleIcon: @Composable () -> Unit = {
  Icon(painterResource(id = R.drawable.drag_horizontal_variant), contentDescription = "Drag Handle")
}

val HeartPlusIcon: @Composable () -> Unit = {
  Icon(painterResource(id = R.drawable.heart_plus), contentDescription = "Heart Plus")
}

val HeartRemoveIcon: @Composable () -> Unit = {
  Icon(painterResource(id = R.drawable.heart_remove), contentDescription = "Heart Remove")
}
