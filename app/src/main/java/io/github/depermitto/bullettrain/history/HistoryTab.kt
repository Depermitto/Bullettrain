package io.github.depermitto.bullettrain.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destination
import io.github.depermitto.bullettrain.components.ConfirmationAlertDialog
import io.github.depermitto.bullettrain.components.DataPanel
import io.github.depermitto.bullettrain.components.DropdownButton
import io.github.depermitto.bullettrain.components.ExtendedListItem
import io.github.depermitto.bullettrain.db.ExerciseDao
import io.github.depermitto.bullettrain.db.HistoryDao
import io.github.depermitto.bullettrain.db.ProgramDao
import io.github.depermitto.bullettrain.exercises.format
import io.github.depermitto.bullettrain.exercises.oneRepMax
import io.github.depermitto.bullettrain.home.HomeViewModel
import io.github.depermitto.bullettrain.protos.ExercisesProto.Exercise
import io.github.depermitto.bullettrain.protos.SettingsProto.Settings
import io.github.depermitto.bullettrain.theme.Large
import io.github.depermitto.bullettrain.theme.Medium
import io.github.depermitto.bullettrain.theme.Small
import io.github.depermitto.bullettrain.train.TrainViewModel
import io.github.depermitto.bullettrain.util.DateFormatters
import io.github.depermitto.bullettrain.util.date
import java.time.YearMonth

@Composable
fun HistoryTab(
  modifier: Modifier = Modifier,
  homeViewModel: HomeViewModel,
  trainViewModel: TrainViewModel,
  historyDao: HistoryDao,
  exerciseDao: ExerciseDao,
  programDao: ProgramDao,
  settings: Settings,
  navController: NavController,
) {
  val days = generateDays(homeViewModel.calendarPage)
  val historyRecords by
    historyDao
      .map { records ->
        records
          .filter { r -> days.first() <= r.date && r.date <= days.last() }
          .sortedByDescending { r -> r.workoutStartTs.seconds }
      }
      .collectAsStateWithLifecycle(emptyList())

  LaunchedEffect(Unit) { if (homeViewModel.selectedDate == null) homeViewModel.resetDate() }
  Box(modifier = modifier.fillMaxSize()) {
    Column(
      modifier =
        Modifier.padding(horizontal = Dp.Medium)
          .verticalScroll(rememberScrollState())
          .padding(bottom = 64.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(Dp.Small),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.width(Dp.Medium))
        Text(
          homeViewModel.calendarPage.format(DateFormatters.MMMM_yyyy),
          modifier = Modifier.weight(1F),
          maxLines = 1,
        )
        IconButton(
          onClick = { homeViewModel.calendarPage = homeViewModel.calendarPage.minusMonths(1) }
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous month")
        }
        IconButton(
          onClick = { homeViewModel.calendarPage = homeViewModel.calendarPage.plusMonths(1) }
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next month")
        }
      }

      Calendar(
        modifier = Modifier.heightIn(0.dp, 400.dp),
        date = homeViewModel.calendarPage,
        records = historyRecords,
        homeViewModel = homeViewModel,
        trainViewModel = trainViewModel,
        programDao = programDao,
        navController = navController,
      )

      for (record in historyRecords) {
        if (record.date != homeViewModel.selectedDate) continue

        val workoutName: String
        val plannedExercises: List<Exercise>
        if (record.hasRelatedProgramId()) {
          val relatedProgram by programDao.whereAsState(record.relatedProgramId)
          workoutName = relatedProgram.name
          plannedExercises =
            relatedProgram.workoutsList.first { it.name == record.workout.name }.exercisesList
        } else {
          workoutName = DateFormatters.MMM_dd.format(record.date) + " Workout"
          plannedExercises = record.workout.exercisesList
        }

        var showRecordDeleteDialog by rememberSaveable { mutableStateOf(false) }
        DataPanel(
          modifier = Modifier.fillMaxWidth(),
          items =
            record.workout.exercisesList.filter { exercise ->
              val skipped = exercise.setsList.all { set -> !set.hasDoneTs() }
              val planned = plannedExercises.any { it.descriptorId == exercise.descriptorId }
              !skipped || planned
            },
          headline = {
            ExtendedListItem(
              headlineContent = { Text(workoutName) },
              headlineTextStyle = MaterialTheme.typography.titleLarge,
              supportingContent = { Text(record.workout.name) },
              trailingContent = {
                var showDropdown by remember { mutableStateOf(false) }
                DropdownButton(showDropdown, onShowChange = { showDropdown = it }) {
                  DropdownMenuItem(
                    text = { Text("Edit") },
                    leadingIcon = { Icon(Icons.Filled.Edit, "Edit workout") },
                    onClick = {
                      showDropdown = false
                      trainViewModel.editWorkout(record.id, navController)
                    },
                  )
                  DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = { Icon(Icons.Filled.Delete, "Delete workout") },
                    onClick = {
                      showDropdown = false
                      showRecordDeleteDialog = true
                    },
                  )
                }

                if (showRecordDeleteDialog) {
                  val date = record.date.format(DateFormatters.MMMM_d_yyyy)
                  val text =
                    if (record.hasRelatedProgramId()) {
                      "Do you definitely want to delete the ${record.workout.name} workout from $workoutName on $date?"
                    } else {
                      "Do you definitely want to delete an $workoutName on $date?"
                    }

                  ConfirmationAlertDialog(
                    text = text,
                    onDismissRequest = { showRecordDeleteDialog = false },
                    onConfirm = { historyDao.delete(record.id) },
                  )
                }
              },
              onClick = { navController.navigate(Destination.Workout(record.id)) },
            )
          },
          headerTextStyle = MaterialTheme.typography.bodyLarge,
          headerContent = {
            Text("Set", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5F))
            Spacer(Modifier.weight(1F))
            Text("Best Set", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5F))
          },
          contentPadding = PaddingValues(horizontal = Dp.Large),
        ) { _, exercise ->
          val descriptor by exerciseDao.whereAsState(exercise.descriptorId)
          ExtendedListItem(
            headlineContent = { Text(descriptor.name, maxLines = 2) },
            trailingContent = {
              Text(
                exercise.setsList
                  .filter { s -> s.hasDoneTs() }
                  .maxByOrNull { s -> oneRepMax(s) }
                  ?.format(settings) ?: "skipped",
                overflow = TextOverflow.Ellipsis,
              )
            },
            modifier = Modifier.clip(MaterialTheme.shapes.small),
            onClick = { navController.navigate(Destination.Exercise(descriptor.id)) },
            contentPadding = PaddingValues(0.dp),
          )
        }
      }
    }

    val notCurrentYearMonth = homeViewModel.calendarPage != YearMonth.now()
    AnimatedVisibility(
      visible = notCurrentYearMonth,
      modifier = Modifier.align(Alignment.BottomCenter),
      enter =
        slideInVertically(animationSpec = tween(durationMillis = 600), initialOffsetY = { it }),
      exit = fadeOut(),
    ) {
      TextButton(
        modifier = Modifier.padding(bottom = Dp.Medium),
        onClick = { homeViewModel.resetDate() },
        colors =
          ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onBackground,
            containerColor = MaterialTheme.colorScheme.background,
          ),
        elevation = ButtonDefaults.buttonElevation(),
      ) {
        Icon(Icons.Filled.Refresh, "Reset date", modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text("Reset Date")
      }
    }
  }
}
