package io.github.depermitto.bullettrain.train

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.components.Ratio
import io.github.depermitto.bullettrain.components.WorkoutTable
import io.github.depermitto.bullettrain.database.Day
import io.github.depermitto.bullettrain.database.Program
import io.github.depermitto.bullettrain.database.ProgramDao
import io.github.depermitto.bullettrain.theme.RegularPadding
import io.github.depermitto.bullettrain.theme.focalGround

@Composable
fun TrainTab(
    modifier: Modifier = Modifier, trainViewModel: TrainViewModel, programDao: ProgramDao, navController: NavController
) = Column(
    modifier = modifier
        .fillMaxSize()
        .padding(horizontal = RegularPadding),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    val programs by programDao.getAlmostAll.collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedProgramIndex by rememberSaveable { mutableIntStateOf(0) }

    Card(
        modifier = Modifier.heightIn(0.dp, 400.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.focalGround)
    ) {
        val program = programs.getOrElse(selectedProgramIndex) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No Program Found")
            }
            return@Card
        }

        WorkoutTable(
            program = program,
            workout = program.nextDay(),
            headers = Pair("Exercise", "Sets"),
            exstractor = { exercise -> exercise.sets.size.toString() },
            ratio = Ratio.Strict(0.9f),
            navController = navController,
            overlayingContent = {
                ElevatedButton(
                    onClick = { trainViewModel.startWorkout(program.nextDay(), program) },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(text = "Start ${program.nextDay().name}")
                }
            })
    }

    Row {
        val (lo, hi) = when {
            programs.size < 5 -> 0 to programs.size
            selectedProgramIndex < 2 -> 0 to 5
            selectedProgramIndex >= programs.size - 2 -> programs.size - 5 to programs.size
            else -> selectedProgramIndex - 2 to selectedProgramIndex + 3
        }
        for (i in lo until hi) {
            RadioButton(
                selected = selectedProgramIndex == i,
                onClick = { selectedProgramIndex = i },
            )
        }
    }

    OutlinedButton(modifier = Modifier.width(220.dp), onClick = { trainViewModel.startWorkout(Day(), Program.EmptyWorkout) }) {
        Text(text = "Start Empty Workout")
    }
}
