package io.github.depermitto.history

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

            WorkoutOverview(
                modifier = Modifier.fillMaxWidth(),
                record = record,
                settingsViewModel = settingsViewModel,
            )
        }
    }
}

// TODO make this more general
@Composable
fun WorkoutOverview(
    modifier: Modifier = Modifier,
    record: HistoryRecord,
    settingsViewModel: SettingsViewModel,
) = Column(modifier = modifier.padding(ItemPadding * 2), verticalArrangement = Arrangement.spacedBy(ItemSpacing)) {
    Text(text = record.relatedProgram.name, style = MaterialTheme.typography.titleMedium)
    Text(text = "Day ${record.relatedProgram.nextDay + 1} Week ${record.relatedProgram.weekStreak}")

    Row(
        modifier = Modifier
            .padding(top = ItemPadding * 2)
            .offset(y = ItemSpacing)
    ) {
        Text(text = "Exercise", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "Sets", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
    }
    HorizontalDivider()

    record.workout.exercises.forEach { exercise ->
        Row {
            val scroll = rememberScrollState(0)
            Text(text = exercise.name, maxLines = 1)
            Spacer(modifier = Modifier.weight(1f))
            Text(modifier = Modifier.horizontalScroll(scroll),
                text = exercise.sets.groupBy { it.weight }
                    .map { (weight, sets) -> "${sets.size} x ${weight.encodeToStringOutput()}" }
                    .joinToString(", ") + " " + settingsViewModel.settings.unitSystem.weightUnit(),
                maxLines = 1)
        }
    }
}