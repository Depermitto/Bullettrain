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
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.depermitto.bullettrain.components.BasicTable
import io.github.depermitto.bullettrain.components.encodeToStringOutput
import io.github.depermitto.bullettrain.database.Exercise
import io.github.depermitto.bullettrain.database.HistoryDao
import io.github.depermitto.bullettrain.database.SettingsDao
import io.github.depermitto.bullettrain.theme.RegularPadding
import io.github.depermitto.bullettrain.theme.RegularSpacing
import io.github.depermitto.bullettrain.theme.ScrollPadding
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ExerciseScreen(modifier: Modifier = Modifier, historyDao: HistoryDao, settingsDao: SettingsDao, exercise: Exercise) {
    val exercises by historyDao.where(exercise).collectAsStateWithLifecycle(initialValue = emptyList())
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM dd yyyy")

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = RegularPadding, end = RegularPadding, bottom = ScrollPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RegularSpacing),
    ) {
        items(exercises) { exercise ->
            val doneDate = exercise.lastPerformedSet()?.doneTs?.atZone(ZoneId.systemDefault()) ?: return@items
            BasicTable(
                headers = Pair("Set", "Completed"), list = exercise.getPerformedSets(), separateHeadersAndContent = false,
                headlineContent = { Text(dateFormatter.format(doneDate), style = MaterialTheme.typography.titleMedium) },
            ) { setIndex, set ->
                Pair(first = {
                    Text("${setIndex + 1}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }, second = {
                    Text(
                        text = run {
                            val perfVar = set.actualPerfVar.encodeToStringOutput() // is always non-zero
                            val weight = set.weight.encodeToStringOutput()

                            when {
                                weight.isBlank() -> "$perfVar ${set.targetPerfVar.category.shortName.lowercase()}"
                                else -> "$perfVar x $weight ${settingsDao.weightUnit()}"
                            }
                        }, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                })
            }
        }
    }
}

