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
    label: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(3.dp),
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value.stringFromNumeric())) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) {
        val endRange = if (isFocused) textFieldValue.text.length else 0
        textFieldValue = textFieldValue.copy(selection = TextRange(start = 0, end = endRange))
    }

    OutlinedTextField(
        modifier = modifier,
        value = textFieldValue,
        onValueChange = { if (!it.text.contains(" ") && it.text.parseFromNumericInput() != null) textFieldValue = it },
        label = label,
        singleLine = singleLine,
        readOnly = readOnly,
        contentPadding = contentPadding,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        interactionSource = interactionSource
    )
}


fun Float.stringFromNumeric(): String = this.roundToInt().takeUnless { it == 0 }?.toString() ?: ""
fun String.parseFromNumericInput(): Float? = if (isBlank()) 0f else runCatching { toFloatOrNull() }.getOrNull()
