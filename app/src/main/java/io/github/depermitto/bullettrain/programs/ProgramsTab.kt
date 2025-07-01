package io.github.depermitto.bullettrain.programs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Screen
import io.github.depermitto.bullettrain.components.AnchoredFloatingActionButton
import io.github.depermitto.bullettrain.components.TextFieldDialogTemplate
import io.github.depermitto.bullettrain.database.Program
import io.github.depermitto.bullettrain.database.ProgramDao
import io.github.depermitto.bullettrain.theme.ItemPadding
import io.github.depermitto.bullettrain.theme.ItemSpacing
import io.github.depermitto.bullettrain.theme.filledContainerColor
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProgramsTab(
    modifier: Modifier = Modifier,
    programDao: ProgramDao,
    navController: NavController,
) = Box(modifier = modifier.fillMaxSize()) {
    val programs by programDao.getAlmostAll.collectAsStateWithLifecycle(initialValue = emptyList())

    LazyColumn(
        modifier = Modifier.padding(horizontal = ItemPadding), verticalArrangement = Arrangement.spacedBy(ItemSpacing)
    ) {
        items(programs) { program ->
            var showDropdown by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .clip(shape = MaterialTheme.shapes.medium)
                    .combinedClickable(onClick = { navController.navigate(program) },
                        onLongClick = { showDropdown = true })
            ) {
                Card(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    colors = CardDefaults.cardColors(containerColor = filledContainerColor())
                ) {
                    ProgramInfo(program = program)
                }

                var showDialog by remember { mutableStateOf(false) }
                DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false }) {
                    DropdownMenuItem(text = { Text(text = "Edit Name") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = { showDialog = true })
                    DropdownMenuItem(text = { Text(text = "Delete") },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                        onClick = { programDao.delete(program) })
                }

                AnimatedVisibility(visible = showDialog, enter = scaleIn(), exit = scaleOut()) {
                    BasicAlertDialog(onDismissRequest = { showDialog = false }) {
                        var name by remember { mutableStateOf("") }
                        TextFieldDialogTemplate(value = name,
                            onValueChange = { name = it },
                            label = { Text("Program Name") },
                            onDismiss = { showDialog = false },
                            onConfirm = {
                                programDao.update(program.copy(name = name))
                                showDialog = false
                                true
                            })
                    }
                }
            }
        }
    }

    AnchoredFloatingActionButton(
        icon = { Icon(Icons.Filled.Add, contentDescription = "Create Program") },
        text = { Text(text = "Create") },
        onClick = { navController.navigate(Screen.ProgramCreationScreen.route) },
    )
}

@Composable
fun ProgramInfo(modifier: Modifier = Modifier, program: Program) = ListItem(modifier = modifier,
    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
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
                    text = "Most recent workout: ${formatter.format(date)}", style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    })
