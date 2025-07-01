package io.github.depermitto.bullettrain.train

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import io.github.depermitto.bullettrain.Destination.Home.Tab.History
import io.github.depermitto.bullettrain.R
import io.github.depermitto.bullettrain.components.DataPanel
import io.github.depermitto.bullettrain.components.EmptyScreen
import io.github.depermitto.bullettrain.components.ExtendedListItem
import io.github.depermitto.bullettrain.components.ListAlertDialog
import io.github.depermitto.bullettrain.db.ExerciseDao
import io.github.depermitto.bullettrain.db.ProgramDao
import io.github.depermitto.bullettrain.home.HomeViewModel
import io.github.depermitto.bullettrain.protos.ProgramsProto.Workout
import io.github.depermitto.bullettrain.theme.ExtraLarge
import io.github.depermitto.bullettrain.theme.Large
import io.github.depermitto.bullettrain.theme.Medium
import java.time.Instant
import kotlinx.coroutines.launch

@Composable
fun TrainTab(
  modifier: Modifier = Modifier,
  homeViewModel: HomeViewModel,
  trainViewModel: TrainViewModel,
  programDao: ProgramDao,
  exerciseDao: ExerciseDao,
  navController: NavController,
) {
  val scope = rememberCoroutineScope()
  Column(
    modifier
      .fillMaxSize()
      .verticalScroll(state = rememberScrollState())
      .padding(horizontal = Dp.Medium)
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      val programs by programDao.getUserPrograms.collectAsStateWithLifecycle(emptyList())
      var selectedProgramIndex by rememberSaveable { mutableIntStateOf(0) }

      Card(
        modifier = Modifier.heightIn(0.dp, 350.dp),
        colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
      ) {
        val program =
          programs.getOrElse(selectedProgramIndex) {
            EmptyScreen(
              "No program found. Please create one first to start training.",
              modifier = modifier,
              showIcon = false,
            )
            return@Card
          }

        DataPanel(
          items = program.getWorkouts(program.nextDayIndex).exercisesList,
          colors = CardDefaults.cardColors(containerColor = Color.Transparent),
          headline = {
            ExtendedListItem(
              headlineContent = { Text(program.name) },
              headlineTextStyle = MaterialTheme.typography.titleLarge,
              supportingContent = { Text(program.getWorkouts(program.nextDayIndex).name) },
            )
          },
          headerTextStyle = MaterialTheme.typography.bodyLarge,
          headerContent = {
            Text("Set", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5F))
            Spacer(Modifier.weight(1F))
            Text("Sets", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5F))
          },
          contentPadding = PaddingValues(horizontal = Dp.Large),
        ) { _, entry ->
          val descriptor by exerciseDao.whereAsState(entry.descriptorId)
          ExtendedListItem(
            headlineContent = { Text(descriptor.name, maxLines = 2) },
            trailingContent = {
              Text(entry.setsCount.toString(), overflow = TextOverflow.Ellipsis, maxLines = 2)
            },
            modifier = Modifier.clip(MaterialTheme.shapes.small),
            onClick = { navController.navigate(Destination.Exercise(descriptor.id)) },
            contentPadding = PaddingValues(0.dp),
          )
        }

        Spacer(Modifier.weight(1F))

        var showChangeDayIndexDialog by rememberSaveable { mutableStateOf(false) }
        if (showChangeDayIndexDialog)
          ListAlertDialog(
            title = "Which day would you like to swap with?",
            onDismissRequest = { showChangeDayIndexDialog = false },
            dismissButton = {
              TextButton(onClick = { showChangeDayIndexDialog = false }) { Text("Cancel") }
            },
            list = program.workoutsList,
            onClick = { day ->
              showChangeDayIndexDialog = false
              programDao.update(
                program
                  .toBuilder()
                  .setNextDayIndex(program.workoutsList.indexOfFirst { it.name == day.name })
                  .build()
              )
            },
          ) { day ->
            ExtendedListItem(headlineContent = { Text(day.name) })
          }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
          OutlinedButton(
            onClick = { showChangeDayIndexDialog = true },
            shape = RoundedCornerShape(16.dp, 4.dp, 4.dp, 16.dp),
          ) {
            Icon(
              painterResource(R.drawable.autorenew),
              null,
              Modifier.size(ButtonDefaults.IconSize),
            )
          }
          Spacer(Modifier.width(2.dp))
          Button(
            onClick = {
              trainViewModel.startWorkout(
                program.getWorkouts(program.nextDayIndex),
                program.id,
                Instant.now(),
                navController,
              )
            },
            shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 4.dp),
          ) {
            Text("Start ${program.getWorkouts(program.nextDayIndex).name}")
          }
        }
        Spacer(Modifier.height(4.dp))
      }

      Row {
        val (lo, hi) =
          when {
            programs.size < 2 -> return@Row
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

    Spacer(modifier = Modifier.height(Dp.ExtraLarge))

    Text("Other Options", modifier = Modifier.padding(start = 8.dp))
    OutlinedCard(colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent)) {
      TextButton(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        onClick = {
          trainViewModel.startWorkout(
            Workout.getDefaultInstance(),
            null,
            Instant.now(),
            navController,
          )
        },
        shape = RoundedCornerShape(12.dp, 12.dp, 4.dp, 4.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
      ) {
        Text("Start Empty Session", maxLines = 1)
      }

      HorizontalDivider()

      Row(modifier = Modifier.padding(horizontal = 4.dp)) {
        TextButton(
          modifier = Modifier.weight(1F),
          onClick = {
            homeViewModel.mostRecentWorkout()
            scope.launch { homeViewModel.screenPager.animateScrollToPage(History.ordinal) }
          },
          shape = RoundedCornerShape(4.dp, 4.dp, 4.dp, 12.dp),
          colors =
            ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
        ) {
          Text("Review Last Workout", maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        TextButton(
          modifier = Modifier.weight(1F),
          onClick = { navController.navigate(Destination.ProgramCreation) },
          shape = RoundedCornerShape(4.dp, 4.dp, 12.dp, 4.dp),
          colors =
            ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
        ) {
          Text("Create a Program", maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
      }
    }
  }
}
