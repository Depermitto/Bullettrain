package io.github.depermitto.bullettrain.train

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destination
import io.github.depermitto.bullettrain.R
import io.github.depermitto.bullettrain.components.AnchoredFloatingActionButton
import io.github.depermitto.bullettrain.components.DataPanel
import io.github.depermitto.bullettrain.components.ListAlertDialog
import io.github.depermitto.bullettrain.components.Tile
import io.github.depermitto.bullettrain.database.entities.ExerciseDao
import io.github.depermitto.bullettrain.database.entities.Program
import io.github.depermitto.bullettrain.database.entities.ProgramDao
import io.github.depermitto.bullettrain.database.entities.Settings
import io.github.depermitto.bullettrain.database.entities.Workout
import io.github.depermitto.bullettrain.database.entities.WorkoutEntry
import io.github.depermitto.bullettrain.theme.Large
import io.github.depermitto.bullettrain.theme.Medium
import io.github.depermitto.bullettrain.theme.focalGround

@Composable
fun TrainTab(
  modifier: Modifier = Modifier,
  trainViewModel: TrainViewModel,
  programDao: ProgramDao,
  exerciseDao: ExerciseDao,
  settings: Settings,
  navController: NavController,
) =
  Box(modifier.fillMaxSize()) {
    Column(
      Modifier.padding(horizontal = Dp.Medium),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      val programs by
        programDao.getUserPrograms.collectAsStateWithLifecycle(initialValue = emptyList())
      var showChangeDayIndexDialog by rememberSaveable { mutableStateOf(false) }
      var selectedProgramIndex by rememberSaveable { mutableIntStateOf(0) }

      Card(
        modifier = Modifier.heightIn(0.dp, 400.dp),
        colors =
          CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.focalGround(settings.theme)
          ),
      ) {
        val program =
          programs.getOrElse(selectedProgramIndex) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Text("No Program Found")
            }
            return@Card
          }

        DataPanel<WorkoutEntry>(
          items = program.nextDay().entries,
          backgroundColor = Color.Transparent,
          headline = {
            Tile(
              headlineContent = { Text(text = program.name) },
              headlineTextStyle = MaterialTheme.typography.titleLarge,
              supportingContent = { Text(text = program.nextDay().name) },
            )
          },
          headerTextStyle = MaterialTheme.typography.bodyLarge,
          headerContent = {
            Text("Set", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            Spacer(Modifier.weight(1f))
            Text("Sets", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
          },
          contentPadding = PaddingValues(horizontal = Dp.Large),
        ) { entryIndex, entry ->
          val exerciseDescriptor = exerciseDao.where(entry.descriptorId)
          Tile(
            headlineContent = { Text(text = exerciseDescriptor.name, maxLines = 2) },
            trailingContent = {
              Text(
                text = entry.sets.size.toString(),
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
              )
            },
            modifier = Modifier.clip(MaterialTheme.shapes.small),
            onClick = { navController.navigate(Destination.Exercise(exerciseDescriptor.id)) },
            contentPadding = PaddingValues(0.dp),
          )
        }

        Spacer(Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
          ElevatedButton(
            onClick = { showChangeDayIndexDialog = true },
            colors =
              ButtonDefaults.elevatedButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary
              ),
            shape = RoundedCornerShape(16.dp, 4.dp, 4.dp, 16.dp),
          ) {
            Icon(
              painterResource(R.drawable.autorenew),
              null,
              Modifier.size(ButtonDefaults.IconSize),
            )
          }
          Spacer(Modifier.width(2.dp))
          ElevatedButton(
            onClick = { trainViewModel.startWorkout(program.nextDay(), program.id) },
            colors =
              ButtonDefaults.elevatedButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary
              ),
            shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 4.dp),
          ) {
            Text("Start ${program.nextDay().name}")
          }
        }

        if (showChangeDayIndexDialog)
          ListAlertDialog(
            title = "Which day would you like to swap with?",
            onDismissRequest = { showChangeDayIndexDialog = false },
            dismissButton = {
              TextButton(onClick = { showChangeDayIndexDialog = false }) { Text("Cancel") }
            },
            list = program.workouts,
            onClick = { day ->
              showChangeDayIndexDialog = false
              programDao.update(program.copy(nextDayIndex = program.workouts.indexOf(day)))
            },
          ) { day ->
            Tile(headlineContent = { Text(day.name) })
          }
      }

      Row {
        val (lo, hi) =
          when {
            programs.size < 5 -> 0 to programs.size
            selectedProgramIndex < 2 -> 0 to 5
            selectedProgramIndex >= programs.size - 2 -> programs.size - 5 to programs.size
            else -> selectedProgramIndex - 2 to selectedProgramIndex + 3
          }
        for (i in lo until hi) {
          RadioButton(selected = selectedProgramIndex == i, onClick = { selectedProgramIndex = i })
        }
      }
    }

    AnchoredFloatingActionButton(
      icon = { Icon(painterResource(R.drawable.checkbox_blank), null) },
      text = { Text("Start Empty Workout") },
    ) {
      trainViewModel.startWorkout(Workout(), Program.EmptyWorkout.id)
    }
  }
