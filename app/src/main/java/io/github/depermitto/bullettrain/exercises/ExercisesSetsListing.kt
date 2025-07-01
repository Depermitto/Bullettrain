package io.github.depermitto.bullettrain.exercises

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import io.github.depermitto.bullettrain.components.DataPanel
import io.github.depermitto.bullettrain.components.ExtendedListItem
import io.github.depermitto.bullettrain.components.format
import io.github.depermitto.bullettrain.protos.ExercisesProto.Exercise
import io.github.depermitto.bullettrain.protos.SettingsProto.*
import io.github.depermitto.bullettrain.theme.Large
import io.github.depermitto.bullettrain.theme.Medium
import io.github.depermitto.bullettrain.theme.focalGround
import io.github.depermitto.bullettrain.util.weightUnit

/**
 * Show sets in a listing for [exercises]. Shows only completed sets and guarantees that at least
 * one set will be shown per exercise, otherwise the exercise is skipped.
 */
@Composable
fun ExercisesSetsListings(
  modifier: Modifier = Modifier,
  exercises: List<Exercise>,
  headline: @Composable (Exercise) -> Unit,
  supportingContent: @Composable (Exercise) -> Unit,
  settings: Settings,
  scroll: ScrollState = rememberScrollState(),
) {
  Column(
    modifier = modifier.verticalScroll(scroll).padding(horizontal = Dp.Medium),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(Dp.Medium),
  ) {
    for (exercise in exercises) {
      val sets = exercise.setsList.filter { s -> s.hasDoneTs() }
      if (sets.isEmpty()) continue
      DataPanel(
        items = sets,
        separateHeaderAndContent = false,
        colors = CardDefaults.cardColors(containerColor = focalGround(settings.theme)),
        headline = {
          ExtendedListItem(
            headlineContent = { headline(exercise) },
            headlineTextStyle = MaterialTheme.typography.titleMedium,
            supportingContent = { supportingContent(exercise) },
            contentPadding = PaddingValues(Dp.Large),
          )
        },
        headerPadding = PaddingValues(horizontal = Dp.Large),
        headerContent = {
          Text("Set", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5F))
          Spacer(Modifier.weight(1F))
          Text("Completed", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        },
        contentPadding = PaddingValues(horizontal = Dp.Medium),
      ) { setIndex, set ->
        ExtendedListItem(
          headlineContent = {
            Text("${setIndex + 1}", maxLines = 1, overflow = TextOverflow.Ellipsis)
          },
          trailingContent = {
            Text(
              run {
                val actual = set.actual.format()
                val weight = set.weight.format()
                when {
                  weight.isBlank() -> "$actual reps"
                  else -> "$actual x $weight ${settings.unitSystem.weightUnit()}"
                }
              },
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
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
