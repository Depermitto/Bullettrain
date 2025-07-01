package io.github.depermitto.bullettrain.train

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import io.github.depermitto.bullettrain.components.ConfirmationAlertDialog
import io.github.depermitto.bullettrain.components.DropdownButton
import io.github.depermitto.bullettrain.components.ExtendedListItem
import io.github.depermitto.bullettrain.components.NumberField
import io.github.depermitto.bullettrain.components.Placeholder
import io.github.depermitto.bullettrain.components.ReorderingAlertDialog
import io.github.depermitto.bullettrain.components.format
import io.github.depermitto.bullettrain.db.ExerciseDao
import io.github.depermitto.bullettrain.db.HistoryDao
import io.github.depermitto.bullettrain.exercises.Exercise
import io.github.depermitto.bullettrain.exercises.ExerciseChooser
import io.github.depermitto.bullettrain.protos.ExercisesProto.*
import io.github.depermitto.bullettrain.protos.SettingsProto.*
import io.github.depermitto.bullettrain.theme.*
import io.github.depermitto.bullettrain.util.lastCompletedSet
import io.github.depermitto.bullettrain.util.weightUnit
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
        .padding(bottom = Dp.EmptyScrollSpace)
  ) {
    val scope = rememberCoroutineScope()

    var showReorderExerciseDialog by rememberSaveable { mutableStateOf(false) }
    if (showReorderExerciseDialog)
      ReorderingAlertDialog(
        title = "Reorder exercises",
        onDismissRequest = { showReorderExerciseDialog = false },
        dismissButton = {
          TextButton(onClick = { showReorderExerciseDialog = false }) { Text("Close") }
        },
        exercises = trainViewModel.getExercises(),
        onSettle = { from, to -> trainViewModel.reorderExercises(from, to) },
      ) { exerciseIndex, exercise ->
        val descriptor by exerciseDao.whereAsState(id = exercise.descriptorId)
        Text("${exerciseIndex + 1}. ${descriptor.name}", maxLines = 2)
      }

    Column(verticalArrangement = Arrangement.spacedBy(Dp.Medium)) {
      trainViewModel.getExercises().forEachIndexed { exerciseIndex, exercise ->
        val descriptor by exerciseDao.whereAsState(id = exercise.descriptorId)
        val lastCompletedSet = exercise.lastCompletedSet

        var showDeleteExerciseDialog by rememberSaveable { mutableStateOf(false) }
        if (showDeleteExerciseDialog)
          ConfirmationAlertDialog(
            text = "Do you definitely want to delete the ${descriptor.name}?",
            onDismissRequest = { showDeleteExerciseDialog = false },
            onConfirm = { trainViewModel.removeExerciseAt(exerciseIndex) },
          )

        var showSwapExerciseChooser by rememberSaveable { mutableStateOf(false) }
        if (showSwapExerciseChooser)
          ExerciseChooser(
            exerciseDao = exerciseDao,
            historyDao = historyDao,
            onDismissRequest = { showSwapExerciseChooser = false },
            exclude = trainViewModel.getExercises().map { e -> e.descriptorId },
            onSelection = {
              trainViewModel.setExercise(
                exerciseIndex,
                exercise.toBuilder().setDescriptorId(it.id).build(),
              )
            },
          )

        Exercise(
          exercise = exercise,
          onExerciseChange = { trainViewModel.setExercise(exerciseIndex, it) },
          descriptor = descriptor,
          settings = settings,
          scope = scope,
          snackbarHostState = snackbarHostState,
          headline = {
            ExtendedListItem(
              onClick = { navController.navigate(Destination.Exercise(descriptor.id)) },
              contentPadding = PaddingValues(12.dp, 12.dp, 8.dp, 12.dp),
              headlineContent = {
                Text(
                  "${exerciseIndex + 1}. ${descriptor.name}",
                  style = MaterialTheme.typography.titleMedium,
                  maxLines = 2,
                )
              },
              trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  if (trainViewModel.isWorkoutRunning())
                    lastCompletedSet?.let { exerciseSet ->
                      Card {
                        Text(
                          if (exercise.setsList.all { it.hasDoneTs() }) "Done"
                          else trainViewModel.elapsed(exerciseSet.doneTs),
                          modifier = Modifier.padding(Dp.Small),
                          style = MaterialTheme.typography.titleMedium,
                        )
                      }
                    }

                  var showDropdown by remember { mutableStateOf(false) }
                  DropdownButton(
                    modifier = Modifier.offset(x = 2.dp),
                    show = showDropdown,
                    onShowChange = { showDropdown = it },
                  ) {
                    DropdownMenuItem(
                      text = { Text("Swap") },
                      onClick = {
                        showDropdown = false
                        showSwapExerciseChooser = true
                      },
                      leadingIcon = SwapIcon,
                    )
                    DropdownMenuItem(
                      text = { Text("Reorder") },
                      onClick = {
                        showDropdown = false
                        showReorderExerciseDialog = true
                      },
                      leadingIcon = NumberedListIcon,
                    )
                    DropdownMenuItem(
                      text = { Text("Delete") },
                      onClick = {
                        showDropdown = false
                        showDeleteExerciseDialog = true
                      },
                      leadingIcon = { Icon(Icons.Filled.Delete, "Remove exercise") },
                    )
                  }

                  FilledTonalIconButton(
                    onClick = { trainViewModel.addExerciseSet(exerciseIndex) }
                  ) {
                    Icon(Icons.Filled.Add, "Add exercise set")
                  }
                }
              },
            )
          },
          headerContent = {
            Text("Set", Modifier.weight(.2F), textAlign = TextAlign.Center)
            if (exercise.hasIntensity)
              Text("RPE", Modifier.weight(.3F), textAlign = TextAlign.Center)
            Text("Target", Modifier.weight(.4F), textAlign = TextAlign.Center)
            Text("Reps", Modifier.weight(.7F), textAlign = TextAlign.Center)
            Text(
              settings.unitSystem.weightUnit(),
              Modifier.weight(.7F),
              textAlign = TextAlign.Center,
            )
            Spacer(Modifier.weight(.3F))
          },
          content = { setIndex, set ->
            Text(
              (setIndex + 1).toString(),
              modifier = Modifier.weight(.2F),
              textAlign = TextAlign.Center,
            )
            if (exercise.hasIntensity)
              Text(
                set.intensity.toString(),
                modifier = Modifier.weight(.3F),
                textAlign = TextAlign.Center,
              )
            Text(
              if (set.target == 0F) "--"
              else if (exercise.hasTarget2)
                set.target.format() + "-" + set.target2.format() + " reps"
              else set.target.format() + " reps",
              modifier = Modifier.weight(.4F),
              textAlign = TextAlign.Center,
              style = MaterialTheme.typography.bodyMedium,
            )
            PrettyToggleNumberField(
              modifier = Modifier.weight(.7F).padding(horizontal = 2.dp),
              value = set.actual,
              onValueChange = {
                trainViewModel.setExerciseSet(
                  exerciseIndex,
                  setIndex,
                  set.toBuilder().setActual(it),
                )
              },
              completed = set.hasDoneTs(),
              floatingPoint = false,
              placeholder = { lastCompletedSet?.let { Placeholder(it.actual.format()) } },
            )
            PrettyToggleNumberField(
              modifier = Modifier.weight(.7F).padding(horizontal = 2.dp),
              value = set.weight,
              onValueChange = {
                trainViewModel.setExerciseSet(
                  exerciseIndex,
                  setIndex,
                  set.toBuilder().setWeight(it),
                )
              },
              completed = set.hasDoneTs(),
              floatingPoint = true,
              placeholder = { lastCompletedSet?.let { Placeholder(it.weight.format()) } },
            )
            Checkbox(
              modifier = Modifier.size(20.dp).weight(.3F),
              checked = set.hasDoneTs(),
              onCheckedChange = { trainViewModel.toggleCompletion(it, exerciseIndex, setIndex) },
            )
          },
        )
      }
    }

    var showExerciseChooser by rememberSaveable { mutableStateOf(false) }
    if (showExerciseChooser)
      ExerciseChooser(
        exerciseDao = exerciseDao,
        historyDao = historyDao,
        onDismissRequest = { showExerciseChooser = false },
        exclude = trainViewModel.getExercises().map { e -> e.descriptorId },
        onSelection = { trainViewModel.addExercise(it) },
      )
    TextButton(
      modifier = Modifier.fillMaxWidth(),
      onClick = { showExerciseChooser = true },
      colors =
        ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
    ) {
      Text("Add Exercise")
    }
  }
}

@Composable
fun PrettyToggleNumberField(
  modifier: Modifier = Modifier,
  value: Float,
  onValueChange: (Float) -> Unit,
  completed: Boolean,
  floatingPoint: Boolean,
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
    floatingPoint = floatingPoint,
    placeholder = placeholder,
    textStyle = textStyle,
    unfocusedBorderThickness = unfocusedBorderThickness,
    colors = colors,
  )
}
