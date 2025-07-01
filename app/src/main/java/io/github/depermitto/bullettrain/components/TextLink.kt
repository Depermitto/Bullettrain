package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destination

@Composable
fun TextLink(
  text: String,
  navController: NavController,
  destination: Destination,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
  style: TextStyle = LocalTextStyle.current,
  fontSize: TextUnit = TextUnit.Unspecified,
  fontStyle: FontStyle? = null,
  fontWeight: FontWeight? = null,
  fontFamily: FontFamily? = null,
  letterSpacing: TextUnit = TextUnit.Unspecified,
  textDecoration: TextDecoration? = null,
  textAlign: TextAlign? = null,
  lineHeight: TextUnit = TextUnit.Unspecified,
  overflow: TextOverflow = TextOverflow.Ellipsis,
  softWrap: Boolean = true,
  maxLines: Int = 2,
  minLines: Int = 1,
) {
  Card(
    modifier = modifier,
    onClick = { navController.navigate((destination)) },
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    content = {
      Text(
        text = text,
        modifier = Modifier.padding(contentPadding),
        style = style,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
      )
    },
  )
}
