package io.github.depermitto.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import kotlin.math.roundToInt

@Composable
fun NumberField(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(5.dp),
) {
    OutlinedTextField(
        modifier = modifier,
        value = value.parseToNumericInput(),
        onValueChange = { onValueChange(it.parseFromNumericInput()) },
        label = { Text(text = label) },
        singleLine = singleLine,
        readOnly = readOnly,
        contentPadding = contentPadding,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

fun String.parseFromNumericInput(): Float = this.takeIf { it.isNotBlank() && it.isDigitsOnly() }?.toFloat() ?: 0f
fun Float.parseToNumericInput(): String = this.roundToInt().takeUnless { it == 0 }?.toString() ?: ""
