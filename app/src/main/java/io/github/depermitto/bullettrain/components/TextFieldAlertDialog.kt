package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.depermitto.bullettrain.theme.RegularPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldAlertDialog(
    startingText: String = "",
    modifier: Modifier = Modifier.size(200.dp, 200.dp),
    onDismissRequest: () -> Unit,
    dismissButton: @Composable () -> Unit,
    confirmButton: @Composable (String) -> Unit,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String = "",
) = BasicAlertDialog(modifier = Modifier, onDismissRequest = onDismissRequest) {
    var name by rememberSaveable { mutableStateOf(startingText) }
    Card(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(RegularPadding)
        ) {
            OutlinedTextField(
                modifier = Modifier.align(Alignment.TopCenter),
                value = name, onValueChange = { name = it },
                label = label,
                maxLines = 4,
                isError = isError,
                supportingText = { if (isError) Text(errorMessage) },
            )

            Row(modifier = Modifier.align(Alignment.BottomEnd)) {
                dismissButton()
                confirmButton(name)
            }
        }
    }
}
