package io.github.depermitto.bullettrain.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut

enum class ScaleTransitionDirection {
  INWARDS,
  OUTWARDS,
}

fun scaleIntoContainer(
  direction: ScaleTransitionDirection = ScaleTransitionDirection.INWARDS,
  initialScale: Float = if (direction == ScaleTransitionDirection.OUTWARDS) 0.9F else 1.1F,
): EnterTransition {
  return scaleIn(animationSpec = tween(220, delayMillis = 90), initialScale = initialScale) +
    fadeIn(animationSpec = tween(220, delayMillis = 90))
}

fun scaleOutOfContainer(
  direction: ScaleTransitionDirection = ScaleTransitionDirection.OUTWARDS,
  targetScale: Float = if (direction == ScaleTransitionDirection.INWARDS) 0.9F else 1.1F,
): ExitTransition {
  return scaleOut(
    animationSpec = tween(durationMillis = 220, delayMillis = 90),
    targetScale = targetScale,
  ) + fadeOut(tween(delayMillis = 90))
}
