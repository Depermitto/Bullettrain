package org.depermitto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.depermitto.database.ProgramDao
import org.depermitto.ui.theme.filledContainerColor

@Composable
fun ProgramsScreen(programDao: ProgramDao, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val programs by programDao.getAllFlow().collectAsState(emptyList())

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(programs) { program ->
                // TODO Navigate to ProgramOverviewScreen, seeing history of the plan and trainingWork
                OutlinedCard(colors = CardDefaults.cardColors(containerColor = filledContainerColor())) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(16.dp)
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
                        IconButton(onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                programDao.delete(program)
                            }
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                        }
                    }
                }
            }
        }

        Button(onClick = { navController.navigate(Screen.ProgramsCreationScreen.route) }) {
            Text(text = "Create Program")
        }
    }
}