package io.github.depermitto.history

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.depermitto.components.WorkoutInfo
import io.github.depermitto.components.encodeToStringOutput
import io.github.depermitto.data.entities.HistoryDao
import io.github.depermitto.data.entities.HistoryRecord
import io.github.depermitto.settings.SettingsViewModel
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.ItemSpacing
import io.github.depermitto.theme.filledContainerColor
import java.time.LocalDate
import java.time.ZoneId

// TODO Finder per date, and maybe some graphs would be good too
@Composable
fun HistoryTab(modifier: Modifier = Modifier, settingsViewModel: SettingsViewModel, historyDao: HistoryDao) = Column(
    modifier = modifier
        .fillMaxSize()
        .padding(horizontal = ItemPadding),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(ItemSpacing),
) {
    val historyRecords by historyDao.getAllFlow().collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedRecord: HistoryRecord? by remember { mutableStateOf(null) }

    LaunchedEffect(historyRecords) {
        if (selectedRecord == null) selectedRecord = historyRecords.lastOrNull()
    }

    fun findWorkout(calendarDay: LocalDate) = historyRecords.find { record ->
        val recordDate = record.date.atZone(ZoneId.systemDefault())
        calendarDay.dayOfMonth == recordDate.dayOfMonth && calendarDay.month == recordDate.month
    }

    Calendar(
        modifier = Modifier.heightIn(0.dp, 350.dp),
        onItemClick = { selectedRecord = findWorkout(it) },
        ifHighlightItem = { findWorkout(it) != null },
    )

    selectedRecord?.let { record ->
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
                        text = exercise.sets.groupBy { it.weight }
                            .map { (weight, sets) -> "${sets.size} x ${weight.encodeToStringOutput().ifBlank { 0 }}" }
                            .joinToString(", ") + " " + settingsViewModel.weightUnit(),
                        maxLines = 1)
                })
        }
    }
}
