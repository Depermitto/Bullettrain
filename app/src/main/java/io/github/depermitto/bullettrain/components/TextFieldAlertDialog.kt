package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.depermitto.bullettrain.theme.ExtraLarge
import io.github.depermitto.bullettrain.theme.Large

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldAlertDialog(
  dismissButton: @Composable () -> Unit,
  confirmButton: @Composable (String) -> Unit,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  startingText: String = "",
  textStyle: TextStyle = LocalTextStyle.current,
  label: @Composable (() -> Unit)? = null,
  isError: Boolean = false,
  errorMessage: String = "",
) {
  BasicAlertDialog(onDismissRequest = onDismissRequest) {
    Card(modifier.heightIn(0.dp, 250.dp).clip(MaterialTheme.shapes.extraLarge)) {
      var name by rememberSaveable { mutableStateOf(startingText) }
      OutlinedTextField(
        modifier =
          Modifier.padding(start = Dp.ExtraLarge, end = Dp.ExtraLarge, top = Dp.ExtraLarge),
        value = name,
        onValueChange = { name = it },
        label = label,
        maxLines = 4,
        isError = isError,
        supportingText = { if (isError) Text(errorMessage) },
        textStyle = textStyle,
      )

      Spacer(Modifier.weight(1F))
      Row(modifier = Modifier.padding(end = Dp.Large, bottom = Dp.ExtraLarge)) {
        Spacer(Modifier.weight(1F))
        dismissButton()
        confirmButton(name)
      }
    }
  }
}
