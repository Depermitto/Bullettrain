package io.github.depermitto.bullettrain.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.components.ExtendedListItem
import io.github.depermitto.bullettrain.components.ListAlertDialog
import io.github.depermitto.bullettrain.db.ProgramDao
import io.github.depermitto.bullettrain.home.HomeViewModel
import io.github.depermitto.bullettrain.protos.HistoryProto.*
import io.github.depermitto.bullettrain.protos.ProgramsProto.*
import io.github.depermitto.bullettrain.train.TrainViewModel
import io.github.depermitto.bullettrain.util.DateFormatters
import io.github.depermitto.bullettrain.util.atTimeNow
import io.github.depermitto.bullettrain.util.date
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.*

@Composable
fun Calendar(
  date: YearMonth,
  records: List<HistoryRecord>,
  modifier: Modifier = Modifier,
  homeViewModel: HomeViewModel,
  trainViewModel: TrainViewModel,
  programDao: ProgramDao,
  navController: NavController,
) {
  val today = LocalDate.now()
  val programs by programDao.getPerformable.collectAsStateWithLifecycle(emptyList())

  var longClickedDate by rememberSaveable { mutableStateOf(today) }
  var showProgramListDialog by rememberSaveable { mutableStateOf(false) }
  Column(modifier = modifier) {
    val days = generateDays(date).iterator()
    for (row in 0..<7) {
      Row {
        for (col in 0..<7) {
          if (row == 0) {
            CalendarItem(
              text = DayOfWeek.of(col + 1).getDisplayName(TextStyle.NARROW, Locale.getDefault()),
              modifier = Modifier.weight(1F),
              textColor = LocalContentColor.current.copy(alpha = 0.6F),
            )
            continue
          }

          val day = days.next()

          val alpha = if (day.month == date.month) 1F else 0.3F
          val (backgroundColor, textColor) =
            when {
              homeViewModel.selectedDate == day ->
                Pair(
                  MaterialTheme.colorScheme.primary.copy(alpha = alpha + 0.2F),
                  MaterialTheme.colorScheme.onPrimary,
                )

              records.binarySearch { ChronoUnit.DAYS.between(it.date, day).toInt() } >= 0 ->
                Pair(
                  MaterialTheme.colorScheme.secondary.copy(alpha = alpha),
                  MaterialTheme.colorScheme.onSecondary,
                )

              else -> Pair(Color.Unspecified, LocalContentColor.current)
            }

          CalendarItem(
            text = day.dayOfMonth.toString(),
            textColor = textColor,
            onClick = { homeViewModel.selectedDate = day },
            onLongClick = {
              if (programs.isNotEmpty()) {
                longClickedDate = day
                showProgramListDialog = true
              }
            },
            backgroundColor = backgroundColor,
            underline = day == today,
            modifier = Modifier.weight(1F),
          )
        }
      }
      if (!days.hasNext()) break
    }
  }

  var selectedProgram: Program? by remember { mutableStateOf(null) }

  if (showProgramListDialog)
    ListAlertDialog(
      title = "Starting a workout on ${DateFormatters.MMM_dd.format(longClickedDate)}...",
      onDismissRequest = { showProgramListDialog = false },
      list = programs,
      dismissButton = {
        TextButton(onClick = { showProgramListDialog = false }) { Text("Cancel") }
      },
      onClick = { program ->
        showProgramListDialog = false
        if (program.id == -1) {
          trainViewModel.startWorkout(
            Workout.getDefaultInstance(),
            -1,
            longClickedDate.atTimeNow(),
            navController,
          )
        } else {
          selectedProgram = program
        }
      },
    ) { program ->
      ExtendedListItem(
        headlineContent = { Text(program.name, maxLines = 2, overflow = TextOverflow.Ellipsis) }
      )
    }

  selectedProgram?.let { program ->
    ListAlertDialog(
      title = "Which one would you like to perform?",
      onDismissRequest = { selectedProgram = null },
      dismissButton = { TextButton(onClick = { selectedProgram = null }) { Text("Cancel") } },
      list = program.workoutsList,
      onClick = { day ->
        selectedProgram = null
        trainViewModel.startWorkout(day, program.id, longClickedDate.atTimeNow(), navController)
      },
    ) { day ->
      ExtendedListItem(
        headlineContent = { Text(day.name, maxLines = 2, overflow = TextOverflow.Ellipsis) }
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CalendarItem(
  modifier: Modifier = Modifier,
  text: String,
  onClick: (() -> Unit)? = null,
  onLongClick: (() -> Unit)? = null,
  backgroundColor: Color = Color.Unspecified,
  textColor: Color = contentColorFor(backgroundColor),
  underline: Boolean = false,
) {
  Box(modifier = modifier.wrapContentSize().padding(4.dp), contentAlignment = Alignment.Center) {
    Surface(
      modifier = Modifier.aspectRatio(1F),
      shape = CircleShape,
      color = backgroundColor,
      content = {
        if (onClick != null) {
          Box(Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick))
        }
      },
    )
    Text(
      text,
      color = textColor.takeOrElse { MaterialTheme.colorScheme.onBackground },
      modifier = Modifier.padding(10.dp),
      textAlign = TextAlign.Center,
      textDecoration = if (underline) TextDecoration.Underline else null,
      maxLines = 1,
    )
  }
}

/**
 * Generate all visible days on the calendar for the given [YearMonth]. The amount of dates
 * generated is always divisible by 7. This function never fails.
 */
fun generateDays(date: YearMonth): List<LocalDate> {
  val dates = mutableListOf<LocalDate>()
  var dayOfMonth = -date.atDay(1).dayOfWeek.value + 2
  while (dayOfMonth <= date.month.length(date.isLeapYear)) {
    for (j in 0..<7) {
      val day =
        when {
          dayOfMonth <= 0 -> {
            val ym = date.minusMonths(1)
            LocalDate.of(ym.year, ym.month, ym.month.length(date.isLeapYear) + dayOfMonth)
          }

          dayOfMonth > date.month.length(date.isLeapYear) -> {
            val ym = date.plusMonths(1)
            LocalDate.of(ym.year, ym.month, dayOfMonth - date.month.length(date.isLeapYear))
          }

          else -> LocalDate.of(date.year, date.month, dayOfMonth)
        }
      dates.add(day)
      dayOfMonth++
    }
  }
  return dates
}
