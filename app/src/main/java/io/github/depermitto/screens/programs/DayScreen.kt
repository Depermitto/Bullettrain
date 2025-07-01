package io.github.depermitto.screens.programs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.depermitto.components.ExpandableOutlinedCard
import io.github.depermitto.data.Day
import io.github.depermitto.data.ExerciseDao
import io.github.depermitto.replaceAt
import io.github.depermitto.screens.exercises.ExerciseChooser
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.ItemSpacing
import io.github.depermitto.theme.transparentTextFieldColors

@Deprecated(message = "Do something akin to TrainScreen")
@Composable
fun DayScreen(day: Day, onDayChange: (Day?) -> Unit, exerciseDao: ExerciseDao) = ExpandableOutlinedCard(title = {
    TextField(
        value = day.name,
        onValueChange = { onDayChange(day.copy(name = it)) },
        textStyle = MaterialTheme.typography.titleMedium,
        colors = transparentTextFieldColors()
    )
}, dropdownItems = {
    DropdownMenuItem(text = { Text(text = "Delete") },
        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
        onClick = { onDayChange(null) })
}, startExpanded = true) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(ItemPadding), verticalArrangement = Arrangement.spacedBy(ItemSpacing)
    ) {
        day.exercises.forEachIndexed { i, exercises ->
            ExpandableOutlinedCard(title = { Text(text = "${i + 1}. ${exercises.first().name}") }) {
                SetScreen(set = exercises, onSetChange = {
                    if (it == null) onDayChange(day.copy(exercises = day.exercises - setOf(exercises)))
                    else onDayChange(day.copy(exercises = day.exercises.replaceAt(i, it)))
                })
            }
        }

        ExerciseChooser(exerciseDao = exerciseDao,
            onChoose = { onDayChange(day.copy(exercises = day.exercises + setOf(listOf(it)))) })
    }
}

