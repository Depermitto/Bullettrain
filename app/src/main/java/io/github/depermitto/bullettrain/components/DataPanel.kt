package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.depermitto.bullettrain.theme.Large

@Composable
fun <T> DataPanel(
  items: List<T>,
  modifier: Modifier = Modifier,
  headline: @Composable () -> Unit,
  headerTextStyle: TextStyle = MaterialTheme.typography.titleSmall,
  headerPadding: PaddingValues = PaddingValues(horizontal = Dp.Large),
  headerContent: @Composable RowScope.() -> Unit,
  colors: CardColors =
    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
  separateHeaderAndContent: Boolean = true,
  textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
  contentPadding: PaddingValues = PaddingValues(0.dp),
  content: @Composable RowScope.(Int, T) -> Unit,
) {
  Card(modifier = modifier, colors = colors) {
    headline.invoke()

    if (items.isEmpty()) return@Card

    CompositionLocalProvider(
      LocalTextStyle provides LocalTextStyle.current.merge(headerTextStyle)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(headerPadding),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        headerContent.invoke(this)
      }
    }
    if (separateHeaderAndContent) HorizontalDivider(modifier = Modifier.padding(headerPadding))
    CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.merge(textStyle)) {
      items.forEachIndexed { index, item ->
        Row(modifier = Modifier.padding(contentPadding)) { content.invoke(this, index, item) }
      }
    }
    Spacer(Modifier.height(12.dp)) // Equivalent to HeroTile vertical Dp
  }
}
