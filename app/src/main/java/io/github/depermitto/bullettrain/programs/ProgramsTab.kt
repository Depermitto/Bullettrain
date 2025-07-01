package io.github.depermitto.bullettrain.programs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destination
import io.github.depermitto.bullettrain.components.AnchoredFloatingActionButton
import io.github.depermitto.bullettrain.components.DiscardConfirmationAlertDialog
import io.github.depermitto.bullettrain.components.HoldToShowOptionsBox
import io.github.depermitto.bullettrain.components.TextFieldAlertDialog
import io.github.depermitto.bullettrain.components.TransparentCard
import io.github.depermitto.bullettrain.database.ProgramDao
import io.github.depermitto.bullettrain.theme.RegularPadding
import io.github.depermitto.bullettrain.theme.RegularSpacing
import io.github.depermitto.bullettrain.theme.ScrollPadding
import io.github.depermitto.bullettrain.theme.SmallPadding

@Composable
fun ProgramsTab(
    modifier: Modifier = Modifier,
    programViewModel: ProgramViewModel,
    programDao: ProgramDao,
    navController: NavController,
) = Box(modifier = modifier.fillMaxSize()) {
    val programs by programDao.getAlmostAll.collectAsStateWithLifecycle(initialValue = emptyList())

    LazyColumn(
        contentPadding = PaddingValues(start = RegularPadding, end = RegularPadding, bottom = ScrollPadding),
        verticalArrangement = Arrangement.spacedBy(RegularSpacing)
    ) {
        items(programs) { program ->
            var showRenameDialog by rememberSaveable { mutableStateOf(false) }
            var showProgramDeleteDialog by rememberSaveable { mutableStateOf(false) }
            HoldToShowOptionsBox(onClick = { navController.navigate(Destination.Program(program.id)) },
                holdOptions = { closeDropdown ->
                    DropdownMenuItem(text = { Text(text = "Rename") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = { closeDropdown(); showRenameDialog = true })
                    DropdownMenuItem(text = { Text(text = "Delete") },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                        onClick = { closeDropdown(); showProgramDeleteDialog = true })
                }) {
                TransparentCard(modifier = Modifier.align(Alignment.Center)) {
                    ListItem(colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text(text = program.name, style = MaterialTheme.typography.titleLarge) },
                        supportingContent = {
                            Column {
                                Text(text = "${program.days.size} day program")
                                Text(text = "${program.days.sumOf { day -> day.exercises.sumOf { it.sets.size } }} total sets")
//                                program.mostRecentWorkoutDate?.let { date ->
//                                    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy")
//                                    Text(
//                                        text = "Most recent workout: ${formatter.format(date)}",
//                                        style = MaterialTheme.typography.bodyMedium
//                                    )
//                                }
                            }
                        },
                        trailingContent = {
                            if (program.draft) Card {
                                Text(
                                    text = "Draft",
                                    modifier = Modifier.padding(SmallPadding),
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        })
                }

                if (showProgramDeleteDialog) DiscardConfirmationAlertDialog(text = "Do you definitely want to discard ${program.name}?",
                    onDismissRequest = { showProgramDeleteDialog = false },
                    onConfirm = { programDao.delete(program) })

                if (showRenameDialog) {
                    var errorMessage by rememberSaveable { mutableStateOf("") }
                    TextFieldAlertDialog(label = { Text("Program Name") },
                        onDismissRequest = { showRenameDialog = false },
                        dismissButton = { TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") } },
                        confirmButton = { name ->
                            TextButton(onClick = {
                                if (name.isBlank()) {
                                    errorMessage = "Black Program Name"
                                    return@TextButton
                                }

                                programDao.update(program.copy(name = name))
                                showRenameDialog = false
                            }) {
                                Text("Confirm")
                            }
                        },
                        errorMessage = errorMessage,
                        isError = errorMessage.isNotEmpty()
                    )
                }
            }
        }
    }


    AnchoredFloatingActionButton(
        icon = { Icon(Icons.Filled.Add, contentDescription = "Create Program") },
        text = { Text(text = "Create") },
        onClick = { programViewModel.revertToDefault(); navController.navigate(Destination.ProgramCreation) },
    )
}