package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    headlineContent: @Composable () -> Unit,
    onClick: (() -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    headlineTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
) = Box(if (onClick == null) Modifier else modifier.clickable { onClick() }) {
    ConstraintLayout(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        val (headline, supporting, trailing) = createRefs()
        createHorizontalChain(headline, trailing, chainStyle = ChainStyle.SpreadInside)

        CompositionLocalProvider(
            LocalContentColor provides ListItemDefaults.colors().headlineColor,
            LocalTextStyle provides LocalTextStyle.current.merge(headlineTextStyle)
        ) {
            Box(Modifier.constrainAs(headline) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                width = Dimension.preferredWrapContent
            }) {
                headlineContent()
            }
        }

        if (supportingContent != null) {
            CompositionLocalProvider(
                LocalContentColor provides ListItemDefaults.colors().supportingTextColor,
                LocalTextStyle provides LocalTextStyle.current.merge(MaterialTheme.typography.bodyMedium)
            ) {
                Box(Modifier.constrainAs(supporting) {
                    start.linkTo(parent.start)
                    top.linkTo(headline.bottom)
                }) {
                    supportingContent()
                }
            }
        }

        if (trailingContent != null) {
            Box(Modifier
                .constrainAs(trailing) {
                    end.linkTo(parent.end)
                    centerVerticallyTo(parent)
                }
                .padding(start = 16.dp)) {
                trailingContent()
            }
        }
    }
}