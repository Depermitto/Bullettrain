package io.github.depermitto.bullettrain.exercises

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.depermitto.bullettrain.components.AnchoredFloatingActionButton
import io.github.depermitto.bullettrain.components.ExtendedListItem
import io.github.depermitto.bullettrain.components.TextFieldAlertDialog
import io.github.depermitto.bullettrain.db.ExerciseDao
import io.github.depermitto.bullettrain.db.HistoryDao
import io.github.depermitto.bullettrain.protos.ExercisesProto.*
import io.github.depermitto.bullettrain.theme.Medium
import io.github.depermitto.bullettrain.theme.unlinedColors
import io.github.depermitto.bullettrain.util.capwords
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun ExercisesListScreen(
  exerciseDao: ExerciseDao,
  historyDao: HistoryDao,
  modifier: Modifier = Modifier,
  exclude: List<Int> = emptyList(),
  onSelection: (Exercise.Descriptor) -> Unit,
) {
  Box(modifier = Modifier.fillMaxHeight() then modifier) {
    val exerciseFrequencyMap by
      historyDao.getSortedByFrequency.collectAsStateWithLifecycle(emptyMap())

    var searchText by rememberSaveable { mutableStateOf("") }
    val descriptors by
      exerciseDao
        .getByName(name = searchText, errorTolerance = 3, ignoreCase = true)
        .map { descriptors ->
          descriptors
            .filterNot { d -> exclude.contains(d.id) }
            .sortedByDescending { d -> exerciseFrequencyMap[d.id] }
        }
        .collectAsStateWithLifecycle(emptyList())

    Column(modifier = Modifier.align(Alignment.TopCenter)) {
      TextField(
        modifier =
          Modifier.fillMaxWidth()
            .padding(start = Dp.Medium, end = Dp.Medium, bottom = 12.dp)
            .clip(shape = RoundedCornerShape(24.dp))
            .shadow(2.dp, shape = RoundedCornerShape(24.dp)),
        value = searchText,
        onValueChange = { searchText = it },
        shape = RoundedCornerShape(24.dp),
        colors = TextFieldDefaults.unlinedColors(),
        maxLines = 1,
        singleLine = true,
        placeholder = { Text("Search Exercises") },
        leadingIcon = { Icon(Icons.Default.Search, "Search exercises") },
      )

      LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        items(descriptors) { descriptor ->
          val count = exerciseFrequencyMap[descriptor.id]
          ExtendedListItem(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onSelection(descriptor) },
            headlineContent = { Text(descriptor.name) },
            supportingContent = {
              if (count != null) Text("$count ${if (count == 1) "record" else "records"}")
            },
          )
        }
      }
    }

    var showDialog by rememberSaveable { mutableStateOf(false) }
    AnchoredFloatingActionButton(
      onClick = { showDialog = true },
      icon = { Icon(Icons.Default.Add, "Add exercise") },
      text = { Text("Add") },
    )

    if (showDialog) {
      val scope = rememberCoroutineScope()
      var errorMessage by rememberSaveable { mutableStateOf("") }
      TextFieldAlertDialog(
        onDismissRequest = { showDialog = false },
        label = { Text("Exercise Name") },
        dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
        confirmButton = { name ->
          TextButton(
            onClick = {
              scope.launch {
                errorMessage =
                  if (name.isBlank()) {
                    "Empty exercise name"
                  } else if (
                    exerciseDao.getVisible.first().any { d -> d.name == name.capwords() }
                  ) {
                    "Duplicate exercise name"
                  } else {
                    showDialog = false
                    exerciseDao.insert(Exercise.Descriptor.newBuilder().setName(name).build())
                    ""
                  }
              }
            }
          ) {
            Text("Confirm")
          }
        },
        errorMessage = errorMessage,
        isError = errorMessage.isNotBlank(),
      )
    }
  }
}
