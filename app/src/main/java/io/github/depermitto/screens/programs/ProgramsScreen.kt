package io.github.depermitto.screens.programs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.times
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.depermitto.data.ProgramDao
import io.github.depermitto.screens.Screen
import io.github.depermitto.theme.filledContainerColor
import io.github.depermitto.theme.paddingDp
import io.github.depermitto.theme.spacingDp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ProgramsScreen(programDao: ProgramDao, navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = paddingDp)
    ) {
        val scope = rememberCoroutineScope { Dispatchers.IO }
        val programs by programDao.getAllFlow().collectAsStateWithLifecycle(emptyList())

        LazyColumn(verticalArrangement = Arrangement.spacedBy(spacingDp)) {
            items(programs) { program ->
                // TODO Navigate to ProgramOverviewScreen, seeing history of the plan and trainingWork, uniform look of exercises
                OutlinedCard(colors = CardDefaults.cardColors(containerColor = filledContainerColor()), onClick = {
                    navController.navigate(Screen.ProgramOverviewScreen.passId(program.programId))
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(paddingDp * 2)
                        ) {
                            Text(text = program.name, style = MaterialTheme.typography.titleLarge)
                            Text(text = "${program.days.size} day program", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "${program.days.sumOf { day -> day.exercises.sumOf { set -> set.size } }} total sets",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        IconButton(onClick = { scope.launch { programDao.delete(program) } }) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { navController.navigate(Screen.ProgramsCreationScreen.route) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 2 * paddingDp, end = paddingDp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
        }
    }
}