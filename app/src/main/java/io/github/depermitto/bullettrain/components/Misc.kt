package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

@Composable
fun Header(
    modifier: Modifier = Modifier,
    text: String,
) = Text(
    modifier = modifier, text = text, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center
)


@Composable
fun GhostCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) = Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    content = content
)

@Composable
fun GhostCard(modifier: Modifier = Modifier, onClick: () -> Unit, content: @Composable ColumnScope.() -> Unit) = Card(
    modifier = modifier,
    onClick = onClick,
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    content = content
)