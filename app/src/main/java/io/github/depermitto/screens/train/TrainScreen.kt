package io.github.depermitto.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import io.github.depermitto.components.ExpandableOutlinedCard
import io.github.depermitto.data.Day
import io.github.depermitto.data.ExerciseDao
import io.github.depermitto.screens.programs.SetScreen
import io.github.depermitto.theme.paddingDp

@Composable
fun TrainScreen(day: Day, exerciseDao: ExerciseDao) {
    var completedSets by remember { mutableStateOf(day.sets) }

    LazyColumn(modifier = Modifier.padding(paddingDp)) {
        itemsIndexed(day.sets) { setIndex, sets ->
            ExpandableOutlinedCard(
                title = { Text(text = "${setIndex + 1}. ${sets.first().name}") },
                startExpanded = true,
            ) {
                SetScreen(set = sets, onSetChange = {}, trainMode = true)
            }
        }
    }
}