package io.github.depermitto.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun NumberField(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    label: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(3.dp),
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value.encodeToStringOutput())) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) {
        val endRange = if (isFocused) textFieldValue.text.length else 0
        textFieldValue = textFieldValue.copy(selection = TextRange(start = 0, end = endRange))
    }

    OutlinedTextField(
        modifier = modifier,
        value = textFieldValue,
        onValueChange = {
            val validNumber = it.text.parseFromNumericInput()
            if (!it.text.contains(" ") && validNumber != null) {
                textFieldValue = it
                onValueChange(validNumber)
            }
        },
        label = label,
        singleLine = singleLine,
        readOnly = readOnly,
        contentPadding = contentPadding,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        interactionSource = interactionSource
    )
}


fun Float.stripTrailingZeros(): Int? = takeIf { it == it.roundToInt().toFloat() }?.roundToInt()
fun Float.encodeToStringOutput(): String {
    if (this == 0f) return ""
    return stripTrailingZeros()?.toString() ?: this.toString()
}
fun String.parseFromNumericInput(): Float? = if (isBlank()) 0f else runCatching { toFloatOrNull() }.getOrNull()
