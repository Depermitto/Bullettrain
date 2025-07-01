package io.github.depermitto.bullettrain.programs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destination
import io.github.depermitto.bullettrain.components.AnchoredFloatingActionButton
import io.github.depermitto.bullettrain.components.DropdownButton
import io.github.depermitto.bullettrain.components.ExtendedListItem
import io.github.depermitto.bullettrain.components.NumberField
import io.github.depermitto.bullettrain.components.ReorderingAlertDialog
import io.github.depermitto.bullettrain.components.SwipeToDeleteBox
import io.github.depermitto.bullettrain.db.ExerciseDao
import io.github.depermitto.bullettrain.db.HistoryDao
import io.github.depermitto.bullettrain.exercises.Exercise
import io.github.depermitto.bullettrain.exercises.ExerciseChooser
import io.github.depermitto.bullettrain.protos.ExercisesProto.*
import io.github.depermitto.bullettrain.protos.SettingsProto.*
import io.github.depermitto.bullettrain.theme.*
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.launch

@Composable
fun DayScreen(
  modifier: Modifier = Modifier,
  programViewModel: ProgramViewModel,
  dayIndex: Int,
  exerciseDao: ExerciseDao,
  historyDao: HistoryDao,
  settings: Settings,
  navController: NavController,
  snackbarHostState: SnackbarHostState,
) {
  Box(modifier = modifier.fillMaxSize()) {
    val scope = rememberCoroutineScope()
    val day = programViewModel.getDay(dayIndex)

    var showReorderExerciseDialog by rememberSaveable { mutableStateOf(false) }
    if (showReorderExerciseDialog)
      ReorderingAlertDialog(
        title = "Reorder exercises",
        onDismissRequest = { showReorderExerciseDialog = false },
        dismissButton = {
          TextButton(onClick = { showReorderExerciseDialog = false }) { Text("Close") }
        },
        exercises = programViewModel.getExercises(dayIndex),
        onSettle = { from, to -> programViewModel.reorderExercises(dayIndex, from, to) },
      ) { exerciseIndex, exercise ->
        val descriptor by exerciseDao.whereAsState(id = exercise.descriptorId)
        Text("${exerciseIndex + 1}. ${descriptor.name}", maxLines = 2)
      }

    Column(
      modifier =
        Modifier.padding(horizontal = Dp.Medium)
          .verticalScroll(rememberScrollState())
          .padding(bottom = Dp.EmptyScrollSpace),
      verticalArrangement = Arrangement.spacedBy(Dp.Medium),
    ) {
      programViewModel.getExercises(dayIndex).forEachIndexed { exerciseIndex, exercise ->
        val descriptor by exerciseDao.whereAsState(exercise.descriptorId)

        var showSwapExerciseChooser by rememberSaveable { mutableStateOf(false) }
        if (showSwapExerciseChooser)
          ExerciseChooser(
            exerciseDao = exerciseDao,
            historyDao = historyDao,
            onDismissRequest = { showSwapExerciseChooser = false },
            exclude = programViewModel.getExercises(dayIndex).map { e -> e.descriptorId },
            onSelection = {
              programViewModel.setExercise(
                dayIndex,
                exerciseIndex,
                exercise.toBuilder().setDescriptorId(it.id).build(),
              )
            },
          )

        SwipeToDeleteBox(
          shape = MaterialTheme.shapes.medium,
          threshold = 0.9F,
          onDelete = {
            programViewModel.setDay(
              dayIndex,
              day.toBuilder().removeExercises(exerciseIndex).build(),
            )
            scope.launch {
              val snackBarResult =
                snackbarHostState.showSnackbar(
                  message =
                    (if (exercise.setsCount == 1) "A set" else "${exercise.setsCount} sets") +
                      " of ${descriptor.name} deleted",
                  actionLabel = "Undo",
                  withDismissAction = true,
                )
              if (snackBarResult == SnackbarResult.ActionPerformed) {
                programViewModel.setDay(dayIndex, day)
              }
            }
          },
        ) {
          Exercise(
            exercise = exercise,
            onExerciseChange = { programViewModel.setExercise(dayIndex, exerciseIndex, it) },
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
                      if (!exercise.hasTarget2)
                        DropdownMenuItem(
                          text = { Text("Secondary Target") },
                          leadingIcon = SplitIcon,
                          onClick = {
                            showDropdown = false
                            programViewModel.setExercise(
                              dayIndex,
                              exerciseIndex,
                              exercise.toBuilder().setHasTarget2(true).build(),
                            )
                          },
                        )
                      else
                        DropdownMenuItem(
                          text = { Text("Singular Target") },
                          leadingIcon = MergeIcon,
                          onClick = {
                            showDropdown = false
                            programViewModel.setExercise(
                              dayIndex,
                              exerciseIndex,
                              exercise.toBuilder().setHasTarget2(false).build(),
                            )
                          },
                        )
                      if (!exercise.hasIntensity)
                        DropdownMenuItem(
                          text = { Text("Add RPE") },
                          leadingIcon = HeartPlusIcon,
                          onClick = {
                            showDropdown = false
                            programViewModel.setExercise(
                              dayIndex,
                              exerciseIndex,
                              exercise.toBuilder().setHasIntensity(true).build(),
                            )
                          },
                        )
                      else
                        DropdownMenuItem(
                          text = { Text("Remove RPE") },
                          leadingIcon = HeartRemoveIcon,
                          onClick = {
                            showDropdown = false
                            programViewModel.setExercise(
                              dayIndex,
                              exerciseIndex,
                              exercise.toBuilder().setHasIntensity(false).build(),
                            )
                          },
                        )
                    }

                    FilledTonalIconButton(
                      onClick = {
                        programViewModel.setExercise(
                          dayIndex,
                          exerciseIndex,
                          exercise.toBuilder().addSets(Exercise.Set.getDefaultInstance()).build(),
                        )
                      }
                    ) {
                      Icon(Icons.Filled.Add, "Add exercise set")
                    }
                  }
                },
              )
            },
            headerContent = {
              Text("Set", Modifier.weight(.2F), textAlign = TextAlign.Center)
              Text("Reps", Modifier.weight(.9F), textAlign = TextAlign.Center)
              if (exercise.hasIntensity)
                Text("RPE", Modifier.weight(.6F), textAlign = TextAlign.Center)
              Spacer(Modifier.weight(.2F))
            },
            content = { setIndex, set ->
              Text((setIndex + 1).toString(), Modifier.weight(.2F), textAlign = TextAlign.Center)
              Box(modifier = Modifier.weight(.9F).padding(horizontal = 2.dp)) {
                if (exercise.hasTarget2)
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    NumberField(
                      modifier = Modifier.weight(0.5F),
                      value = set.target,
                      onValueChange = {
                        programViewModel.setExercise(
                          dayIndex,
                          exerciseIndex,
                          exercise
                            .toBuilder()
                            .setSets(setIndex, set.toBuilder().setTarget(it))
                            .build(),
                        )
                      },
                      floatingPoint = false,
                    )
                    Text("-", modifier = Modifier.padding(horizontal = 2.dp))
                    NumberField(
                      modifier = Modifier.weight(0.5F),
                      value = set.target2,
                      onValueChange = {
                        programViewModel.setExercise(
                          dayIndex,
                          exerciseIndex,
                          exercise
                            .toBuilder()
                            .setSets(setIndex, set.toBuilder().setTarget2(it))
                            .build(),
                        )
                      },
                      floatingPoint = false,
                    )
                  }
                else
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    NumberField(
                      modifier = Modifier.weight(1F),
                      value = set.target,
                      onValueChange = {
                        programViewModel.setExercise(
                          dayIndex,
                          exerciseIndex,
                          exercise
                            .toBuilder()
                            .setSets(setIndex, set.toBuilder().setTarget(it))
                            .build(),
                        )
                      },
                      floatingPoint = false,
                    )
                  }
              }
              if (exercise.hasIntensity)
                NumberField(
                  modifier = Modifier.weight(0.6F).padding(horizontal = 2.dp),
                  value = set.intensity.toFloat(),
                  onValueChange = {
                    programViewModel.setExercise(
                      dayIndex,
                      exerciseIndex,
                      exercise
                        .toBuilder()
                        .setSets(
                          setIndex,
                          set.toBuilder().setIntensity(max(min(it.toInt(), 10), 0)),
                        )
                        .build(),
                    )
                  },
                  floatingPoint = false,
                )
              IconButton(
                modifier = Modifier.weight(0.2F).size(20.dp),
                onClick = {
                  programViewModel.setExercise(
                    dayIndex,
                    exerciseIndex,
                    exercise.toBuilder().addSets(setIndex, set).build(),
                  )
                },
                content = DuplicateIcon,
              )
            },
            descriptor = descriptor,
            settings = settings,
            scope = scope,
            snackbarHostState = snackbarHostState,
          )
        }
      }
    }

    var showAddExerciseChooser by rememberSaveable { mutableStateOf(false) }
    if (showAddExerciseChooser)
      ExerciseChooser(
        exerciseDao = exerciseDao,
        historyDao = historyDao,
        onDismissRequest = { showAddExerciseChooser = false },
        exclude = programViewModel.getExercises(dayIndex).map { e -> e.descriptorId },
        onSelection = {
          programViewModel.setDay(
            dayIndex,
            day
              .toBuilder()
              .addExercises(
                Exercise.newBuilder()
                  .setDescriptorId(it.id)
                  .addSets(Exercise.Set.getDefaultInstance())
              )
              .build(),
          )
        },
      )

    AnchoredFloatingActionButton(
      text = { Text("Add Exercise") },
      icon = { Icon(Icons.Filled.Add, "Add exercise") },
      onClick = { showAddExerciseChooser = true },
    )
  }
}
