package io.github.depermitto.bullettrain.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.depermitto.bullettrain.components.BasicTable
import io.github.depermitto.bullettrain.components.GhostCard
import io.github.depermitto.bullettrain.components.encodeToStringOutput
import io.github.depermitto.bullettrain.database.Exercise
import io.github.depermitto.bullettrain.database.HistoryDao
import io.github.depermitto.bullettrain.database.SettingsDao
import io.github.depermitto.bullettrain.theme.RegularPadding
import io.github.depermitto.bullettrain.theme.RegularSpacing
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ExerciseScreen(modifier: Modifier = Modifier, historyDao: HistoryDao, settingsDao: SettingsDao, exercise: Exercise) {
    val loggedExercises by historyDao.where(exercise).collectAsStateWithLifecycle(initialValue = emptyList())
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM dd yyyy")

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = RegularPadding, end = RegularPadding, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RegularSpacing),
    ) {
        items(loggedExercises) { exercise ->
            val doneDate = exercise.sets.getOrElse(0) { return@items }.doneTs?.atZone(ZoneId.systemDefault()) ?: return@items
            GhostCard {
                BasicTable(
                    headers = Pair("Set", "Completed"), list = exercise.sets, separateHeadersAndContent = false,
                    headlineContent = { Text(dateFormatter.format(doneDate), style = MaterialTheme.typography.titleMedium) },
                ) { setIndex, set ->
                    Pair(
                        "${setIndex + 1}",
                        "${set.actualPerfVar.encodeToStringOutput()} x ${set.weight.encodeToStringOutput()} ${settingsDao.weightUnit()}"
                    )
                }
            }
        }
    }
}

