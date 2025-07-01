package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.depermitto.bullettrain.theme.ExtraLarge

@Composable
fun EmptyScreen(text: String, modifier: Modifier = Modifier, showIcon: Boolean = true) {
  Column(
    modifier = Modifier.fillMaxSize().padding(horizontal = Dp.ExtraLarge) then modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Spacer(Modifier.weight(if (showIcon) 0.8F else 1F))
    val color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7F)
    if (showIcon)
      Icon(
        Icons.Filled.Clear,
        contentDescription = "No data found",
        modifier = Modifier.size(70.dp),
        tint = color,
      )
    Text(text = text, textAlign = TextAlign.Center, color = color)
    Spacer(Modifier.weight(1F))
  }
}
