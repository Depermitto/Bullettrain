package io.github.depermitto.train

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.github.depermitto.components.AnchoredFloatingActionButton
import io.github.depermitto.data.ExerciseDao
import io.github.depermitto.data.HistoryDao
import io.github.depermitto.data.ProgramDao
import io.github.depermitto.programs.ProgramInfo
import io.github.depermitto.screen.Screen
import io.github.depermitto.settings.SettingsViewModel
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.ItemSpacing
import io.github.depermitto.theme.filledContainerColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun TrainTab(
    settingsViewModel: SettingsViewModel,
    historyDao: HistoryDao,
    programDao: ProgramDao,
    exerciseDao: ExerciseDao,
    navController: NavController,
) {
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val activeProgram by programDao.getActiveProgram().collectAsStateWithLifecycle(initialValue = null)

    Box(modifier = Modifier.fillMaxSize()) {
        activeProgram?.let { program ->
            val trainViewModel =
                viewModel<TrainViewModel>(factory = TrainViewModel.Factory(program.days[program.nextDay], historyDao))
            TrainingScreen(trainViewModel, settingsViewModel, exerciseDao)
//            WorkoutPreview()
        }

        if (activeProgram == null) {
            val programs by programDao.getAllFlow().collectAsStateWithLifecycle(initialValue = emptyList())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = ItemPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ItemSpacing),
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(ItemPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Active Program Found",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "Please choose one from the list below",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(programs) { program ->
                        OutlinedCard(colors = CardDefaults.cardColors(containerColor = filledContainerColor()),
                            onClick = { scope.launch { programDao.upsert(program.copy(active = true)) } }) {
                            ProgramInfo(modifier = Modifier.fillMaxWidth(), program = program)
                        }
                    }
                }
            }

            AnchoredFloatingActionButton(onClick = { navController.navigate(Screen.MainScreen.passTab(Screen.MainScreen.Tabs.Programs)) },
                icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) })
        }
    }
}

@Composable
fun WorkoutPreview() {

}