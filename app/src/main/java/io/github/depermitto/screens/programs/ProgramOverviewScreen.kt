package io.github.depermitto.screens.programs

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import io.github.depermitto.data.ExerciseDao
import io.github.depermitto.data.Program
import io.github.depermitto.theme.paddingDp

@Composable
fun ProgramOverviewScreen(program: Program, exerciseDao: ExerciseDao) {
    LazyColumn(contentPadding = PaddingValues(paddingDp)) {
        items(program.days) { day ->
            DayScreen(day = day, onDayChange = { }, exerciseDao = exerciseDao)
        }
    }
}