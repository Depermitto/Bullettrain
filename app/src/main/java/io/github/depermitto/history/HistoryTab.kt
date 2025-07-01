package io.github.depermitto.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.depermitto.data.entities.Day
import io.github.depermitto.data.entities.HistoryDao
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.filledContainerColor
import java.time.LocalDate
import java.time.ZoneId

// TODO Actually make it worth being here, some stats, a finder per date, calendar and maybe some graphs would be good too
@Composable
fun HistoryTab(modifier: Modifier = Modifier, historyDao: HistoryDao) = Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = ItemPadding),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    val historyRecords by historyDao.getAllFlow().collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedWorkout: Day? by remember { mutableStateOf(null) }

    fun findWorkout(calendarDay: LocalDate) = historyRecords.find { record ->
        val recordDate = record.date.atZone(ZoneId.systemDefault())
        calendarDay.dayOfMonth == recordDate.dayOfMonth && calendarDay.month == recordDate.month
    }

    Calendar(
        modifier = modifier.heightIn(0.dp, 300.dp),
        onItemClick = { selectedWorkout = findWorkout(it)?.day },
        ifHighlightItem = { findWorkout(it) != null },
    )

    selectedWorkout?.let { workout ->
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(containerColor = filledContainerColor())
        ) {
            if (workout.exercises.isEmpty()) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ItemPadding),
                    text = "Empty Workout",
                    textAlign = TextAlign.Center
                )
                return@OutlinedCard
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ItemPadding)
            ) {
                items(workout.exercises) { exercise ->
                    Row {
                        Text(text = exercise.name)
                        Text(
                            text = exercise.sets.size.toString() + " sets", style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
