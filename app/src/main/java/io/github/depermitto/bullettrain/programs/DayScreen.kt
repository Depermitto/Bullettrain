package io.github.depermitto.bullettrain.programs

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import io.github.depermitto.bullettrain.components.DragButton
import io.github.depermitto.bullettrain.components.NumberField
import io.github.depermitto.bullettrain.components.SwipeToDeleteBox
import io.github.depermitto.bullettrain.components.TextLink
import io.github.depermitto.bullettrain.components.Tile
import io.github.depermitto.bullettrain.database.entities.*
import io.github.depermitto.bullettrain.exercises.ExerciseChooser
import io.github.depermitto.bullettrain.exercises.WorkoutEntry
import io.github.depermitto.bullettrain.theme.*
import io.github.depermitto.bullettrain.util.reorder
import io.github.depermitto.bullettrain.util.smallListSet
import io.github.depermitto.bullettrain.util.splitOnUppercase
import kotlin.math.max
import kotlin.math.min
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
    val filter = { descriptor: ExerciseDescriptor ->
      programViewModel.getDay(dayIndex).entries.none { it.descriptorId == descriptor.id }
    }
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val day = programViewModel.getDay(dayIndex)
    ReorderableColumn(
      modifier =
        Modifier.padding(horizontal = Dp.Medium)
          .verticalScroll(rememberScrollState())
          .padding(bottom = Dp.EmptyScrollSpace),
      list = day.entries,
      verticalArrangement = Arrangement.spacedBy(Dp.Medium),
      onMove = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
          view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
        }
      },
      onSettle = { from, to ->
        programViewModel.setDay(dayIndex, day.copy(entries = day.entries.reorder(from, to)))
      },
    ) { exerciseIndex, exercise, isDragging ->
      key(exercise.descriptorId) {
        val exerciseDescriptor = exerciseDao.where(exercise.descriptorId)
        val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
        var showSwapExerciseChooser by rememberSaveable { mutableStateOf(false) }

        SwipeToDeleteBox(
          shadowElevation = elevation,
          shape = MaterialTheme.shapes.medium,
          threshold = 0.9f,
          onDelete = {
            programViewModel.setDay(dayIndex, day.copy(entries = day.entries - exercise))
            scope.launch {
              val snackBarResult =
                snackbarHostState.showSnackbar(
                  message =
                    (if (exercise.sets.size == 1) "A set" else "${exercise.sets.size} sets") +
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
          var showTargetEditDropdown by remember { mutableStateOf(false) }
          WorkoutEntry(
            workoutEntry = exercise,
            onWorkoutEntryChange = { programViewModel.setExercise(dayIndex, exerciseIndex, it) },
            headline = {
              Tile(
                headlineContent = {
                  TextLink(
                    exerciseDescriptor.name,
                    navController = navController,
                    destination = Destination.Exercise(exerciseDescriptor.id),
                    contentPadding = PaddingValues(Dp.Medium),
                    style = MaterialTheme.typography.titleMedium,
                  )
                },
                trailingContent = {
                  Row(horizontalArrangement = Arrangement.SpaceAround) {
                    FilledTonalIconButton(
                      modifier = Modifier.size(SqueezableIconSize),
                      onClick = {
                        programViewModel.setExercise(
                          dayIndex,
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

                    IconButton(
                      modifier = Modifier.size(SqueezableIconSize),
                      onClick = { showSwapExerciseChooser = true },
                    ) {
                      SwapIcon()
                    }

                    if (!exercise.hasIntensity)
                      IconButton(
                        modifier = Modifier.size(SqueezableIconSize),
                        onClick = {
                          programViewModel.setExercise(
                            dayIndex,
                            exerciseIndex,
                            exercise.copy(
                              intensity = Intensity.RPE,
                              sets = exercise.sets.map { it.copy(actualIntensity = 0f) },
                            ),
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
                            exercise.copy(
                              intensity = null,
                              sets = exercise.sets.map { it.copy(actualIntensity = null) },
                            ),
                          )
                        },
                      ) {
                        HeartRemoveIcon()
                      }

                    DragButton(this@ReorderableColumn, view)
                  }
                },
              )
            },
            headerContent = {
              Text("Set", Modifier.weight(.2f), textAlign = TextAlign.Center)
              Row(
                modifier = Modifier.weight(.9f).clickable { showTargetEditDropdown = true },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = exercise.perfVarCategory.name.splitOnUppercase(),
                  textAlign = TextAlign.Center,
                )
                Icon(Icons.Sharp.KeyboardArrowDown, contentDescription = null)

                DropdownMenu(
                  expanded = showTargetEditDropdown,
                  onDismissRequest = { showTargetEditDropdown = false },
                ) {
                  PerfVarCategory.entries.forEach { entry ->
                    DropdownMenuItem(
                      text = { Text(entry.name.splitOnUppercase()) },
                      onClick = {
                        programViewModel.setExercise(
                          dayIndex,
                          exerciseIndex,
                          exercise.copy(
                            sets = exercise.sets.map { it.copy(targetPerfVar = PerfVar.of(entry)) },
                            perfVarCategory = entry,
                          ),
                        )
                        showTargetEditDropdown = false
                      },
                    )
                  }
                }
              }
              if (exercise.hasIntensity) {
                Text("RPE", Modifier.weight(.6f), textAlign = TextAlign.Center)
              }
              Spacer(Modifier.weight(.2f))
            },
            content = { setIndex, set ->
              Text((setIndex + 1).toString(), Modifier.weight(.2f), textAlign = TextAlign.Center)
              ExerciseTargetField(
                modifier = Modifier.weight(.9f).padding(horizontal = 2.dp),
                value = set.targetPerfVar,
                onValueChange = {
                  programViewModel.setExercise(
                    dayIndex,
                    exerciseIndex,
                    exercise.copy(
                      sets = exercise.sets.smallListSet(setIndex, set.copy(targetPerfVar = it))
                    ),
                  )
                },
              )
              if (set.actualIntensity != null)
                NumberField(
                  modifier = Modifier.weight(0.6f).padding(horizontal = 2.dp),
                  value = set.actualIntensity,
                  onValueChange = {
                    programViewModel.setExercise(
                      dayIndex,
                      exerciseIndex,
                      exercise.copy(
                        sets =
                          exercise.sets.smallListSet(
                            setIndex,
                            set.copy(actualIntensity = max(min(it, 10f), 0f)),
                          )
                      ),
                    )
                  },
                )
              IconButton(
                modifier = Modifier.weight(0.2f).size(CompactIconSize),
                onClick = {
                  programViewModel.setExercise(
                    dayIndex,
                    exerciseIndex,
                    exercise.copy(sets = exercise.sets + set),
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
                exercise.copy(descriptorId = it.id),
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
            day.copy(
              entries =
                day.entries +
                  WorkoutEntry(
                    descriptorId = it.id,
                    sets = listOf(ExerciseSet(targetPerfVar = PerfVar.Reps())),
                  )
            ),
          )
        },
      )
    AnchoredFloatingActionButton(
      text = { Text("Add Exercise") },
      icon = { Icon(Icons.Filled.Add, null) },
      onClick = { showAddExerciseChooser = true },
    )
  }
}
