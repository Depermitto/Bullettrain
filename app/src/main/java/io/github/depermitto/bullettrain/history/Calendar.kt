package io.github.depermitto.bullettrain.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@Composable
fun Calendar(
    date: LocalDate,
    modifier: Modifier = Modifier,
    onItemClick: (LocalDate) -> Unit,
    ifHighlightItem: (LocalDate) -> Boolean,
) = OutlinedCard(modifier = modifier) {
    val today = LocalDate.now()
    val firstDayOfMonth = LocalDate.of(date.year, date.month, 1)
    var days = generateSequence(-firstDayOfMonth.dayOfWeek.value + 2) {
        if (it < date.month.length(date.isLeapYear)) it + 1
        else null
    }
    var selectedDay: LocalDate? by rememberSaveable { mutableStateOf(null) }

    Column {
        repeat(if (days.count() < 36) 6 else 7) { i ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(7) { j ->
                    if (i == 0) CalendarItem(
                        modifier = Modifier.weight(1f),
                        text = DayOfWeek.of(j + 1).getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault()),
                        textAlpha = 0.6f,
                    ) else {
                        val dayOfMonth: Int? = days.firstOrNull()?.takeUnless { it <= 0 }
                        if (dayOfMonth == null) {
                            CalendarItem(modifier = Modifier.weight(1f), text = "")
                        } else {
                            val day = LocalDate.of(date.year, date.month, dayOfMonth)

                            CalendarItem(modifier = Modifier
                                .weight(1f)
                                .padding(4.dp)
                                .clip(shape = CircleShape)
                                .aspectRatio(1f)
                                .clickable { onItemClick(day); selectedDay = day }
                                .background(
                                    color = if (day == selectedDay) MaterialTheme.colorScheme.tertiaryContainer
                                    else if (ifHighlightItem(day)) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                ), underline = day == today, text = dayOfMonth.toString())
                        }
                        days = days.drop(1)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarItem(
    modifier: Modifier = Modifier,
    text: String,
    textAlpha: Float = 1f,
    underline: Boolean = false,
) = Box(modifier = modifier) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = textAlpha),
        modifier = Modifier
            .align(Alignment.Center)
            .padding(10.dp),
        textAlign = TextAlign.Center,
        textDecoration = if (underline) TextDecoration.Underline else null,
        maxLines = 1,
    )
}
