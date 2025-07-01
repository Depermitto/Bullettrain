package io.github.depermitto.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
fun Header(
    modifier: Modifier = Modifier.Companion,
    text: String,
) = Text(
    modifier = modifier,
    text = text,
    style = MaterialTheme.typography.titleSmall,
    textAlign = TextAlign.Companion.Center
)