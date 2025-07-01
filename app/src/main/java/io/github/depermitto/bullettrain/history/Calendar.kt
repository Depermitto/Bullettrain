package io.github.depermitto.bullettrain.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
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
import io.github.depermitto.bullettrain.util.toTimestamp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.*

@Composable
fun Calendar(
  date: YearMonth,
  recordsSorted: List<HistoryRecord>,
  modifier: Modifier = Modifier,
  homeViewModel: HomeViewModel,
  trainViewModel: TrainViewModel,
  programDao: ProgramDao,
) {
  Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
    val today = LocalDate.now()
    var dayOfMonth = -date.atDay(1).dayOfWeek.value + 2

    var longClickedDate by rememberSaveable { mutableStateOf(today) }
    var showProgramListDialog by rememberSaveable { mutableStateOf(false) }
    Column {
      for (row in 0..<7) {
        Row {
          for (col in 0..<7) {
            if (row == 0) {
              CalendarItem(
                text = DayOfWeek.of(col + 1).getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                modifier = Modifier.weight(1F),
                textAlpha = 0.6F,
              )
              continue
            }

            val day =
              when {
                dayOfMonth <= 0 -> {
                  val month = date.month - 1
                  LocalDate.of(date.year, month, month.length(date.isLeapYear) + dayOfMonth)
                }
                dayOfMonth > date.month.length(date.isLeapYear) -> {
                  val month = date.month + 1
                  LocalDate.of(date.year, month, dayOfMonth - date.month.length(date.isLeapYear))
                }
                else -> LocalDate.of(date.year, date.month, dayOfMonth)
              }
            dayOfMonth++

            val alpha = if (day.month == date.month) 1F else 0.3F
            CalendarItem(
              text = day.dayOfMonth.toString(),
              onClick = { homeViewModel.selectedDate = day },
              onLongClick = {
                longClickedDate = day
                showProgramListDialog = true
              },
              backgroundColor =
                when {
                  homeViewModel.selectedDate == day ->
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = alpha)

                  recordsSorted.binarySearch { ChronoUnit.DAYS.between(it.date, day).toInt() } >=
                    0 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = alpha)

                  else -> Color.Transparent
                },
              underline = day == today,
              modifier = Modifier.weight(1F),
              textAlpha = alpha,
            )
          }
        }
        if (dayOfMonth > date.month.length(date.isLeapYear)) break
      }
    }

    val programs by
      programDao.getPerformable.collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedProgram: Program? by
      rememberSaveable(
        saver =
          Saver(
            save = { original -> original.value?.toByteArray() },
            restore = { saveable -> mutableStateOf(Program.parseFrom(saveable)) },
          ),
        init = { mutableStateOf(null) },
      )

    if (showProgramListDialog)
      ListAlertDialog(
        title = "Start a workout on ${longClickedDate.format(DateFormatters.MMMM_d_yyyy)} from",
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
              longClickedDate.atTimeNow().toTimestamp(),
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
          trainViewModel.startWorkout(day, program.id, longClickedDate.atTimeNow().toTimestamp())
        },
      ) { day ->
        ExtendedListItem(
          headlineContent = { Text(day.name, maxLines = 2, overflow = TextOverflow.Ellipsis) }
        )
      }
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
  textAlpha: Float = 1F,
  underline: Boolean = false,
  backgroundColor: Color = Color.Unspecified,
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
      text = text,
      color =
        MaterialTheme.colorScheme
          .contentColorFor(backgroundColor)
          .takeOrElse { MaterialTheme.colorScheme.onBackground }
          .copy(alpha = textAlpha),
      modifier = Modifier.padding(10.dp),
      textAlign = TextAlign.Center,
      textDecoration = if (underline) TextDecoration.Underline else null,
      maxLines = 1,
    )
  }
}
