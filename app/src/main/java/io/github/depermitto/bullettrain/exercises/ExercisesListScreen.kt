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
import io.github.depermitto.bullettrain.components.TextFieldAlertDialog
import io.github.depermitto.bullettrain.components.Tile
import io.github.depermitto.bullettrain.database.entities.ExerciseDao
import io.github.depermitto.bullettrain.database.entities.ExerciseDescriptor
import io.github.depermitto.bullettrain.database.entities.HistoryDao
import io.github.depermitto.bullettrain.theme.EmptyScrollSpace
import io.github.depermitto.bullettrain.theme.Medium
import io.github.depermitto.bullettrain.theme.unlinedColors
import kotlinx.coroutines.flow.map

@Composable
fun ExercisesListScreen(
  exerciseDao: ExerciseDao,
  historyDao: HistoryDao,
  modifier: Modifier = Modifier,
  filter: ((ExerciseDescriptor) -> Boolean)? = null,
  onSelection: (ExerciseDescriptor) -> Unit,
) =
  Box(modifier = modifier.fillMaxSize()) {
    val exerciseFrequencyMap by
      historyDao.getAll
        .map { records ->
          records
            .flatMap { record ->
              record.workout.entries.filter { entry -> entry.sets.any { set -> set.completed } }
            }
            .groupingBy { entry -> entry.descriptorId }
            .eachCount()
        }
        .collectAsStateWithLifecycle(initialValue = emptyMap())

    var searchText by rememberSaveable { mutableStateOf("") }
    val exercises by
      exerciseDao
        .where(name = searchText, errorTolerance = 3, ignoreCase = true)
        .map { exercises ->
          if (filter != null) {
              exercises.filter(filter)
            } else {
              exercises
            }
            .sortedByDescending { exerciseFrequencyMap[it.id] }
        }
        .collectAsStateWithLifecycle(initialValue = emptyList())

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
        placeholder = { Text(text = "Search Exercises") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Exercises") },
      )

      LazyColumn(
        contentPadding = PaddingValues(bottom = Dp.EmptyScrollSpace),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        items(exercises) { exerciseDescriptor ->
          val count = exerciseFrequencyMap[exerciseDescriptor.id]
          Tile(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onSelection(exerciseDescriptor) },
            headlineContent = { Text(exerciseDescriptor.name) },
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
      icon = { Icon(Icons.Default.Add, contentDescription = "Add Exercise") },
      text = { Text(text = "Add") },
    )

    if (showDialog) {
      var errorMessage by rememberSaveable { mutableStateOf("") }
      TextFieldAlertDialog(
        onDismissRequest = { showDialog = false },
        label = { Text("Exercise Name") },
        dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
        confirmButton = { name ->
          TextButton(
            onClick = {
              errorMessage =
                exerciseDao.validateName(name)
                  ?: ""
                    .also {
                      showDialog = false
                      exerciseDao.insert(ExerciseDescriptor(name = name))
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
