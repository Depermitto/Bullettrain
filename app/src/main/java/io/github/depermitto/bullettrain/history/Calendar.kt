package io.github.depermitto.bullettrain.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.depermitto.bullettrain.components.ListAlertDialog
import io.github.depermitto.bullettrain.components.HeroTile
import io.github.depermitto.bullettrain.database.entities.*
import io.github.depermitto.bullettrain.home.HomeViewModel
import io.github.depermitto.bullettrain.train.TrainViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Calendar(
    currentDate: LocalDate,
    currentHistoryRecords: List<HistoryRecord>,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel,
    trainViewModel: TrainViewModel,
    programDao: ProgramDao,
    historyDao: HistoryDao,
) = Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
    val today = LocalDate.now()
    var days = generateSequence(-currentDate.withDayOfMonth(1).dayOfWeek.value + 2) {
        if (it < currentDate.month.length(currentDate.isLeapYear)) it + 1
        else null
    }

    var longClickedDate by rememberSaveable { mutableStateOf(today) }
    var showProgramListDialog by rememberSaveable { mutableStateOf(false) }
    var showWorkoutDiscardDialog by rememberSaveable { mutableStateOf(false) }
    Column {
        repeat(if (days.count() < 36) 6 else 7) { i ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(7) { j ->
                    var showDropdown by rememberSaveable { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        if (i == 0) {
                            CalendarItem(
                                textAlpha = 0.6f,
                                text = DayOfWeek.of(j + 1).getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault()),
                            )
                            return@Box
                        }

                        val dayOfMonth: Int? = days.firstOrNull()?.takeUnless { it <= 0 }
                        if (dayOfMonth == null) {
                            CalendarItem(text = "")
                        } else {
                            val day = LocalDate.of(currentDate.year, currentDate.month, dayOfMonth)
                            CalendarItem(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(shape = CircleShape)
                                    .aspectRatio(1f)
                                    .combinedClickable(onClick = { homeViewModel.selectedDate = day },
                                        onLongClick = { showDropdown = true })
                                    .background(
                                        color = if (homeViewModel.selectedDate == day) MaterialTheme.colorScheme.tertiaryContainer
                                        else if (currentHistoryRecords.any { it.date == day }) MaterialTheme.colorScheme.primaryContainer
                                        else Color.Transparent
                                    ), underline = day == today, text = dayOfMonth.toString()
                            )

                            DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false }) {
                                DropdownMenuItem(
                                    text = { Text("Start a workout on $day") },
                                    onClick = { longClickedDate = day; showDropdown = false; showProgramListDialog = true },
                                )

                                if (currentHistoryRecords.any { it.date == day }) DropdownMenuItem(
                                    text = { Text("Discard a workout") },
                                    onClick = { longClickedDate = day; showDropdown = false; showWorkoutDiscardDialog = true },
                                )
                            }
                        }
                        days = days.drop(1)
                    }
                }
            }
        }
    }

    val programs by programDao.getPerformable.collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedProgram: Program? by rememberSaveable { mutableStateOf(null) }

    if (showProgramListDialog) ListAlertDialog(title = "Which program does the workout belong to?",
        onDismissRequest = { showProgramListDialog = false },
        list = programs,
        dismissButton = { TextButton(onClick = { showProgramListDialog = false }) { Text("Cancel") } },
        onClick = { program ->
            showProgramListDialog = false
            if (program corresponds Program.EmptyWorkout) {
                trainViewModel.startWorkout(Workout(), Program.EmptyWorkout.id, date = longClickedDate)
            } else {
                selectedProgram = program
            }
        }) { program ->
        HeroTile(headlineContent = { Text(program.name, maxLines = 2, overflow = TextOverflow.Ellipsis) })
    }

    selectedProgram?.let { program ->
        ListAlertDialog(title = "Which workout would you like to perform?",
            onDismissRequest = { selectedProgram = null },
            dismissButton = { TextButton(onClick = { selectedProgram = null }) { Text("Cancel") } },
            list = program.workouts,
            onClick = { day ->
                selectedProgram = null
                trainViewModel.startWorkout(day, program.id, date = longClickedDate)
            }) { day ->
            HeroTile(headlineContent = { Text(day.name, maxLines = 2, overflow = TextOverflow.Ellipsis) })
        }
    }

    if (showWorkoutDiscardDialog) ListAlertDialog(title = "Which workout to discard?",
        onDismissRequest = { showWorkoutDiscardDialog = false },
        dismissButton = { TextButton(onClick = { showWorkoutDiscardDialog = false }) { Text("Cancel") } },
        list = currentHistoryRecords.filter { it.date == longClickedDate },
        onClick = { workout -> showWorkoutDiscardDialog = false; historyDao.delete(workout) }) { record ->
        val relatedProgram = programDao.where(record.relatedProgramId)
        var text = relatedProgram.name
        if (relatedProgram correspondsNot Program.EmptyWorkout) text += ", " + record.workout.name

        HeroTile(headlineContent = { Text(text, maxLines = 2, overflow = TextOverflow.Ellipsis) }, supportingContent = {
            val totalSets = record.workout.entries.sumOf { it.sets.count { it.actualPerfVar != 0f } }
            if (totalSets != 0) Text(text = "$totalSets performed sets")
        })
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
