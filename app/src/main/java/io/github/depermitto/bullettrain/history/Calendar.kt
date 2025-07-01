package io.github.depermitto.bullettrain.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import io.github.depermitto.bullettrain.util.getDate
import io.github.depermitto.bullettrain.util.toTimestamp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Calendar(
  currentDate: LocalDate,
  currentRecords: List<HistoryRecord>,
  modifier: Modifier = Modifier,
  homeViewModel: HomeViewModel,
  trainViewModel: TrainViewModel,
  programDao: ProgramDao,
) {
  Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
    val today = LocalDate.now()
    var days =
      generateSequence(-currentDate.withDayOfMonth(1).dayOfWeek.value + 2) {
        if (it < currentDate.month.length(currentDate.isLeapYear)) it + 1 else null
      }

    var longClickedDate by rememberSaveable { mutableStateOf(today) }
    var showProgramListDialog by rememberSaveable { mutableStateOf(false) }
    Column {
      repeat(if (days.count() < 36) 6 else 7) { i ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          repeat(7) { j ->
            var showDropdown by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1F)) {
              if (i == 0) {
                CalendarItem(
                  textAlpha = 0.6F,
                  text =
                    DayOfWeek.of(j + 1)
                      .getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault()),
                )
                return@Box
              }

              val dayOfMonth: Int? = days.firstOrNull()?.takeUnless { it <= 0 }
              if (dayOfMonth == null) {
                CalendarItem(text = "")
              } else {
                val day = LocalDate.of(currentDate.year, currentDate.month, dayOfMonth)
                CalendarItem(
                  modifier =
                    Modifier.padding(4.dp)
                      .clip(shape = CircleShape)
                      .aspectRatio(1F)
                      .combinedClickable(
                        onClick = { homeViewModel.selectedDate = day },
                        onLongClick = {
                          longClickedDate = day
                          showDropdown = false
                          showProgramListDialog = true
                        },
                      ),
                  backgroundColor =
                    when {
                      homeViewModel.selectedDate == day ->
                        MaterialTheme.colorScheme.tertiaryContainer

                      currentRecords.any { it.getDate() == day } ->
                        MaterialTheme.colorScheme.primaryContainer

                      else -> Color.Transparent
                    },
                  underline = day == today,
                  text = dayOfMonth.toString(),
                )
              }
              days = days.drop(1)
            }
          }
        }
      }
    }

    val programs by
      programDao.getPerformable.collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedProgram: Program? by
      rememberSaveable(
        saver =
          Saver(
            save = { original -> original.value?.toByteString() },
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

@Composable
private fun CalendarItem(
  modifier: Modifier = Modifier,
  text: String,
  textAlpha: Float = 1F,
  underline: Boolean = false,
  backgroundColor: Color = Color.Unspecified,
) {
  Box(modifier = modifier.background(backgroundColor)) {
    Text(
      text = text,
      color =
        MaterialTheme.colorScheme
          .contentColorFor(backgroundColor)
          .takeOrElse { MaterialTheme.colorScheme.onBackground }
          .copy(alpha = textAlpha),
      modifier = Modifier.align(Alignment.Center).padding(10.dp),
      textAlign = TextAlign.Center,
      textDecoration = if (underline) TextDecoration.Underline else null,
      maxLines = 1,
    )
  }
}
