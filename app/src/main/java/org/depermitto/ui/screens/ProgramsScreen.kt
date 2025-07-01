package org.depermitto.ui.screens

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.depermitto.data.ProgramDao
import org.depermitto.ui.theme.filledContainerColor
import org.depermitto.ui.theme.paddingDp
import org.depermitto.ui.theme.spacingDp

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
                // TODO Navigate to ProgramOverviewScreen, seeing history of the plan and trainingWork
                OutlinedCard(colors = CardDefaults.cardColors(containerColor = filledContainerColor())) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(paddingDp * 2)
                        ) {
                            Text(
                                text = program.name,
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                text = "${program.trainingWork.size} day program",
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