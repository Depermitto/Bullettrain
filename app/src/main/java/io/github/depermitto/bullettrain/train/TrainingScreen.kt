package io.github.depermitto.bullettrain.train

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destination
import io.github.depermitto.bullettrain.components.DiscardConfirmationAlertDialog
import io.github.depermitto.bullettrain.components.DropdownButton
import io.github.depermitto.bullettrain.components.ExtendedListItem
import io.github.depermitto.bullettrain.components.NumberField
import io.github.depermitto.bullettrain.components.Placeholder
import io.github.depermitto.bullettrain.components.TextLink
import io.github.depermitto.bullettrain.components.encodeToStringOutput
import io.github.depermitto.bullettrain.database.daos.ExerciseDao
import io.github.depermitto.bullettrain.database.daos.HistoryDao
import io.github.depermitto.bullettrain.database.entities.*
import io.github.depermitto.bullettrain.exercises.ExerciseChooser
import io.github.depermitto.bullettrain.exercises.WorkoutEntry
import io.github.depermitto.bullettrain.theme.*
import kotlin.collections.all

@Composable
fun TrainingScreen(
  trainViewModel: TrainViewModel,
  exerciseDao: ExerciseDao,
  historyDao: HistoryDao,
  settings: Settings,
  modifier: Modifier = Modifier,
  navController: NavController,
  snackbarHostState: SnackbarHostState,
) {
  Column(
    modifier =
      modifier
        .padding(horizontal = Dp.Medium)
        .verticalScroll(rememberScrollState())
        .padding(bottom = Dp.EmptyScrollSpace),
    verticalArrangement = Arrangement.spacedBy(Dp.Medium),
  ) {
    val filter = { descriptor: ExerciseDescriptor ->
      trainViewModel.getWorkoutEntries().none { it.descriptorId == descriptor.id }
    }
    val scope = rememberCoroutineScope()
    trainViewModel.getWorkoutEntries().forEachIndexed { exerciseIndex, exercise ->
      val exerciseDescriptor = exerciseDao.where(exercise.descriptorId)
      val lastPerformedSet = exercise.lastPerformedSet()

      var showExerciseDeleteDialog by rememberSaveable { mutableStateOf(false) }
      var showSwapExerciseChooser by rememberSaveable { mutableStateOf(false) }

      if (showSwapExerciseChooser)
        ExerciseChooser(
          exerciseDao = exerciseDao,
          historyDao = historyDao,
          onDismissRequest = { showSwapExerciseChooser = false },
          filter = filter,
          // Swap ExerciseDescriptor
          onSelection = {
            trainViewModel.setWorkoutEntry(exerciseIndex, exercise.copy(descriptorId = it.id))
          },
        )

      WorkoutEntry(
        workoutEntry = exercise,
        // Delete set
        onWorkoutEntryChange = { trainViewModel.setWorkoutEntry(exerciseIndex, it) },
        headline = {
          ExtendedListItem(
            headlineContent = {
              TextLink(
                "${exerciseIndex + 1}. ${exerciseDescriptor.name}",
                navController = navController,
                destination = Destination.Exercise(exerciseDescriptor.id),
                contentPadding = PaddingValues(Dp.Medium),
                style = MaterialTheme.typography.titleMedium,
              )
            },
            trailingContent = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                if (!trainViewModel.isWorkoutEditing())
                  lastPerformedSet?.let { exerciseSet ->
                    Card {
                      Text(
                        modifier = Modifier.padding(Dp.Small),
                        text =
                          if (exercise.sets.all { it.completed }) "Done"
                          else trainViewModel.elapsedSince(exerciseSet.doneTs!!),
                        style = MaterialTheme.typography.titleMedium,
                      )
                    }
                  }
                var showDropdownButton by remember { mutableStateOf(false) }
                DropdownButton(
                  modifier = Modifier.size(SqueezableIconSize),
                  show = showDropdownButton,
                  onShowChange = { showDropdownButton = it },
                ) {
                  DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                    text = { Text(text = "Delete") },
                    onClick = {
                      showDropdownButton = false
                      showExerciseDeleteDialog = true
                    },
                  )
                  DropdownMenuItem(
                    leadingIcon = { SwapIcon() },
                    text = { Text(text = "Swap") },
                    onClick = {
                      showDropdownButton = false
                      showSwapExerciseChooser = true
                    },
                  )
                }
                Spacer(Modifier.width(8.dp))
                FilledTonalIconButton(
                  modifier = Modifier.size(SqueezableIconSize),
                  onClick = {
                    // Add set
                    trainViewModel.setWorkoutEntry(
                      exerciseIndex,
                      exercise.copy(
                        sets =
                          exercise.sets +
                            ExerciseSet(
                              actualIntensity = exercise.intensity?.let { 0f },
                              targetPerfVar = PerfVar.of(exercise.perfVarCategory),
                            )
                      ),
                    )
                  },
                ) {
                  Icon(Icons.Filled.Add, null)
                }
              }
            },
          )
        },
        headerContent = {
          Text("Set", Modifier.weight(.2f), textAlign = TextAlign.Center)
          if (exercise.intensity != null) {
            Text(exercise.intensity.name, Modifier.weight(.3f), textAlign = TextAlign.Center)
          }
          Text("Target", Modifier.weight(.4f), textAlign = TextAlign.Center)
          Text(
            exercise.perfVarCategory.shortName,
            Modifier.weight(.7f),
            textAlign = TextAlign.Center,
          )
          Text(settings.unitSystem.weightUnit(), Modifier.weight(.7f), textAlign = TextAlign.Center)
          if (trainViewModel.isWorkoutRunning()) {
            Spacer(Modifier.weight(.3f))
          }
        },
        content = { setIndex, set ->
          Text(
            modifier = Modifier.weight(.2f),
            text = (setIndex + 1).toString(),
            textAlign = TextAlign.Center,
          )
          if (set.actualIntensity != null)
            Text(
              modifier = Modifier.weight(.3f),
              text = set.actualIntensity.encodeToStringOutput().ifBlank { "0" },
              textAlign = TextAlign.Center,
            )
          Text(
            modifier = Modifier.weight(.4f),
            text =
              set.targetPerfVar.encodeToStringOutput().takeIf {
                it.isNotBlank() && set.targetPerfVar.category == exercise.perfVarCategory
              } ?: "--",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
          )
          CompletableNumberField(
            modifier = Modifier.weight(.7f).padding(horizontal = 2.dp),
            value = set.actualPerfVar,
            onValueChange = {
              trainViewModel.setExerciseSet(exerciseIndex, setIndex, set.copy(actualPerfVar = it))
            },
            completed = set.completed,
            placeholder = {
              lastPerformedSet?.let { Placeholder(it.actualPerfVar.encodeToStringOutput()) }
            },
          )
          CompletableNumberField(
            modifier = Modifier.weight(.7f).padding(horizontal = 2.dp),
            value = set.weight,
            onValueChange = {
              trainViewModel.setExerciseSet(exerciseIndex, setIndex, set.copy(weight = it))
            },
            completed = set.completed,
            placeholder = {
              lastPerformedSet?.let { Placeholder(it.weight.encodeToStringOutput()) }
            },
          )
          Checkbox(
            modifier = Modifier.size(CompactIconSize).weight(.3f),
            checked = set.doneTs != null,
            onCheckedChange = { trainViewModel.toggleCompletion(it, exerciseIndex, setIndex) },
          )
        },
        exerciseDescriptor = exerciseDescriptor,
        settings = settings,
        scope = scope,
        snackbarHostState = snackbarHostState,
      )

      if (showExerciseDeleteDialog)
        DiscardConfirmationAlertDialog(
          onDismissRequest = { showExerciseDeleteDialog = false },
          text = "Do you definitely want to discard ${exerciseDescriptor.name}?",
          onConfirm = { trainViewModel.removeWorkoutEntryAt(exerciseIndex) },
        )
    }

    var showExerciseChooser by rememberSaveable { mutableStateOf(false) }
    if (showExerciseChooser)
      ExerciseChooser(
        exerciseDao = exerciseDao,
        historyDao = historyDao,
        onDismissRequest = { showExerciseChooser = false },
        filter = filter,
        onSelection = {
          trainViewModel.addWorkoutEntry(
            WorkoutEntry(
              descriptorId = it.id,
              sets = listOf(ExerciseSet(targetPerfVar = PerfVar.Reps())),
            )
          )
        },
      )
    OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { showExerciseChooser = true }) {
      Text(text = "Add Exercise")
    }
  }
}

@Composable
fun CompletableNumberField(
  modifier: Modifier = Modifier,
  value: Float,
  onValueChange: (Float) -> Unit,
  completed: Boolean,
  placeholder: @Composable () -> Unit,
) {
  val (textStyle, unfocusedBorderThickness, colors) =
    if (completed)
      Triple(
        TextStyle.numeric(
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.SemiBold,
        ),
        OutlinedTextFieldDefaults.FocusedBorderThickness,
        OutlinedTextFieldDefaults.colors(
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.primary,
          disabledBorderColor = MaterialTheme.colorScheme.primary,
        ),
      )
    else
      Triple(
        TextStyle.numeric(),
        OutlinedTextFieldDefaults.UnfocusedBorderThickness,
        OutlinedTextFieldDefaults.colors(),
      )

  NumberField(
    modifier = modifier,
    value = value,
    onValueChange = onValueChange,
    enabled = !completed,
    placeholder = placeholder,
    textStyle = textStyle,
    unfocusedBorderThickness = unfocusedBorderThickness,
    colors = colors,
  )
}
