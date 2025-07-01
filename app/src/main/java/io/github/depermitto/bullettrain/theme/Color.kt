package io.github.depermitto.bullettrain.theme

import androidx.compose.material3.ColorScheme
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
import io.github.depermitto.bullettrain.database.Theme


@Composable
fun TextFieldDefaults.unlinedColors() = colors(
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent
)

@Composable
fun ColorScheme.focalGround(theme: Theme) = if (theme.isDarkMode()) Color(
    ColorUtils.blendARGB(
        MaterialTheme.colorScheme.background.toArgb(), MaterialTheme.colorScheme.surfaceBright.toArgb(), 0.4F
    )
)
else Color(
    ColorUtils.blendARGB(
        MaterialTheme.colorScheme.background.toArgb(), MaterialTheme.colorScheme.surfaceDim.toArgb(), 0.6F
    )
)

@Composable
fun TextStyle.Companion.numeric(color: Color = LocalContentColor.current, fontWeight: FontWeight = FontWeight.Normal) =
    TextStyle(color).copy(
        textAlign = TextAlign.Center, fontStyle = MaterialTheme.typography.bodyLarge.fontStyle, fontWeight = fontWeight
    )


val primaryLight = Color(0xFF32628D)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFD0E4FF)
val onPrimaryContainerLight = Color(0xFF001D34)
val secondaryLight = Color(0xFF7E570F)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFFFDDB0)
val onSecondaryContainerLight = Color(0xFF281800)
val tertiaryLight = Color(0xFF874B6D)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFFFD8EA)
val onTertiaryContainerLight = Color(0xFF370727)
val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF410002)
val backgroundLight = Color(0xFFF8F9FF)
val onBackgroundLight = Color(0xFF191C20)
val surfaceLight = Color(0xFFF8F9FF)
val onSurfaceLight = Color(0xFF191C20)
val surfaceVariantLight = Color(0xFFDEE3EB)
val onSurfaceVariantLight = Color(0xFF42474E)
val outlineLight = Color(0xFF73777F)
val outlineVariantLight = Color(0xFFC2C7CF)
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF2D3135)
val inverseOnSurfaceLight = Color(0xFFEFF1F6)
val inversePrimaryLight = Color(0xFF9DCAFC)
val surfaceDimLight = Color(0xFFD8DAE0)
val surfaceBrightLight = Color(0xFFF8F9FF)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFF2F3F9)
val surfaceContainerLight = Color(0xFFECEEF4)
val surfaceContainerHighLight = Color(0xFFE6E8EE)
val surfaceContainerHighestLight = Color(0xFFE0E2E8)

val primaryDark = Color(0xFF9DCAFC)
val onPrimaryDark = Color(0xFF003355)
val primaryContainerDark = Color(0xFF144A74)
val onPrimaryContainerDark = Color(0xFFD0E4FF)
val secondaryDark = Color(0xFFF2BE6E)
val onSecondaryDark = Color(0xFF442C00)
val secondaryContainerDark = Color(0xFF614000)
val onSecondaryContainerDark = Color(0xFFFFDDB0)
val tertiaryDark = Color(0xFFFBB0D7)
val onTertiaryDark = Color(0xFF511D3D)
val tertiaryContainerDark = Color(0xFF6C3454)
val onTertiaryContainerDark = Color(0xFFFFD8EA)
val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)
val backgroundDark = Color(0xFF101418)
val onBackgroundDark = Color(0xFFE0E2E8)
val surfaceDark = Color(0xFF101418)
val onSurfaceDark = Color(0xFFE0E2E8)
val surfaceVariantDark = Color(0xFF42474E)
val onSurfaceVariantDark = Color(0xFFC2C7CF)
val outlineDark = Color(0xFF8C9199)
val outlineVariantDark = Color(0xFF42474E)
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFE0E2E8)
val inverseOnSurfaceDark = Color(0xFF2D3135)
val inversePrimaryDark = Color(0xFF32628D)
val surfaceDimDark = Color(0xFF101418)
val surfaceBrightDark = Color(0xFF36393E)
val surfaceContainerLowestDark = Color(0xFF0B0E12)
val surfaceContainerLowDark = Color(0xFF191C20)
val surfaceContainerDark = Color(0xFF1D2024)
val surfaceContainerHighDark = Color(0xFF272A2F)
val surfaceContainerHighestDark = Color(0xFF32353A)