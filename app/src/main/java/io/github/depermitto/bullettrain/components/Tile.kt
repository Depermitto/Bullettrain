package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension

/**
 * Similar to [androidx.compose.material3.ListItem] but with rectangular shape, customizable padding and no leading content.
 * @see [RadioTile]
 */
@Composable
fun HeroTile(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    headlineContent: @Composable () -> Unit,
    supportingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    headlineTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    supportingTextStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    contentPadding: PaddingValues = PaddingValues(vertical = 12.dp, horizontal = 16.dp)
) = Box(if (onClick == null) Modifier else modifier.clickable { onClick() }) {
    ConstraintLayout(
        Modifier
            .fillMaxWidth()
            .padding(contentPadding)
    ) {
        val (headline, supporting, trailing) = createRefs()
        createHorizontalChain(headline, trailing, chainStyle = ChainStyle.SpreadInside)

        CompositionLocalProvider(
            LocalContentColor provides ListItemDefaults.colors().headlineColor,
            LocalTextStyle provides LocalTextStyle.current.merge(headlineTextStyle)
        ) {
            Box(Modifier.constrainAs(headline) {
                top.linkTo(parent.top)
                width = Dimension.preferredWrapContent
            }) {
                headlineContent()
            }
        }

        if (supportingContent != null) {
            CompositionLocalProvider(
                LocalContentColor provides ListItemDefaults.colors().supportingTextColor,
                LocalTextStyle provides LocalTextStyle.current.merge(supportingTextStyle)
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
                .padding(start = 4.dp)
                .constrainAs(trailing) {
                    end.linkTo(parent.end)
                    centerVerticallyTo(parent)
                }) {
                trailingContent()
            }
        }
    }
}

/**
 * Similar to [androidx.compose.material3.ListItem] but with a [androidx.compose.material3.RadioButton] as leading content.
 * @see [HeroTile]
 */
@Composable
fun RadioTile(
    modifier: Modifier = Modifier,
    selected: Boolean,
    headlineContent: @Composable () -> Unit,
    supportingContent: (@Composable () -> Unit)? = null,
    headlineTextStyle: TextStyle = MaterialTheme.typography.bodyLarge
) = ConstraintLayout(modifier) {
    val (leading, headline, supporting) = createRefs()

    RadioButton(selected = selected, onClick = null, modifier = Modifier.constrainAs(leading) {
        centerVerticallyTo(parent)
        start.linkTo(parent.start, 10.dp)
    })

    CompositionLocalProvider(
        LocalContentColor provides ListItemDefaults.colors().headlineColor,
        LocalTextStyle provides LocalTextStyle.current.merge(headlineTextStyle)
    ) {
        Box(Modifier.constrainAs(headline) {
            start.linkTo(leading.end, 24.dp)
            if (supportingContent == null) centerVerticallyTo(parent) else top.linkTo(parent.top)
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
                start.linkTo(headline.start)
                top.linkTo(headline.bottom)
            }) {
                supportingContent()
            }
        }
    }
}
