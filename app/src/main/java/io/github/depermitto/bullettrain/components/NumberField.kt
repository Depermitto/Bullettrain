package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.placeCursorAtEnd
import androidx.compose.foundation.text.input.selectAll
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.depermitto.bullettrain.theme.numeric
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun NumberField(
  modifier: Modifier = Modifier,
  value: Float,
  onValueChange: (Float) -> Unit,
  floatingPoint: Boolean,
  label: @Composable (() -> Unit)? = null,
  placeholder: @Composable (() -> Unit)? = null,
  singleLine: Boolean = true,
  enabled: Boolean = true,
  textStyle: TextStyle = TextStyle.numeric(),
  colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
  focusedBorderThickness: Dp = OutlinedTextFieldDefaults.FocusedBorderThickness,
  unfocusedBorderThickness: Dp = OutlinedTextFieldDefaults.UnfocusedBorderThickness,
  contentPadding: PaddingValues = PaddingValues(3.dp),
) {
  val valueFormatted = rememberSaveable(value) { value.format() }
  val textFieldState =
    rememberSaveable(valueFormatted, saver = TextFieldState.Saver) {
      TextFieldState(initialText = valueFormatted)
    }

  LaunchedEffect(enabled) {
    textFieldState.edit {
      if (!enabled) {
        if (hasSelection) {
          selection = TextRange.Zero
          placeCursorAtEnd()
        }

        // Autocomplete empty fields
        if (value == 0F) {
          replace(0, length, "0")
        } else if (length == 0 && toString() != valueFormatted) {
          replace(0, length, valueFormatted)
        }
      } else if (value == 0F) {
        delete(0, length)
      }
    }
  }

  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()
  LaunchedEffect(isFocused) { if (isFocused) textFieldState.edit { selectAll() } }

  val regex = remember { if (floatingPoint) Regex("""\d+\.?\d*""") else Regex("""\d+""") }
  OutlinedTextField(
    modifier = modifier,
    state = textFieldState,
    inputTransformation = {
      when {
        length == 0 -> onValueChange(0F)
        regex.matches(toString()) -> onValueChange(toString().toFloat())
        else -> revertAllChanges()
      }
    },
    textStyle = textStyle,
    label = label,
    placeholder = placeholder,
    singleLine = singleLine,
    readOnly = false,
    enabled = enabled,
    colors = colors,
    contentPadding = contentPadding,
    keyboardOptions =
      KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
    interactionSource = interactionSource,
    focusedBorderThickness = focusedBorderThickness,
    unfocusedBorderThickness = unfocusedBorderThickness,
  )
}

fun Float.format(): String {
  if (this == 0F) return ""

  return if (this == this.roundToInt().toFloat()) {
    this.roundToInt().toString()
  } else {
    String.format(Locale.getDefault(), "%.1f", this)
  }
}
