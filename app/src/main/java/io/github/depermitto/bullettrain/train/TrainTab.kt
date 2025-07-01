package io.github.depermitto.bullettrain.train

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.github.depermitto.bullettrain.components.WorkoutInfo
import io.github.depermitto.bullettrain.database.Day
import io.github.depermitto.bullettrain.database.ProgramDao
import io.github.depermitto.bullettrain.theme.ItemPadding
import io.github.depermitto.bullettrain.theme.ItemSpacing
import io.github.depermitto.bullettrain.theme.filledContainerColor
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlin.math.max
import kotlin.math.min

@Composable
fun TrainTab(
    modifier: Modifier = Modifier,
    trainViewModel: TrainViewModel,
    programDao: ProgramDao,
) = Column(
    modifier = modifier.padding(horizontal = ItemPadding),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
) {
    val programs = runBlocking { programDao.getAlmostAll.firstOrNull() ?: emptyList() }
    var selectedProgramIndex by rememberSaveable { mutableIntStateOf(0) }
    var dragDirection by remember { mutableFloatStateOf(0f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(0.dp, 350.dp)
            .pointerInput(Unit) {
                detectDragGestures(onDragEnd = {
                    when {
                        dragDirection > 0 -> selectedProgramIndex = max(selectedProgramIndex - 1, 0)
                        dragDirection < 0 -> selectedProgramIndex = min(selectedProgramIndex + 1, programs.size - 1)
                    }
                }, onDrag = { _, dragAmount -> dragDirection = dragAmount.x })
            }, colors = CardDefaults.cardColors(containerColor = filledContainerColor())
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = ItemSpacing)
        ) {
            if (programs.isEmpty()) Text(
                modifier = Modifier
                    .padding(ItemPadding)
                    .align(Alignment.Center), text = "No Program Found"
            )

            programs.getOrNull(selectedProgramIndex)?.let { program ->
                WorkoutInfo(modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart),
                    workout = program.days[program.nextDay],
                    program = program,
                    map = { exercises -> exercises.map { exercise -> exercise.sets.size.toString() } })
                ElevatedButton(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onClick = { trainViewModel.startWorkout(program.days[program.nextDay], program) },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(text = "Start ${program.days[program.nextDay].name}, Week ${program.weekStreak}")
                }
            }
        }
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

    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = { trainViewModel.startWorkout(Day(), ProgramDao.EmptyWorkout) }) {
        Text(text = "Start Empty Workout")
    }
}
