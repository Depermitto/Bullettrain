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
import io.github.depermitto.bullettrain.components.DataPanel
import io.github.depermitto.bullettrain.components.ExtendedListItem
import io.github.depermitto.bullettrain.components.format
import io.github.depermitto.bullettrain.protos.ExercisesProto.Exercise
import io.github.depermitto.bullettrain.protos.SettingsProto.*
import io.github.depermitto.bullettrain.theme.Medium
import io.github.depermitto.bullettrain.theme.Small
import io.github.depermitto.bullettrain.util.weightUnit

/**
 * Show sets in a listing for [exercises]. Shows only completed sets and guarantees that at least
 * one set will be shown per exercise, otherwise exercise is skipped.
 */
@Composable
fun ExercisesSetsListings(
  modifier: Modifier = Modifier,
  exercises: List<Exercise>,
  exerciseHeadline: @Composable (Exercise) -> Unit,
  settings: Settings,
) {
  LazyColumn(
    modifier = modifier,
    contentPadding = PaddingValues(start = Dp.Medium, end = Dp.Medium),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(Dp.Small),
  ) {
    items(exercises) { exercise ->
      val sets = exercise.setsList.filter { set -> set.hasDoneTs() }
      if (sets.isEmpty()) return@items
      DataPanel(
        items = sets,
        separateHeaderAndContent = false,
        headline = {
          ExtendedListItem(
            headlineContent = { exerciseHeadline(exercise) },
            headlineTextStyle = MaterialTheme.typography.titleMedium,
            contentPadding = PaddingValues(Dp.Medium),
          )
        },
        headerPadding = PaddingValues(horizontal = Dp.Medium),
        headerContent = {
          Text("Set", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5F))
          Spacer(Modifier.weight(1F))
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
                  val actual = set.actual.format()
                  val weight = set.weight.format()
                  when {
                    weight.isBlank() -> "$actual ${exercise.type}"
                    else -> "$actual x $weight ${settings.unitSystem.weightUnit()}"
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
