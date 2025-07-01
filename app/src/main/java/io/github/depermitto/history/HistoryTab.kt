package io.github.depermitto.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.depermitto.data.entities.HistoryDao
import io.github.depermitto.exercises.Exercise
import io.github.depermitto.theme.ItemPadding

// TODO Actually make it worth being here, some stats, a finder per date, calendar and maybe some graphs would be good too
@Composable
fun HistoryTab(modifier: Modifier = Modifier, historyDao: HistoryDao) {
    val historyRecords by historyDao.getAllFlow().collectAsStateWithLifecycle(initialValue = emptyList())

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        items(historyRecords) { historyRecord ->
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ItemPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = historyRecord.workoutPhase.name)
                    historyRecord.day.exercises.forEach { exercise ->
                        Exercise(
                            exercise = exercise,
                            onExerciseChange = { }
                        )
                    }
                }
            }
        }
    }
}
