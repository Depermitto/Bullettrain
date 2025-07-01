package io.github.depermitto.bullettrain.train

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.depermitto.bullettrain.components.WorkoutInfo
import io.github.depermitto.bullettrain.database.Day
import io.github.depermitto.bullettrain.database.ProgramDao
import io.github.depermitto.bullettrain.theme.ItemPadding
import io.github.depermitto.bullettrain.theme.ItemSpacing
import io.github.depermitto.bullettrain.theme.filledContainerColor
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

@Composable
fun TrainTab(
    modifier: Modifier = Modifier,
    trainViewModel: TrainViewModel,
    programDao: ProgramDao,
): Unit = Column(
    modifier = modifier.padding(horizontal = ItemPadding),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
) {
    val programs = runBlocking { programDao.getAlmostAll.firstOrNull() ?: emptyList() }
    var selectedProgramIndex by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(0.dp, 350.dp),
        colors = CardDefaults.cardColors(containerColor = filledContainerColor())
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = ItemSpacing)
        ) {
            if (programs.isEmpty()) {
                Text(
                    modifier = Modifier.Companion
                        .padding(ItemPadding)
                        .align(Alignment.Center), text = "No Program Found"
                )
            }

            programs.getOrNull(selectedProgramIndex)?.let { program ->
                WorkoutInfo(modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart),
                    workout = program.days[program.nextDay],
                    program = program,
                    exerciseInfo = { Text(text = it.sets.size.toString()) })
                ElevatedButton(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onClick = { trainViewModel.startWorkout(program.days[program.nextDay], program) },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) { Text(text = "Start Week ${program.weekStreak}, ${program.days[program.nextDay].name}") }
            }
        }
    }

    Row {
        programs.forEachIndexed { i, _ ->
            RadioButton(
                selected = selectedProgramIndex == i,
                onClick = { selectedProgramIndex = i },
            )
        }
    }

    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = { trainViewModel.startWorkout(Day(), ProgramDao.Companion.EmptyWorkout) }) {
        Text(text = "Start Empty Workout")
    }
}
