package io.github.depermitto.bullettrain.programs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destinations
import io.github.depermitto.bullettrain.components.AnchoredFloatingActionButton
import io.github.depermitto.bullettrain.components.DiscardConfirmationAlertDialog
import io.github.depermitto.bullettrain.components.HoldToShowOptionsBox
import io.github.depermitto.bullettrain.components.TextFieldAlertDialog
import io.github.depermitto.bullettrain.database.ProgramDao
import io.github.depermitto.bullettrain.theme.CardSpacing
import io.github.depermitto.bullettrain.theme.ItemPadding
import io.github.depermitto.bullettrain.theme.filledContainerColor
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ProgramsTab(
    modifier: Modifier = Modifier,
    programViewModel: ProgramViewModel,
    programDao: ProgramDao,
    navController: NavController,
) = Box(modifier = modifier.fillMaxSize()) {
    val programs by programDao.getAlmostAll.collectAsStateWithLifecycle(initialValue = emptyList())

    LazyColumn(
        modifier = Modifier.padding(horizontal = ItemPadding), verticalArrangement = Arrangement.spacedBy(CardSpacing)
    ) {
        items(programs) { program ->
            var showRenameDialog by rememberSaveable { mutableStateOf(false) }
            var showProgramDeleteDialog by rememberSaveable { mutableStateOf(false) }
            HoldToShowOptionsBox(onClick = { navController.navigate(Destinations.Program(program)) }, holdOptions = {
                DropdownMenuItem(text = { Text(text = "Rename") },
                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                    onClick = { showRenameDialog = true })
                DropdownMenuItem(text = { Text(text = "Delete") },
                    leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                    onClick = { showProgramDeleteDialog = true })
            }) {

                Card(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    colors = CardDefaults.cardColors(containerColor = filledContainerColor())
                ) {
                    ListItem(colors = ListItemDefaults.colors(containerColor = Color.Transparent),
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
                                    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy")
                                    Text(
                                        text = "Most recent workout: ${formatter.format(date)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        })
                }

                if (showProgramDeleteDialog) DiscardConfirmationAlertDialog(text = "Do you definitely want to delete ${program.name}?",
                    onDismissRequest = { showProgramDeleteDialog = false },
                    onConfirm = { programDao.delete(program) })

                if (showRenameDialog) TextFieldAlertDialog(label = { Text("Program Name") },
                    onDismissRequest = { showRenameDialog = false },
                    dismissButton = { TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") } },
                    confirmButton = { name ->
                        TextButton(onClick = {
                            programDao.update(program.copy(name = name))
                            showRenameDialog = false
                        }) {
                            Text("Confirm")
                        }
                    })
            }
        }
    }

    AnchoredFloatingActionButton(
        icon = { Icon(Icons.Filled.Add, contentDescription = "Create Program") },
        text = { Text(text = "Create") },
        onClick = { programViewModel.clear(); navController.navigate(Destinations.ProgramCreation) },
    )
}