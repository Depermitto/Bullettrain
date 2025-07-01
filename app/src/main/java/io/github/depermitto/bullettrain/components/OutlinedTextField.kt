package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import io.github.depermitto.bullettrain.theme.numeric

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinedTextField(
  state: TextFieldState,
  modifier: Modifier = Modifier,
  textStyle: TextStyle = TextStyle.numeric(),
  inputTransformation: InputTransformation? = null,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  singleLine: Boolean = true,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  label: @Composable (() -> Unit)? = null,
  placeholder: @Composable (() -> Unit)? = null,
  keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
  keyboardActionHandler: KeyboardActionHandler? = null,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
  cursorBrush: Brush = SolidColor(MaterialTheme.colorScheme.primary),
  focusedBorderThickness: Dp = OutlinedTextFieldDefaults.FocusedBorderThickness,
  unfocusedBorderThickness: Dp = OutlinedTextFieldDefaults.UnfocusedBorderThickness,
  contentPadding: PaddingValues,
) {
  BasicTextField(
    state = state,
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    textStyle = textStyle,
    inputTransformation = inputTransformation,
    keyboardOptions = keyboardOptions,
    onKeyboardAction = keyboardActionHandler,
    interactionSource = interactionSource,
    cursorBrush = cursorBrush,
    decorator = { innerTextField ->
      OutlinedTextFieldDefaults.DecorationBox(
        value = state.text.toString(),
        visualTransformation = visualTransformation,
        label = label,
        placeholder = placeholder,
        innerTextField = innerTextField,
        colors = colors,
        singleLine = singleLine,
        enabled = enabled,
        interactionSource = interactionSource,
        contentPadding = contentPadding,
        container = {
          OutlinedTextFieldDefaults.Container(
            enabled = enabled,
            isError = false,
            interactionSource = interactionSource,
            colors = colors,
            focusedBorderThickness = focusedBorderThickness,
            unfocusedBorderThickness = unfocusedBorderThickness,
          )
        },
      )
    },
  )
}

@Composable
fun Placeholder(text: String) {
  Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
    Text(
      text,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.40F),
      maxLines = 1,
    )
  }
}
