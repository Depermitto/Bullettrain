package io.github.depermitto.screens.programs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.times
import io.github.depermitto.data.Day
import io.github.depermitto.data.ExerciseDao
import io.github.depermitto.data.Program
import io.github.depermitto.presentation.ProgramViewModel
import io.github.depermitto.theme.paddingDp
import io.github.depermitto.theme.spacingDp

/**
 * [fabText] - null to not show the fab at all
 */
@Composable
fun ProgramScreen(
    viewModel: ProgramViewModel,
    fabText: (Program) -> String?,
    onFabClick: (Program) -> Unit,
    exerciseDao: ExerciseDao,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(paddingDp),
            verticalArrangement = Arrangement.spacedBy(spacingDp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(viewModel.state.days) { i, day ->
                DayScreen(
                    day = day, onDayChange = {
                        if (it != null) viewModel.setDayAt(i, it)
                        else viewModel.removeDay(day)
                    }, exerciseDao = exerciseDao
                )
            }
            item {
                Button(
                    onClick = { viewModel.addDay(Day("Day ${viewModel.state.days.size + 1}")) },
                    enabled = viewModel.state.days.size < 7,
                ) {
                    Text("Add Day")
                }
            }
        }
        fabText(viewModel.state)?.let { title ->
            ExtendedFloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 2 * paddingDp, end = 2 * paddingDp),
                icon = { Icon(Icons.Filled.Check, contentDescription = null) },
                text = { Text(text = title) },
                onClick = { onFabClick(viewModel.state) },
            )
        }
    }
}