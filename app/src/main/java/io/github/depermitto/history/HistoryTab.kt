package io.github.depermitto.history

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.depermitto.components.WorkoutInfo
import io.github.depermitto.components.encodeToStringOutput
import io.github.depermitto.database.HistoryDao
import io.github.depermitto.database.SettingsDao
import io.github.depermitto.home.HomeViewModel
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.ItemSpacing
import io.github.depermitto.theme.filledContainerColor
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HistoryTab(
    modifier: Modifier = Modifier, homeViewModel: HomeViewModel, settingsDao: SettingsDao, historyDao: HistoryDao
) = Box(modifier = modifier.fillMaxSize()) {
    val historyRecords by historyDao.where(homeViewModel.date.month, homeViewModel.date.year)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    LaunchedEffect(historyRecords) { homeViewModel.selectedRecord = historyRecords.firstOrNull() }

    fun findWorkout(calendarDay: LocalDate) = historyRecords.find { record ->
        val recordDate = record.date.atZone(ZoneId.systemDefault())
        calendarDay.dayOfMonth == recordDate.dayOfMonth && calendarDay.month == recordDate.month
    }

    Scaffold(modifier = Modifier.padding(horizontal = ItemPadding), topBar = {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { homeViewModel.date = homeViewModel.date.minusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
            Text(
                modifier = Modifier.weight(1f),
                text = homeViewModel.date.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = { homeViewModel.date = homeViewModel.date.plusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
            }
        }
    }) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ItemSpacing),
        ) {
            Calendar(
                date = homeViewModel.date,
                modifier = Modifier.heightIn(0.dp, 350.dp),
                onItemClick = { homeViewModel.selectedRecord = findWorkout(it) },
                ifHighlightItem = { findWorkout(it) != null },
            )

            homeViewModel.selectedRecord?.let { record ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(containerColor = filledContainerColor())
                ) {
                    if (record.workout.exercises.isEmpty()) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(ItemPadding),
                            text = "Empty Workout",
                            textAlign = TextAlign.Center
                        )
                        return@OutlinedCard
                    }

                    WorkoutInfo(modifier = Modifier.fillMaxWidth(),
                        workout = record.workout,
                        program = record.relatedProgram,
                        exerciseInfo = { exercise ->
                            val scroll = rememberScrollState(0)
                            Text(modifier = Modifier.horizontalScroll(scroll),
                                text = exercise.sets.groupBy { it.weight }.map { (weight, sets) ->
                                    "${sets.size} x ${weight.encodeToStringOutput().ifBlank { 0 }}"
                                }.joinToString(", ") + " " + settingsDao.weightUnit(),
                                maxLines = 1,
                            )
                        })
                }
            }
        }
    }

    val today = LocalDate.now()
    if (homeViewModel.date.month != today.month || homeViewModel.date.year != today.year) TextButton(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = ItemPadding),
        onClick = { homeViewModel.date = today },
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        elevation = ButtonDefaults.buttonElevation()
    ) {
        Icon(modifier = Modifier.size(ButtonDefaults.IconSize), imageVector = Icons.Filled.Refresh, contentDescription = null)
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text("Reset Date")
    }
}
