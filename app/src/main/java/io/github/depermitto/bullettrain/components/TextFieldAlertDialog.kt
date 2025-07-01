package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.depermitto.bullettrain.theme.ItemPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldAlertDialog(
    startingText: String = "",
    modifier: Modifier = Modifier.size(200.dp, 200.dp),
    onDismissRequest: () -> Unit,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String = "",
    cancelButton: @Composable () -> Unit,
    confirmButton: @Composable (String) -> Unit,
) = BasicAlertDialog(modifier = Modifier, onDismissRequest = onDismissRequest) {
    var name by remember { mutableStateOf(startingText) }
    OutlinedCard(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(ItemPadding)
        ) {
            OutlinedTextField(
                modifier = Modifier.align(Alignment.TopCenter),
                value = name, onValueChange = { name = it },
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
                supportingText = { Text(errorMessage) },
            )

            Row(modifier = Modifier.align(Alignment.BottomEnd)) {
                cancelButton()
                confirmButton(name)
            }
        }
    }
}
