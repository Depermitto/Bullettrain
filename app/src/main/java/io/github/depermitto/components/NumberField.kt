package io.github.depermitto.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.KeyboardOptions
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
    label: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    trailingText: String? = null,
    contentPadding: PaddingValues = PaddingValues(3.dp),
) {
    OutlinedTextField(
        modifier = modifier,
        value = with(value.parseToNumericInput()) {
            if (isNotBlank() && (trailingText != null)) "$this $trailingText" else this
        },
        onValueChange = {
            onValueChange((if (trailingText != null) it.replace(trailingText, "") else it).parseFromNumericInput())
        },
        label = label,
        singleLine = singleLine,
        readOnly = readOnly,
        contentPadding = contentPadding,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

fun Float.parseToNumericInput(): String = this.roundToInt().takeUnless { it == 0 }?.toString() ?: ""
fun String.parseFromNumericInput(): Float =
    this.filterNot { it in " -.," }.takeIf { it.isNotBlank() && it.isDigitsOnly() }?.toFloat() ?: 0f
