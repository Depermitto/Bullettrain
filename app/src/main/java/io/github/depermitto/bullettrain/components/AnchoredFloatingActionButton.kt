package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import io.github.depermitto.bullettrain.theme.Large

@Composable
fun BoxScope.AnchoredFloatingActionButton(
  modifier: Modifier = Modifier,
  icon: (@Composable () -> Unit)? = { Icon(Icons.Filled.Edit, contentDescription = null) },
  text: (@Composable () -> Unit)? = null,
  containerColor: Color = FloatingActionButtonDefaults.containerColor,
  contentColor: Color = contentColorFor(containerColor),
  onClick: () -> Unit,
) {
  val layeredModifier = Modifier.align(Alignment.BottomEnd).padding(Dp.Large) then modifier
  if (icon != null && text != null) {
    ExtendedFloatingActionButton(
      modifier = layeredModifier,
      onClick = onClick,
      text = text,
      icon = icon,
      containerColor = containerColor,
      contentColor = contentColor,
    )
  } else {
    FloatingActionButton(
      modifier = layeredModifier,
      onClick = onClick,
      containerColor = containerColor,
      contentColor = contentColor,
    ) {
      if (icon != null) icon() else text?.invoke() ?: return@FloatingActionButton
    }
  }
}
