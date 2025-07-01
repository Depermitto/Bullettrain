package io.github.depermitto.bullettrain.train

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destination
import io.github.depermitto.bullettrain.components.ExtendedListItem
import io.github.depermitto.bullettrain.components.NumberField
import io.github.depermitto.bullettrain.components.Placeholder
import io.github.depermitto.bullettrain.components.TextLink
import io.github.depermitto.bullettrain.components.format
import io.github.depermitto.bullettrain.components.reorderable
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
import sh.calvin.reorderable.ReorderableColumn

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
    val filter = { descriptor: Exercise.Descriptor ->
      trainViewModel.getExercises().none { it.descriptorId == descriptor.id }
    }
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    ReorderableColumn(
      list = trainViewModel.getExercises(),
      verticalArrangement = Arrangement.spacedBy(Dp.Medium),
      onMove = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
          view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
        }
      },
      onSettle = { from, to -> trainViewModel.reorderExercises(from, to) },
    ) { exerciseIndex, exercise, isDragging ->
      key(exercise.descriptorId) {
        val exerciseDescriptor = exerciseDao.where(exercise.descriptorId)
        val lastPerformedSet = exercise.lastCompletedSet

        val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
        var showSwapExerciseChooser by rememberSaveable { mutableStateOf(false) }

        if (showSwapExerciseChooser)
          ExerciseChooser(
            exerciseDao = exerciseDao,
            historyDao = historyDao,
            onDismissRequest = { showSwapExerciseChooser = false },
            filter = filter,
            onSelection = {
              trainViewModel.setExercise(
                exerciseIndex,
                exercise.toBuilder().setDescriptorId(it.id).build(),
              )
            },
          )

        Surface(shadowElevation = elevation, shape = MaterialTheme.shapes.medium) {
          Exercise(
            exercise = exercise,
            onExerciseChange = { trainViewModel.setExercise(exerciseIndex, it) },
            headline = {
              ExtendedListItem(
                contentPadding = PaddingValues(8.dp, 12.dp, 12.dp, 12.dp),
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
                    if (trainViewModel.isWorkoutRunning())
                      lastPerformedSet?.let { exerciseSet ->
                        Card {
                          Text(
                            modifier = Modifier.padding(Dp.Small),
                            text =
                              if (exercise.setsList.all { it.hasDoneTs() }) "Done"
                              else trainViewModel.elapsed(exerciseSet.doneTs),
                            style = MaterialTheme.typography.titleMedium,
                          )
                        }
                      }

                    IconButton(
                      modifier =
                        Modifier.size(SqueezableIconSize).reorderable(this@ReorderableColumn, view),
                      onClick = { showSwapExerciseChooser = true },
                    ) {
                      SwapIcon()
                    }

                    Spacer(Modifier.width(2.dp))

                    FilledTonalIconButton(
                      modifier = Modifier.size(SqueezableIconSize),
                      onClick = {
                        trainViewModel.setExercise(
                          exerciseIndex,
                          exercise
                            .toBuilder()
                            .apply {
                              // TODO get target and RPE from relatedProgram
                              if (setsCount == 0) {
                                addSets(Exercise.Set.getDefaultInstance())
                              } else {
                                val lastSet = exercise.setsList.last()
                                val builder =
                                  Exercise.Set.newBuilder()
                                    .setTarget(lastSet.target)
                                    .setIntensity(lastSet.intensity)
                                if (exercise.hasTarget2) builder.setTarget2(lastSet.target2)
                                addSets(builder)
                              }
                            }
                            .build(),
                        )
                      },
                    ) {
                      Icon(Icons.Filled.Add, "Add Exercise Set")
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
              Text(exercise.type.name, Modifier.weight(.7F), textAlign = TextAlign.Center)
              Text(
                settings.unitSystem.weightUnit(),
                Modifier.weight(.7F),
                textAlign = TextAlign.Center,
              )
              Spacer(Modifier.weight(.3F))
            },
            content = { setIndex, set ->
              Text(
                modifier = Modifier.weight(.2F),
                text = (setIndex + 1).toString(),
                textAlign = TextAlign.Center,
              )
              if (exercise.hasIntensity)
                Text(
                  modifier = Modifier.weight(.3F),
                  text = set.intensity.toString(),
                  textAlign = TextAlign.Center,
                )
              Text(
                modifier = Modifier.weight(.4F),
                text =
                  run {
                    val target =
                      if (set.target == 0F) return@run "--"
                      else if (exercise.hasTarget2) set.target.format() + "-" + set.target2.format()
                      else set.target.format()
                    "$target ${if (exercise.type == Exercise.Type.Reps) "reps" else "min"}"
                  },
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
                placeholder = { lastPerformedSet?.let { Placeholder(it.actual.format()) } },
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
                placeholder = { lastPerformedSet?.let { Placeholder(it.weight.format()) } },
              )
              Checkbox(
                modifier = Modifier.size(CompactIconSize).weight(.3F),
                checked = set.hasDoneTs(),
                onCheckedChange = { trainViewModel.toggleCompletion(it, exerciseIndex, setIndex) },
              )
            },
            exerciseDescriptor = exerciseDescriptor,
            settings = settings,
            scope = scope,
            snackbarHostState = snackbarHostState,
          )
        }
      }
    }

    var showExerciseChooser by rememberSaveable { mutableStateOf(false) }
    if (showExerciseChooser)
      ExerciseChooser(
        exerciseDao = exerciseDao,
        historyDao = historyDao,
        onDismissRequest = { showExerciseChooser = false },
        filter = filter,
        onSelection = {
          trainViewModel.addExercise(
            Exercise.newBuilder()
              .setDescriptorId(it.id)
              .addSets(Exercise.Set.getDefaultInstance())
              .build()
          )
        },
      )
    TextButton(
      modifier = Modifier.fillMaxWidth(),
      onClick = { showExerciseChooser = true },
      colors =
        ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
    ) {
      Text(text = "Add Exercise")
    }
  }
}

@Composable
fun PrettyToggleNumberField(
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
