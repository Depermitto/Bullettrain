package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.depermitto.bullettrain.theme.BigPadding
import io.github.depermitto.bullettrain.theme.SuperBigPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldAlertDialog(
    startingText: String = "",
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    dismissButton: @Composable () -> Unit,
    confirmButton: @Composable (String) -> Unit,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String = "",
) = BasicAlertDialog(onDismissRequest = onDismissRequest) {
    Card(
        modifier
            .heightIn(0.dp, 250.dp)
            .clip(MaterialTheme.shapes.extraLarge)
    ) {
        var name by rememberSaveable { mutableStateOf(startingText) }
        OutlinedTextField(
            modifier = Modifier.padding(start = SuperBigPadding, end = SuperBigPadding, top = SuperBigPadding),
            value = name, onValueChange = { name = it },
            label = label,
            maxLines = 4,
            isError = isError,
            supportingText = { if (isError) Text(errorMessage) },
        )

        Spacer(Modifier.weight(1f))
        Row(modifier = Modifier.padding(end = BigPadding, bottom = SuperBigPadding)) {
            Spacer(Modifier.weight(1f))
            dismissButton()
            confirmButton(name)
        }
    }
}
