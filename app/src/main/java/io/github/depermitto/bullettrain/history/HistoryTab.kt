package io.github.depermitto.bullettrain.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destination
import io.github.depermitto.bullettrain.components.DataPanel
import io.github.depermitto.bullettrain.components.Tile
import io.github.depermitto.bullettrain.components.encodeToStringOutput
import io.github.depermitto.bullettrain.database.entities.*
import io.github.depermitto.bullettrain.home.HomeViewModel
import io.github.depermitto.bullettrain.theme.EmptyScrollSpace
import io.github.depermitto.bullettrain.theme.Large
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
    val today = LocalDate.now()
    val resetDateButtonVisible = homeViewModel.calendarDate.month != today.month || homeViewModel.calendarDate.year != today.year

    Column(
        modifier = Modifier
            .padding(horizontal = Dp.Medium)
            .verticalScroll(rememberScrollState())
            .padding(bottom = if (resetDateButtonVisible) Dp.EmptyScrollSpace / 4 else Dp.Medium),
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
            val plannedWorkout = relatedProgram.workouts.first { it.name == record.workout.name }
            DataPanel<WorkoutEntry>(
                modifier = Modifier.fillMaxWidth(),
                items = record.workout.entries.filter { workoutEntry ->
                    val skipped = workoutEntry.getPerformedSets().isEmpty()
                    val planned = plannedWorkout.entries.any { it.descriptorId == workoutEntry.descriptorId }
                    !skipped || planned
                },
                backgroundColor = MaterialTheme.colorScheme.focalGround(settings.theme),
                headline = {
                    Tile(headlineContent = { Text(text = relatedProgram.name) },
                        headlineTextStyle = MaterialTheme.typography.titleLarge,
                        supportingContent = { Text(text = record.workout.name) },
                        trailingContent = {
                            TextButton(onClick = { trainViewModel.editWorkout(record) }) {
                                Icon(Icons.Filled.Edit, "Edit Workout", Modifier.size(ButtonDefaults.IconSize))
                                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                                Text("Edit")
                            }
                        })
                },
                headerTextStyle = MaterialTheme.typography.bodyLarge,
                headerContent = {
                    Text("Set", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    Spacer(Modifier.weight(1f))
                    Text("Best Set", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                },
                contentPadding = PaddingValues(horizontal = Dp.Large),
            ) { entryIndex, entry ->
                val bestSet = entry.getPerformedSets().maxByOrNull { set -> set.weight * set.actualPerfVar }?.let { bestSet ->
                    val perfVar = bestSet.actualPerfVar.encodeToStringOutput() // is always non-zero
                    val weight = bestSet.weight.encodeToStringOutput()

                    when {
                        weight.isBlank() -> "$perfVar ${bestSet.targetPerfVar.category.shortName.lowercase()}"
                        else -> "$perfVar x $weight ${settings.unitSystem.weightUnit()}"
                    }
                }

                val exerciseDescriptor = exerciseDao.where(entry.descriptorId)
                Tile(headlineContent = { Text(text = exerciseDescriptor.name, maxLines = 2) },
                    trailingContent = { Text(text = bestSet ?: "skipped", overflow = TextOverflow.Ellipsis, maxLines = 2) },
                    modifier = Modifier.clip(MaterialTheme.shapes.small),
                    onClick = { navController.navigate(Destination.Exercise(exerciseDescriptor.id)) },
                    contentPadding = PaddingValues(0.dp)
                )
            }
        }
    }

    AnimatedVisibility(
        visible = resetDateButtonVisible,
        modifier = Modifier.align(Alignment.BottomCenter),
        enter = slideInVertically(animationSpec = tween(durationMillis = 600), initialOffsetY = { it }),
        exit = fadeOut(),
    ) {
        TextButton(
            modifier = Modifier.padding(bottom = Dp.Medium),
            onClick = { homeViewModel.calendarDate = today },
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.onBackground,
                containerColor = MaterialTheme.colorScheme.background,
            ),
            elevation = ButtonDefaults.buttonElevation()
        ) {
            Icon(modifier = Modifier.size(ButtonDefaults.IconSize), imageVector = Icons.Filled.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text("Reset Date")
        }
    }
}