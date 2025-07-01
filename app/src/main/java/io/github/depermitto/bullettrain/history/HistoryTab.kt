package io.github.depermitto.bullettrain.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.depermitto.bullettrain.components.WorkoutInfo
import io.github.depermitto.bullettrain.components.encodeToStringOutput
import io.github.depermitto.bullettrain.database.HistoryDao
import io.github.depermitto.bullettrain.database.SettingsDao
import io.github.depermitto.bullettrain.home.HomeViewModel
import io.github.depermitto.bullettrain.theme.ItemPadding
import io.github.depermitto.bullettrain.theme.ItemSpacing
import io.github.depermitto.bullettrain.theme.filledContainerColor
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HistoryTab(
    modifier: Modifier = Modifier, homeViewModel: HomeViewModel, settingsDao: SettingsDao, historyDao: HistoryDao
) = Box(modifier = modifier.fillMaxSize()) {
    val historyRecords by historyDao.where(homeViewModel.date.month, homeViewModel.date.year)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    LaunchedEffect(historyRecords) { homeViewModel.selectedRecord = historyRecords.lastOrNull() }

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
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState(0)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ItemSpacing),
        ) {
            var dragDirection by remember { mutableFloatStateOf(0f) }
            Calendar(
                date = homeViewModel.date,
                modifier = Modifier
                    .heightIn(0.dp, 350.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(onDragEnd = {
                            when {
                                dragDirection > 0 -> homeViewModel.date = homeViewModel.date.minusMonths(1)
                                dragDirection < 0 -> homeViewModel.date = homeViewModel.date.plusMonths(1)
                            }
                        }, onDrag = { _, dragAmount -> dragDirection = dragAmount.x })
                    },
                onItemClick = { homeViewModel.selectedRecord = findWorkout(it) },
                ifHighlightItem = { findWorkout(it) != null },
            )

            homeViewModel.selectedRecord?.let { record ->
                Card(
                    modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = filledContainerColor())
                ) {
                    WorkoutInfo(
                        modifier = Modifier.fillMaxWidth(),
                        workout = record.workout,
                        program = record.relatedProgram,
                        map = { exercises ->
                            val summaries = exercises.map { exercise ->
                                val setsGroupedByWeight = exercise.sets
                                    .groupBy { it.weight }
                                    .filter { (weight, _) -> weight != 0f }
                                if (setsGroupedByWeight.isNotEmpty()) setsGroupedByWeight
                                    .map { (weight, sets) -> "${sets.size}x${weight.encodeToStringOutput()}" }
                                    .joinToString(", ", postfix = " " + settingsDao.weightUnit())
                                else ""
                            }
                            summaries.filter { it.isNotBlank() }
                        }
                    )
                }
            }
        }
    }

    val today = LocalDate.now()
    AnimatedVisibility(
        modifier = Modifier.align(Alignment.BottomCenter),
        visible = homeViewModel.date.month != today.month || homeViewModel.date.year != today.year,
        enter = slideInVertically(animationSpec = tween(durationMillis = 600, easing = EaseInCubic), initialOffsetY = { it }),
        exit = slideOutVertically(animationSpec = tween(durationMillis = 600, easing = EaseInCubic), targetOffsetY = { it }),
    ) {
        TextButton(
            modifier = Modifier.padding(bottom = ItemPadding),
            onClick = { homeViewModel.date = today },
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            elevation = ButtonDefaults.buttonElevation()
        ) {
            Icon(modifier = Modifier.size(ButtonDefaults.IconSize), imageVector = Icons.Filled.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text("Reset Date")
        }
    }
}