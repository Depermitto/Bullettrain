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
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import io.github.depermitto.bullettrain.database.Day
import io.github.depermitto.bullettrain.database.HistoryDao
import io.github.depermitto.bullettrain.database.HistoryRecord
import io.github.depermitto.bullettrain.database.Program
import io.github.depermitto.bullettrain.database.ProgramDao
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

    var longClickedDate: LocalDate by rememberSaveable { mutableStateOf(today) }
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

    val programs by programDao.getAll.collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedProgram: Program? by rememberSaveable { mutableStateOf(null) }

    if (showProgramListDialog) ListAlertDialog(onDismissRequest = { showProgramListDialog = false },
        list = programs,
        dismissButton = { TextButton(onClick = { showProgramListDialog = false }) { Text("Cancel") } },
        confirmButton = {
            TextButton(onClick = {
                showProgramListDialog = false
                if (it == Program.EmptyWorkout) {
                    trainViewModel.startWorkout(Day(), Program.EmptyWorkout, date = longClickedDate)
                } else {
                    selectedProgram = it
                }
            }) {
                Text("Confirm")
            }
        }) { program ->
        ListItem(headlineContent = {
            Text(program.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }, colors = ListItemDefaults.colors(containerColor = Color.Transparent))
    }

    selectedProgram?.let { program ->
        ListAlertDialog(onDismissRequest = { selectedProgram = null },
            list = program.days,
            dismissButton = { TextButton(onClick = { selectedProgram = null }) { Text("Cancel") } },
            confirmButton = {
                TextButton(onClick = {
                    selectedProgram = null
                    trainViewModel.startWorkout(program.days[program.nextDay], program, date = longClickedDate)
                }) {
                    Text("Confirm")
                }
            }) { day ->
            ListItem(headlineContent = {
                Text(day.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }, colors = ListItemDefaults.colors(containerColor = Color.Transparent))
        }
    }

    if (showWorkoutDiscardDialog) ListAlertDialog(onDismissRequest = { showWorkoutDiscardDialog = false },
        list = currentHistoryRecords.filter { it.date == longClickedDate },
        dismissButton = { TextButton(onClick = { showWorkoutDiscardDialog = false }) { Text("Cancel") } },
        confirmButton = {
            TextButton(onClick = {
                showWorkoutDiscardDialog = false; historyDao.delete(it)
            }) {
                Text("Discard")
            }
        }) { record ->
        var text = record.relatedProgram.name
        if (record.relatedProgram != Program.EmptyWorkout) text += ", " + record.workout.name

        ListItem(headlineContent = {
            Text(text, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }, supportingContent = {
            val totalSets = record.workout.exercises.sumOf { it.sets.count { it.actualPerfVar != 0f } }
            if (totalSets != 0) Text(text = "$totalSets total sets")
        }, colors = ListItemDefaults.colors(containerColor = Color.Transparent))
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
