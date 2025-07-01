package io.github.depermitto.bullettrain.theme

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import io.github.depermitto.bullettrain.R

val SwapIcon: @Composable () -> Unit = {
    Image(
        painterResource(id = R.drawable.swap_horizontal),
        contentDescription = "Swap Horizontal",
        colorFilter = ColorFilter.tint(adaptiveIconTint())
    )
}

val DuplicateIcon: @Composable () -> Unit = {
    Image(
        painterResource(id = R.drawable.duplicate),
        contentDescription = "Duplicate",
        colorFilter = ColorFilter.tint(adaptiveIconTint())
    )
}

val DragHandleIcon: @Composable () -> Unit = {
    Image(
        painterResource(id = R.drawable.drag_horizontal_variant),
        contentDescription = "Drag Handle",
        colorFilter = ColorFilter.tint(adaptiveIconTint())
    )
}

val HeartPlusIcon: @Composable () -> Unit = {
    Image(
        painterResource(id = R.drawable.heart_plus),
        contentDescription = "Heart Plus",
        colorFilter = ColorFilter.tint(adaptiveIconTint())
    )
}
val HeartRemoveIcon: @Composable () -> Unit = {
    Image(
        painterResource(id = R.drawable.heart_remove),
        contentDescription = "Heart Remove",
        colorFilter = ColorFilter.tint(adaptiveIconTint())
    )
}
