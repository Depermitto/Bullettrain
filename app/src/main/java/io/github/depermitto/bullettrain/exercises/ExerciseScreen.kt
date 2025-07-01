package io.github.depermitto.bullettrain.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.depermitto.bullettrain.components.DataPanel
import io.github.depermitto.bullettrain.components.ExtendedListItem
import io.github.depermitto.bullettrain.components.encodeToStringOutput
import io.github.depermitto.bullettrain.database.daos.HistoryDao
import io.github.depermitto.bullettrain.database.daos.SettingsDao
import io.github.depermitto.bullettrain.database.entities.ExerciseDescriptor
import io.github.depermitto.bullettrain.theme.EmptyScrollSpace
import io.github.depermitto.bullettrain.theme.Medium
import io.github.depermitto.bullettrain.theme.Small
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ExerciseScreen(
  modifier: Modifier = Modifier,
  historyDao: HistoryDao,
  settingsDao: SettingsDao,
  exerciseDescriptor: ExerciseDescriptor,
) {
  val settings by settingsDao.getSettings.collectAsStateWithLifecycle()
  val exercises by
    historyDao.where(exerciseDescriptor).collectAsStateWithLifecycle(initialValue = emptyList())
  val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM dd yyyy")

  LazyColumn(
    modifier = modifier,
    contentPadding =
      PaddingValues(start = Dp.Medium, end = Dp.Medium, bottom = Dp.EmptyScrollSpace),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(Dp.Small),
  ) {
    items(exercises) { exercise ->
      val doneDate =
        exercise.lastPerformedSet()?.doneTs?.atZone(ZoneId.systemDefault()) ?: return@items
      DataPanel(
        items = exercise.getPerformedSets(),
        separateHeaderAndContent = false,
        headline = {
          ExtendedListItem(
            headlineContent = { Text(dateFormatter.format(doneDate)) },
            headlineTextStyle = MaterialTheme.typography.titleMedium,
            contentPadding = PaddingValues(Dp.Medium),
          )
        },
        headerPadding = PaddingValues(horizontal = Dp.Medium),
        headerContent = {
          Text("Set", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
          Spacer(Modifier.weight(1f))
          Text("Completed", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        },
      ) { setIndex, set ->
        ExtendedListItem(
          headlineContent = {
            Text("${setIndex + 1}", maxLines = 1, overflow = TextOverflow.Ellipsis)
          },
          trailingContent = {
            Text(
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              text =
                run {
                  val perfVar = set.actualPerfVar.encodeToStringOutput() // is always non-zero
                  val weight = set.weight.encodeToStringOutput()

                  when {
                    weight.isBlank() ->
                      "$perfVar ${set.targetPerfVar.category.shortName.lowercase()}"
                    else -> "$perfVar x $weight ${settings.unitSystem.weightUnit()}"
                  }
                },
            )
          },
          contentPadding = PaddingValues(horizontal = Dp.Medium),
          headlineTextStyle = MaterialTheme.typography.bodyLarge,
          supportingTextStyle = MaterialTheme.typography.bodyLarge,
        )
      }
    }
  }
}
