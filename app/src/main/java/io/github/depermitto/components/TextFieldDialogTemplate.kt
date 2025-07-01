package io.github.depermitto.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.depermitto.theme.ItemPadding

@Composable
fun TextFieldDialogTemplate(
    modifier: Modifier = Modifier.size(200.dp, 200.dp),
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable (() -> Unit)? = null,
    errorMessage: @Composable (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Boolean
) = OutlinedCard(modifier = modifier) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = ItemPadding)
    ) {
        var isError by remember { mutableStateOf(false) }
        OutlinedTextField(
            modifier = Modifier.align(Alignment.TopCenter),
            value = value, onValueChange = onValueChange,
            label = label,
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                cursorColor = MaterialTheme.colorScheme.tertiary,
                selectionColors = TextSelectionColors(
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f),
                ),
            ),
            isError = isError,
            supportingText = { if (isError) errorMessage?.invoke() }
        )

        Row(modifier = Modifier.align(Alignment.BottomEnd)) {
            TextButton(onClick = { onDismiss() }) {
                Text(text = "Cancel")
            }

            TextButton(onClick = { isError = !onConfirm(value) }) {
                Text(text = "Confirm")
            }
        }
    }
}
