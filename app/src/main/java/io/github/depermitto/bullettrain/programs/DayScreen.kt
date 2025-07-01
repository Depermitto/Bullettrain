package io.github.depermitto.bullettrain.programs

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.sharp.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destination
import io.github.depermitto.bullettrain.components.AnchoredFloatingActionButton
import io.github.depermitto.bullettrain.components.ExtendedListItem
import io.github.depermitto.bullettrain.components.NumberField
import io.github.depermitto.bullettrain.components.SwipeToDeleteBox
import io.github.depermitto.bullettrain.components.TextLink
import io.github.depermitto.bullettrain.components.reorderable
import io.github.depermitto.bullettrain.db.ExerciseDao
import io.github.depermitto.bullettrain.db.HistoryDao
import io.github.depermitto.bullettrain.exercises.Exercise
import io.github.depermitto.bullettrain.exercises.ExerciseChooser
import io.github.depermitto.bullettrain.protos.ExercisesProto.*
import io.github.depermitto.bullettrain.protos.SettingsProto.*
import io.github.depermitto.bullettrain.theme.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableColumn

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
    val filter = { descriptor: Exercise.Descriptor ->
      programViewModel.getDay(dayIndex).exercisesList.none { it.descriptorId == descriptor.id }
    }
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val day = programViewModel.getDay(dayIndex)
    ReorderableColumn(
      modifier =
        Modifier.padding(horizontal = Dp.Medium)
          .verticalScroll(rememberScrollState())
          .padding(bottom = Dp.EmptyScrollSpace),
      list = day.exercisesList,
      verticalArrangement = Arrangement.spacedBy(Dp.Medium),
      onMove = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
          view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
        }
      },
      onSettle = { from, to ->
        programViewModel.setDay(
          dayIndex,
          day
            .toBuilder()
            .apply {
              val exercise = getExercises(from)
              removeExercises(from)
              addExercises(to, exercise)
            }
            .build(),
        )
      },
    ) { exerciseIndex, exercise, isDragging ->
      key(exercise.descriptorId) {
        val exerciseDescriptor = exerciseDao.where(exercise.descriptorId)
        val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
        var showSwapExerciseChooser by rememberSaveable { mutableStateOf(false) }

        SwipeToDeleteBox(
          shadowElevation = elevation,
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
                      " of ${exerciseDescriptor.name} deleted",
                  actionLabel = "Undo",
                  withDismissAction = true,
                )
              if (snackBarResult == SnackbarResult.ActionPerformed) {
                programViewModel.setDay(dayIndex, day)
              }
            }
          },
        ) {
          var showExerciseTypeDropdown by remember { mutableStateOf(false) }
          Exercise(
            exercise = exercise,
            onExerciseChange = { programViewModel.setExercise(dayIndex, exerciseIndex, it) },
            headline = {
              ExtendedListItem(
                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp),
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
                  Column(Modifier.padding(end = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      IconButton(
                        modifier =
                          Modifier.size(SqueezableIconSize)
                            .reorderable(this@ReorderableColumn, view),
                        onClick = { showSwapExerciseChooser = true },
                      ) {
                        SwapIcon()
                      }

                      FilledTonalIconButton(
                        modifier = Modifier.size(SqueezableIconSize),
                        onClick = {
                          programViewModel.setExercise(
                            dayIndex,
                            exerciseIndex,
                            exercise.toBuilder().addSets(Exercise.Set.getDefaultInstance()).build(),
                          )
                        },
                      ) {
                        Icon(Icons.Filled.Add, null)
                      }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      if (!exercise.hasTarget2)
                        IconButton(
                          modifier = Modifier.size(SqueezableIconSize),
                          onClick = {
                            programViewModel.setExercise(
                              dayIndex,
                              exerciseIndex,
                              exercise.toBuilder().setHasTarget2(true),
                            )
                          },
                        ) {
                          SplitIcon()
                        }
                      else
                        IconButton(
                          modifier = Modifier.size(SqueezableIconSize),
                          onClick = {
                            programViewModel.setExercise(
                              dayIndex,
                              exerciseIndex,
                              exercise.toBuilder().setHasTarget2(false),
                            )
                          },
                        ) {
                          MergeIcon()
                        }

                      if (!exercise.hasIntensity)
                        IconButton(
                          modifier = Modifier.size(SqueezableIconSize),
                          onClick = {
                            programViewModel.setExercise(
                              dayIndex,
                              exerciseIndex,
                              exercise.toBuilder().setHasIntensity(true),
                            )
                          },
                        ) {
                          HeartPlusIcon()
                        }
                      else
                        IconButton(
                          modifier = Modifier.size(SqueezableIconSize),
                          onClick = {
                            programViewModel.setExercise(
                              dayIndex,
                              exerciseIndex,
                              exercise.toBuilder().setHasIntensity(false),
                            )
                          },
                        ) {
                          HeartRemoveIcon()
                        }
                    }
                  }
                },
              )
            },
            headerContent = {
              Text("Set", Modifier.weight(.2F), textAlign = TextAlign.Center)
              Row(
                modifier = Modifier.weight(.9F).clickable { showExerciseTypeDropdown = true },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(text = exercise.type.name, textAlign = TextAlign.Center)
                Icon(Icons.Sharp.KeyboardArrowDown, contentDescription = "Choose Exercise Type")
                DropdownMenu(
                  expanded = showExerciseTypeDropdown,
                  onDismissRequest = { showExerciseTypeDropdown = false },
                ) {
                  for (type in 0..1) {
                    DropdownMenuItem(
                      text = { Text(Exercise.Type.forNumber(type).name) },
                      onClick = {
                        programViewModel.setExercise(
                          dayIndex,
                          exerciseIndex,
                          exercise.toBuilder().setTypeValue(type),
                        )
                        showExerciseTypeDropdown = false
                      },
                    )
                  }
                }
              }
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
                          exercise.toBuilder().setSets(setIndex, set.toBuilder().setTarget(it)),
                        )
                      },
                    )
                    Text(text = "-", modifier = Modifier.padding(horizontal = 2.dp))
                    NumberField(
                      modifier = Modifier.weight(0.5F),
                      value = set.target2,
                      onValueChange = {
                        programViewModel.setExercise(
                          dayIndex,
                          exerciseIndex,
                          exercise.toBuilder().setSets(setIndex, set.toBuilder().setTarget2(it)),
                        )
                      },
                    )
                    if (exercise.type == Exercise.Type.Time) Text(text = "min")
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
                          exercise.toBuilder().setSets(setIndex, set.toBuilder().setTarget(it)),
                        )
                      },
                    )
                    if (exercise.type == Exercise.Type.Time) Text(text = "min")
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
                          set.toBuilder().setIntensity(max(min(it.roundToInt(), 10), 0)),
                        ),
                    )
                  },
                )
              IconButton(
                modifier = Modifier.weight(0.2F).size(CompactIconSize),
                onClick = {
                  programViewModel.setExercise(
                    dayIndex,
                    exerciseIndex,
                    exercise.toBuilder().addSets(setIndex, set),
                  )
                },
              ) {
                DuplicateIcon()
              }
            },
            exerciseDescriptor = exerciseDescriptor,
            settings = settings,
            scope = scope,
            snackbarHostState = snackbarHostState,
          )
        }

        if (showSwapExerciseChooser)
          ExerciseChooser(
            exerciseDao = exerciseDao,
            historyDao = historyDao,
            onDismissRequest = { showSwapExerciseChooser = false },
            filter = filter,
            onSelection = {
              programViewModel.setExercise(
                dayIndex,
                exerciseIndex,
                exercise.toBuilder().setDescriptorId(it.id),
              )
            },
          )
      }
    }

    var showAddExerciseChooser by rememberSaveable { mutableStateOf(false) }
    if (showAddExerciseChooser)
      ExerciseChooser(
        exerciseDao = exerciseDao,
        historyDao = historyDao,
        onDismissRequest = { showAddExerciseChooser = false },
        filter = filter,
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
      icon = { Icon(Icons.Filled.Add, "Add Exercise") },
      onClick = { showAddExerciseChooser = true },
    )
  }
}
