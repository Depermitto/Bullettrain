package io.github.depermitto.programs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.navigation.NavController
import io.github.depermitto.Screen
import io.github.depermitto.components.AnchoredFloatingActionButton
import io.github.depermitto.data.entities.Program
import io.github.depermitto.data.entities.ProgramDao
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.ItemSpacing
import io.github.depermitto.theme.filledContainerColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProgramsTab(
    modifier: Modifier = Modifier,
    programDao: ProgramDao,
    navController: NavController,
) = Box(modifier = modifier.fillMaxSize()) {
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val programs = runBlocking { programDao.getAll() }

    LazyColumn(
        modifier = Modifier.padding(horizontal = ItemPadding), verticalArrangement = Arrangement.spacedBy(ItemSpacing)
    ) {
        items(programs) { program ->
            var showDropdown by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .clip(shape = MaterialTheme.shapes.medium)
                    .combinedClickable(onClick = { navController.navigate(Screen.ProgramScreen.passId(program.programId)) },
                        onLongClick = { showDropdown = true })
            ) {
                ProgramInfo(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center), program = program
                )

                DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false }) {
                    DropdownMenuItem(text = { Text(text = "Edit Program") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = { navController.navigate(Screen.ProgramScreen.passId(program.programId)) })
                    DropdownMenuItem(text = { Text(text = "Delete Program") },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                        onClick = { scope.launch(Dispatchers.IO) { programDao.delete(program) } })
                }
            }
        }
    }

    AnchoredFloatingActionButton(
        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
        onClick = { navController.navigate(Screen.ProgramCreationScreen.route) },
    )
}

@Composable
fun ProgramInfo(modifier: Modifier = Modifier, program: Program) = ListItem(
    modifier = modifier.clip(MaterialTheme.shapes.medium),
    colors = ListItemDefaults.colors(containerColor = filledContainerColor()),
    headlineContent = { Text(text = program.name, style = MaterialTheme.typography.titleLarge) },
    supportingContent = {
        Column {
            Text(text = "${program.days.size} day program", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${program.days.sumOf { day -> day.exercises.sumOf { it.sets.size } }} total sets",
                style = MaterialTheme.typography.bodyMedium
            )
            program.mostRecentWorkoutDate?.let { instant ->
                val date = instant.atZone(ZoneId.systemDefault())
                val formatter = DateTimeFormatter.ofPattern("dd MM yyyy")
                Text(
                    text = "Most recent workout: ${formatter.format(date)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
)
