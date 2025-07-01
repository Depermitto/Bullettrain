package io.github.depermitto.bullettrain.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.components.WorkoutInfo
import io.github.depermitto.bullettrain.components.encodeToStringOutput
import io.github.depermitto.bullettrain.database.entities.*
import io.github.depermitto.bullettrain.home.HomeViewModel
import io.github.depermitto.bullettrain.theme.Medium
import io.github.depermitto.bullettrain.theme.Small
import io.github.depermitto.bullettrain.theme.focalGround
import io.github.depermitto.bullettrain.train.TrainViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HistoryTab(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel,
    trainViewModel: TrainViewModel,
    historyDao: HistoryDao,
    exerciseDao: ExerciseDao,
    programDao: ProgramDao,
    settings: Settings,
    navController: NavController
) = Box(modifier = modifier.fillMaxSize()) {
    val historyRecords by historyDao.where(homeViewModel.calendarDate.month, homeViewModel.calendarDate.year)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedHistoryRecords = historyRecords.filter { record -> record.date == homeViewModel.selectedDate }
    LaunchedEffect(historyRecords) {
        if (homeViewModel.selectedDate == null) homeViewModel.selectedDate = historyRecords.lastOrNull()?.date
    }

    Column(
        modifier = Modifier
            .padding(horizontal = Dp.Medium)
            .verticalScroll(rememberScrollState())
            .padding(bottom = Dp.Medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dp.Small)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { homeViewModel.calendarDate = homeViewModel.calendarDate.minusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
            Text(
                modifier = Modifier.weight(1f),
                text = homeViewModel.calendarDate.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            IconButton(onClick = { homeViewModel.calendarDate = homeViewModel.calendarDate.plusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
            }
        }

        Calendar(
            homeViewModel = homeViewModel,
            trainViewModel = trainViewModel,
            currentHistoryRecords = historyRecords,
            currentDate = homeViewModel.calendarDate,
            programDao = programDao,
            historyDao = historyDao,
            modifier = Modifier.heightIn(0.dp, 400.dp)
        )

        selectedHistoryRecords.forEach { record ->
            val relatedProgram = programDao.where(record.relatedProgramId)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.focalGround(settings.theme))) {
                WorkoutInfo(
                    modifier = Modifier.fillMaxWidth(),
                    workout = record.workout,
                    program = relatedProgram,
                    headers = listOf("Exercise", "Best Set"),
                    navController = navController,
                    exerciseDao = exerciseDao,
                    trailingContent = {
                        TextButton(onClick = { trainViewModel.editWorkout(record) }) {
                            Icon(Icons.Filled.Edit, "Edit Workout", Modifier.size(ButtonDefaults.IconSize))
                            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                            Text("Edit")
                        }
                    },
                    exstractor = { exercise ->
                        exercise.getPerformedSets().maxByOrNull { set -> set.weight * set.actualPerfVar }?.let { bestSet ->
                            val perfVar = bestSet.actualPerfVar.encodeToStringOutput() // is always non-zero
                            val weight = bestSet.weight.encodeToStringOutput()

                            when {
                                weight.isBlank() -> "$perfVar ${bestSet.targetPerfVar.category.shortName.lowercase()}"
                                else -> "$perfVar x $weight ${settings.unitSystem.weightUnit()}"
                            }
                        }
                    },
                )
            }
        }
    }

    val today = LocalDate.now()
    AnimatedVisibility(
        modifier = Modifier.align(Alignment.BottomCenter),
        visible = homeViewModel.calendarDate.month != today.month || homeViewModel.calendarDate.year != today.year,
        enter = slideInVertically(animationSpec = tween(durationMillis = 600, easing = EaseInCubic), initialOffsetY = { it }),
        exit = slideOutVertically(animationSpec = tween(durationMillis = 200), targetOffsetY = { it }),
    ) {
        TextButton(
            modifier = Modifier.padding(bottom = Dp.Medium),
            onClick = { homeViewModel.calendarDate = today },
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            elevation = ButtonDefaults.buttonElevation()
        ) {
            Icon(modifier = Modifier.size(ButtonDefaults.IconSize), imageVector = Icons.Filled.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text("Reset Date")
        }
    }
}