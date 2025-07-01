package io.github.depermitto.bullettrain.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.graphics.ColorUtils

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

@Composable
fun transparentTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    errorContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent
)

@Composable
fun notUnderlinedTextFieldColors() = TextFieldDefaults.colors(
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent
)

@Composable
fun filledContainerColor() = if (isSystemInDarkTheme()) Color(
    ColorUtils.blendARGB(
        MaterialTheme.colorScheme.background.toArgb(), MaterialTheme.colorScheme.surfaceBright.toArgb(), 0.5F
    )
)
else Color(
    ColorUtils.blendARGB(
        MaterialTheme.colorScheme.background.toArgb(), MaterialTheme.colorScheme.surfaceDim.toArgb(), 0.5F
    )
)

@Composable
fun numberFieldTextStyle(color: Color = LocalContentColor.current, fontWeight: FontWeight = FontWeight.Normal) =
    TextStyle(LocalContentColor.current).copy(
        color = color,
        textAlign = TextAlign.Center,
        fontStyle = MaterialTheme.typography.bodyLarge.fontStyle,
        fontWeight = fontWeight
    )
