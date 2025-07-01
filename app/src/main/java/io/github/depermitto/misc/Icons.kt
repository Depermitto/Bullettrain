package io.github.depermitto.misc

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import io.github.depermitto.R
import io.github.depermitto.theme.adaptiveIconTint

val SwapIcon: @Composable () -> Unit = {
    Image(
        painterResource(id = R.drawable.swap_horizontal),
        contentDescription = "Swap Horizontal",
        colorFilter = ColorFilter.tint(adaptiveIconTint())
    )
}

val IntensityIcon: @Composable () -> Unit = {
    Image(
        painterResource(id = R.drawable.heart_flash),
        contentDescription = "Heart Flash",
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


val TodayIcon: @Composable () -> Unit = {
    Image(
        painterResource(id = R.drawable.calendar_today),
        contentDescription = "Calendar Today",
        colorFilter = ColorFilter.tint(adaptiveIconTint())
    )
}
