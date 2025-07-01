package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.depermitto.bullettrain.theme.numberFieldTextStyle
import kotlin.math.roundToInt

@Composable
fun NumberField(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    textStyle: TextStyle = numberFieldTextStyle(),
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    focusedBorderThickness: Dp = OutlinedTextFieldDefaults.FocusedBorderThickness,
    unfocusedBorderThickness: Dp = OutlinedTextFieldDefaults.UnfocusedBorderThickness,
    contentPadding: PaddingValues = PaddingValues(3.dp),
) {
    val textValue = if (value == 0f) "" else value.toString().removeSuffix(".0")

    var textFieldValue by remember { mutableStateOf(TextFieldValue(textValue)) }
    if (textFieldValue.text != "-" && value != textFieldValue.text.toFloatOrNull()) {
        textFieldValue = textFieldValue.copy(text = textValue)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) {
        if (isFocused) textFieldValue = textFieldValue.copy(selection = TextRange(0, textFieldValue.text.length))
    }
    if (!enabled) textFieldValue = textFieldValue.copy(selection = TextRange(textFieldValue.text.length))

    OutlinedTextField(
        modifier = modifier,
        value = textFieldValue,
        onValueChange = { it ->
            if (it.text.contains(" ")) return@OutlinedTextField

            textFieldValue = if (it.text == ".") textFieldValue.copy(text = "0.", selection = TextRange(2))
            else it

            if (it.text.isBlank()) onValueChange(0f)
            else it.text.toFloatOrNull()?.let { value -> onValueChange(value) }
        },
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        singleLine = singleLine,
        readOnly = readOnly || !enabled,
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        interactionSource = interactionSource,
        focusedBorderThickness = focusedBorderThickness,
        unfocusedBorderThickness = unfocusedBorderThickness,
    )
}


fun Float.encodeToStringOutput(): String {
    if (this == 0f) return ""
    return takeIf { it == it.roundToInt().toFloat() }?.roundToInt()?.toString() ?: this.toString()
}