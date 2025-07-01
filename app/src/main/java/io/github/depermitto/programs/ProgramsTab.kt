package io.github.depermitto.programs

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.depermitto.components.AnchoredFloatingActionButton
import io.github.depermitto.data.Program
import io.github.depermitto.data.ProgramDao
import io.github.depermitto.screen.Screen
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.ItemSpacing
import io.github.depermitto.theme.filledContainerColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ProgramsTab(modifier: Modifier = Modifier, programDao: ProgramDao, navController: NavController) = Box(
    modifier = modifier
        .fillMaxSize()
        .padding(horizontal = ItemPadding)
) {
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val programs by programDao.getAllFlow().collectAsStateWithLifecycle(emptyList())

    LazyColumn(verticalArrangement = Arrangement.spacedBy(ItemSpacing)) {
        items(programs) { program ->
            OutlinedCard(colors = CardDefaults.cardColors(containerColor = filledContainerColor()), onClick = {
                navController.navigate(Screen.ProgramScreen.passId(program.programId))
            }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgramInfo(modifier = Modifier.weight(1f), program = program)
                    IconButton(onClick = { scope.launch { programDao.delete(program) } }) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                    }
                }
            }
        }
    }

    AnchoredFloatingActionButton(
        modifier = modifier.offset(x = ItemPadding),
        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
        onClick = { navController.navigate(Screen.ProgramCreationScreen.route) },
    )
}

@Composable
fun ProgramInfo(modifier: Modifier = Modifier, program: Program) = Column(modifier.padding(ItemPadding * 2)) {
    Text(text = program.name, style = MaterialTheme.typography.titleLarge)
    Text(text = "${program.days.size} day program", style = MaterialTheme.typography.bodyMedium)
    Row {
        Text(
            modifier = Modifier.weight(1f),
            text = "${program.days.sumOf { day -> day.exercises.sumOf { it.sets.size } }} total sets",
            style = MaterialTheme.typography.bodyMedium
        )
        Card {
            Text(modifier = Modifier.padding(2.dp), text = if (program.active) "Active" else "Inactive")
        }
    }
}