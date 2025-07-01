package io.github.depermitto.bullettrain.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ConfirmAlertDialog(
    text: String, onDismissRequest: () -> Unit, onConfirm: () -> Unit, modifier: Modifier = Modifier,
) = AlertDialog(
    modifier = modifier,
    onDismissRequest = onDismissRequest,
    dismissButton = { TextButton(onClick = onDismissRequest) { Text("Cancel") } },
    confirmButton = { TextButton(onClick = { onDismissRequest(); onConfirm() }) { Text("Discard") } },
    text = { Text(text = text) },
)
