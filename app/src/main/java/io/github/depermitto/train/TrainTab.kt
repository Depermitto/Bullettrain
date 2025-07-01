package io.github.depermitto.train

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.depermitto.Screen
import io.github.depermitto.components.WorkoutInfo
import io.github.depermitto.data.entities.ProgramDao
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.ItemSpacing
import io.github.depermitto.theme.filledContainerColor
import kotlinx.coroutines.runBlocking

// TODO THIS BRANCH empty workout starter
@Composable
fun TrainTab(
    modifier: Modifier = Modifier,
    trainViewModel: TrainViewModel,
    programDao: ProgramDao,
    navController: NavController,
): Unit = Column(
    modifier = modifier.padding(horizontal = ItemPadding),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
) {
    val programs = runBlocking { programDao.getAll() }
    var selectedProgramIndex by remember { mutableIntStateOf(0) }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(0.dp, 350.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = filledContainerColor())
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = ItemSpacing)
        ) {
            if (programs.isEmpty()) {
                Text(
                    modifier = Modifier
                        .padding(ItemPadding)
                        .align(Alignment.Center),
                    text = "No Program Found"
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
                    modifier = Modifier.align(Alignment.BottomCenter), onClick = {
                        trainViewModel.startWorkout(program.days[program.nextDay], program)
                        navController.navigate(Screen.TrainingScreen.route) {
                            popUpTo(Screen.MainScreen.route) { inclusive = true }
                        }
                    }, colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) { Text(text = "Start Week ${program.weekStreak}, Day ${program.nextDay + 1}") }
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
}
