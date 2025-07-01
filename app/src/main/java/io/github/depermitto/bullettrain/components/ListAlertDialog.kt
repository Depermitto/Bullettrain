package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.depermitto.bullettrain.theme.BigPadding
import io.github.depermitto.bullettrain.theme.SuperBigPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ListAlertDialog(
    title: String,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onSelected: (T) -> Unit,
    dismissButton: @Composable () -> Unit,
    list: List<T>,
    content: @Composable (T) -> Unit,
) = BasicAlertDialog(onDismissRequest = onDismissRequest) {
    Card(
        modifier
            .heightIn(0.dp, 350.dp)
            .clip(MaterialTheme.shapes.extraLarge)
    ) {
        Text(title, Modifier.padding(SuperBigPadding), style = MaterialTheme.typography.titleLarge, maxLines = 2)

        LazyColumn(Modifier.heightIn(0.dp, 190.dp)) {
            items(list) { item ->
                DropdownMenuItem(text = { content(item) }, onClick = { onSelected(item) })
            }
        }

        Spacer(Modifier.weight(1f))
        Box(
            Modifier
                .fillMaxWidth()
                .padding(bottom = BigPadding, end = BigPadding), contentAlignment = Alignment.BottomEnd
        ) {
            dismissButton()
        }
    }
}