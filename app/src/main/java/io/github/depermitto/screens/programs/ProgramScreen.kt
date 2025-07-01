package io.github.depermitto.screens.programs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.depermitto.data.Day
import io.github.depermitto.data.ExerciseDao
import io.github.depermitto.presentation.ProgramViewModel
import io.github.depermitto.replaceAt
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.ItemSpacing

@Composable
fun ProgramScreen(
    programViewModel: ProgramViewModel,
    exerciseDao: ExerciseDao,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(ItemPadding),
        verticalArrangement = Arrangement.spacedBy(ItemSpacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(programViewModel.days) { i, day ->
            DayScreen(
                day = day, onDayChange = {
                    if (it != null) programViewModel.days = programViewModel.days.replaceAt(i, it)
                    else programViewModel.days -= day
                }, exerciseDao = exerciseDao
            )
        }
        item {
            Button(
                onClick = { programViewModel.days += (Day("Day ${programViewModel.days.size + 1}")) },
                enabled = programViewModel.days.size < 7,
            ) {
                Text("Add Day")
            }
        }
    }
}