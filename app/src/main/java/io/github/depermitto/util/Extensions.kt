package io.github.depermitto.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

fun <T> List<T>.set(index: Int, value: T) = List(size) { if (it == index) value else this[it] }

fun Color.blend(color: Color, ratio: Float) = Color(ColorUtils.blendARGB(this.toArgb(), color.toArgb(), ratio))