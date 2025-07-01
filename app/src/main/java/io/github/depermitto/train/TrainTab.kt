package io.github.depermitto.train

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.depermitto.data.entities.ProgramDao
import io.github.depermitto.Screen
import io.github.depermitto.programs.ProgramInfo
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.ItemSpacing
import io.github.depermitto.theme.filledContainerColor

// TODO P1
//  1. Slideshow of programs to start/track with progress stats
//   - If there is no workout tracked, big-ass button that lets you choose
//  2. Next workout preview with a start Day x button
//  3. Empty workout starter
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
    val programs by programDao.getAll()
        .collectAsStateWithLifecycle(initialValue = emptyList()) // TODO followed programs P2
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
                    text = "No Program Found" // TODO maybe some spinning? P3
                )
            }

            programs.getOrNull(selectedProgramIndex)?.let { program ->
                ProgramInfo(modifier = Modifier.align(Alignment.TopStart), program = program)
                ElevatedButton(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onClick = {
                        navController.navigate(Screen.TrainingScreen.route)
                        trainViewModel.startWorkout(program.days[program.nextDay], program)
                    },
                    colors = ButtonDefaults.elevatedButtonColors(
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
