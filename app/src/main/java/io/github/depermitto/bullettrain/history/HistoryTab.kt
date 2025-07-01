package io.github.depermitto.bullettrain.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
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
import io.github.depermitto.bullettrain.components.format
import io.github.depermitto.bullettrain.db.ExerciseDao
import io.github.depermitto.bullettrain.db.HistoryDao
import io.github.depermitto.bullettrain.db.ProgramDao
import io.github.depermitto.bullettrain.home.HomeViewModel
import io.github.depermitto.bullettrain.protos.SettingsProto.*
import io.github.depermitto.bullettrain.theme.EmptyScrollSpace
import io.github.depermitto.bullettrain.theme.Large
import io.github.depermitto.bullettrain.theme.Medium
import io.github.depermitto.bullettrain.theme.Small
import io.github.depermitto.bullettrain.theme.focalGround
import io.github.depermitto.bullettrain.train.TrainViewModel
import io.github.depermitto.bullettrain.util.DateFormatters
import io.github.depermitto.bullettrain.util.getDate
import io.github.depermitto.bullettrain.util.weightUnit
import java.time.LocalDate

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
  Box(modifier = modifier.fillMaxSize()) {
    val historyRecords by
      historyDao
        .where(homeViewModel.calendarDate.month, homeViewModel.calendarDate.year)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedHistoryRecords =
      historyRecords.filter { record -> record.getDate() == homeViewModel.selectedDate }
    LaunchedEffect(historyRecords) {
      if (homeViewModel.selectedDate == null)
        homeViewModel.selectedDate = historyRecords.lastOrNull()?.getDate()
    }
    val today = LocalDate.now()
    val resetDateButtonVisible =
      homeViewModel.calendarDate.month != today.month ||
        homeViewModel.calendarDate.year != today.year

    Column(
      modifier =
        Modifier.padding(horizontal = Dp.Medium)
          .verticalScroll(rememberScrollState())
          .padding(bottom = if (resetDateButtonVisible) Dp.EmptyScrollSpace / 4 else Dp.Medium),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(Dp.Small),
    ) {
      Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = { homeViewModel.calendarDate = homeViewModel.calendarDate.minusMonths(1) }
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
        }
        Text(
          modifier = Modifier.weight(1F),
          text = homeViewModel.calendarDate.format(DateFormatters.MMMM_yyyy),
          maxLines = 1,
          textAlign = TextAlign.Center,
        )
        IconButton(
          onClick = { homeViewModel.calendarDate = homeViewModel.calendarDate.plusMonths(1) }
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
        }
      }

      Calendar(
        homeViewModel = homeViewModel,
        trainViewModel = trainViewModel,
        currentRecords = historyRecords,
        currentDate = homeViewModel.calendarDate,
        programDao = programDao,
        modifier = Modifier.heightIn(0.dp, 400.dp),
      )

      selectedHistoryRecords.forEach { record ->
        var showRecordDeleteDialog by rememberSaveable { mutableStateOf(false) }
        val relatedProgram = programDao.where(record.relatedProgramId)
        val plannedWorkout = relatedProgram.workoutsList.first { it.name == record.workout.name }
        DataPanel(
          modifier = Modifier.fillMaxWidth(),
          items =
            record.workout.exercisesList.filter { exercise ->
              val skipped = exercise.setsList.all { set -> !set.hasDoneTs() }
              val planned =
                plannedWorkout.exercisesList.any { it.descriptorId == exercise.descriptorId }
              !skipped || planned
            },
          backgroundColor = focalGround(settings.theme),
          headline = {
            ExtendedListItem(
              headlineContent = { Text(text = relatedProgram.name) },
              headlineTextStyle = MaterialTheme.typography.titleLarge,
              supportingContent = { Text(text = record.workout.name) },
              trailingContent = {
                var showDropdown by rememberSaveable { mutableStateOf(false) }
                DropdownButton(showDropdown, onShowChange = { showDropdown = it }) {
                  DropdownMenuItem(
                    text = { Text(text = "Edit") },
                    leadingIcon = { Icon(Icons.Filled.Edit, "Edit Workout") },
                    onClick = {
                      showDropdown = false
                      trainViewModel.editWorkout(record.id)
                    },
                  )
                  DropdownMenuItem(
                    text = { Text(text = "Delete") },
                    leadingIcon = { Icon(Icons.Filled.Delete, "Delete Workout") },
                    onClick = {
                      showDropdown = false
                      showRecordDeleteDialog = true
                    },
                  )
                }

                if (showRecordDeleteDialog) {
                  val date = record.getDate().format(DateFormatters.MMMM_d_yyyy)
                  val text =
                    if (relatedProgram.id != -1) {
                      "Do you definitely want to delete the ${record.workout.name} workout from ${relatedProgram.name} on $date?"
                    } else {
                      "Do you definitely want to delete an ${relatedProgram.name} on $date?"
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
          val bestSet =
            exercise.setsList
              .filter { it.hasDoneTs() }
              .maxByOrNull { set -> set.weight * (1 + set.actual / 30F) } // 1RM
              ?.let { bestSet ->
                val actual = bestSet.actual.format()
                val weight = bestSet.weight.format()
                when {
                  actual.isBlank() -> "$weight ${settings.unitSystem.weightUnit()}"
                  weight.isBlank() -> "$actual ${exercise.type}"
                  else -> "$actual x $weight ${settings.unitSystem.weightUnit()}"
                }
              }

          val exerciseDescriptor = exerciseDao.where(exercise.descriptorId)
          ExtendedListItem(
            headlineContent = { Text(text = exerciseDescriptor.name, maxLines = 2) },
            trailingContent = {
              Text(text = bestSet ?: "skipped", overflow = TextOverflow.Ellipsis, maxLines = 2)
            },
            modifier = Modifier.clip(MaterialTheme.shapes.small),
            onClick = { navController.navigate(Destination.Exercise(exerciseDescriptor.id)) },
            contentPadding = PaddingValues(0.dp),
          )
        }
      }
    }

    AnimatedVisibility(
      visible = resetDateButtonVisible,
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
        Icon(
          modifier = Modifier.size(ButtonDefaults.IconSize),
          imageVector = Icons.Filled.Refresh,
          contentDescription = "Reset Date",
        )
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text("Reset Date")
      }
    }
  }
}
