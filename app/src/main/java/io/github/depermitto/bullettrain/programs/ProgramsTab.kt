package io.github.depermitto.bullettrain.programs

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.protobuf.InvalidProtocolBufferException
import io.github.depermitto.bullettrain.Destination
import io.github.depermitto.bullettrain.components.AnchoredFloatingActionButton
import io.github.depermitto.bullettrain.components.ConfirmationAlertDialog
import io.github.depermitto.bullettrain.components.DropdownButton
import io.github.depermitto.bullettrain.components.EmptyScreen
import io.github.depermitto.bullettrain.components.ExtendedListItem
import io.github.depermitto.bullettrain.components.TextFieldAlertDialog
import io.github.depermitto.bullettrain.db.ExerciseDao
import io.github.depermitto.bullettrain.db.ProgramDao
import io.github.depermitto.bullettrain.protos.DbProto.*
import io.github.depermitto.bullettrain.theme.EmptyScrollSpace
import io.github.depermitto.bullettrain.theme.FileExportIcon
import io.github.depermitto.bullettrain.theme.FileImportIcon
import io.github.depermitto.bullettrain.theme.Medium
import io.github.depermitto.bullettrain.theme.Small
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun ProgramsTab(
  modifier: Modifier = Modifier,
  programViewModel: ProgramViewModel,
  exerciseDao: ExerciseDao,
  programDao: ProgramDao,
  navController: NavController,
  snackbarHostState: SnackbarHostState,
) {
  Box(modifier = Modifier.fillMaxHeight() then modifier) {
    val scope = rememberCoroutineScope()
    val programs by programDao.getUserPrograms.collectAsStateWithLifecycle(emptyList())

    if (programs.isEmpty())
      EmptyScreen(
        "No programs found. After creating a program it will appear here.",
        modifier = modifier,
      )
    else
      Column(
        modifier =
          Modifier.padding(start = Dp.Medium, end = Dp.Medium)
            .verticalScroll(rememberScrollState())
            .padding(bottom = Dp.EmptyScrollSpace),
        verticalArrangement = Arrangement.spacedBy(Dp.Medium),
      ) {
        programs.forEach { program ->
          var showRenameDialog by rememberSaveable { mutableStateOf(false) }
          if (showRenameDialog) {
            var errorMessage by rememberSaveable { mutableStateOf("") }
            TextFieldAlertDialog(
              startingText = program.name,
              label = { Text("Program Name") },
              onDismissRequest = { showRenameDialog = false },
              dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
              },
              confirmButton = { name ->
                TextButton(
                  onClick = {
                    if (name.isBlank()) {
                      errorMessage = "Blank program name"
                      return@TextButton
                    }

                    programDao.update(program.toBuilder().setName(name).build())
                    showRenameDialog = false
                  }
                ) {
                  Text("Confirm")
                }
              },
              errorMessage = errorMessage,
              isError = errorMessage.isNotEmpty(),
            )
          }

          var showProgramDeleteDialog by rememberSaveable { mutableStateOf(false) }
          if (showProgramDeleteDialog)
            ConfirmationAlertDialog(
              text = "Do you definitely want to delete ${program.name}?",
              onDismissRequest = { showProgramDeleteDialog = false },
              onConfirm = { programDao.delete(program) },
            )

          Card(
            onClick = { navController.navigate(Destination.Program(program.id)) },
            colors =
              CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
              ),
          ) {
            Column(
              modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
              verticalArrangement = Arrangement.spacedBy(Dp.Small),
            ) {
              val clipboardManager = LocalClipboardManager.current
              ExtendedListItem(
                contentPadding = PaddingValues(0.dp),
                headlineContent = {
                  Text(program.name, style = MaterialTheme.typography.titleLarge)
                },
                trailingContent = {
                  var showDropdown by remember { mutableStateOf(false) }
                  DropdownButton(showDropdown, onShowChange = { showDropdown = it }) {
                    DropdownMenuItem(
                      text = { Text("Export") },
                      leadingIcon = FileExportIcon,
                      onClick = {
                        showDropdown = false

                        val db =
                          Db.newBuilder()
                            .addPrograms(
                              program
                                .toBuilder()
                                .clearNextDayIndex()
                                .clearLastWorkoutTs()
                                .clearObsolete()
                                .clearId()
                            )
                            .apply {
                              for (day in program.workoutsList) {
                                for (exercise in day.exercisesList) {
                                  addDescriptors(exerciseDao.where(exercise.descriptorId))
                                }
                              }
                            }
                            .build()

                        val code = Base64.encode(db.toByteArray())
                        clipboardManager.setText(AnnotatedString(code))

                        scope.launch {
                          snackbarHostState.showSnackbar(
                            message = "Copied ${program.name} code to clipboard",
                            withDismissAction = true,
                          )
                        }
                      },
                    )
                    DropdownMenuItem(
                      text = { Text("Rename") },
                      leadingIcon = { Icon(Icons.Filled.Edit, "Rename program") },
                      onClick = {
                        showDropdown = false
                        showRenameDialog = true
                      },
                    )
                    DropdownMenuItem(
                      text = { Text("Delete") },
                      leadingIcon = { Icon(Icons.Filled.Delete, "Delete program") },
                      onClick = {
                        showDropdown = false
                        showProgramDeleteDialog = true
                      },
                    )
                  }
                },
                supportingContent = {
                  Column {
                    Text("${program.workoutsCount} day program")
                    Text(
                      "${program.workoutsList.sumOf { day -> day.exercisesList.sumOf { e -> e.setsCount } }} total sets"
                    )
                  }
                },
              )

              Spacer(Modifier.height(20.dp))

              Text("Next up...")
              Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Dp.Small),
              ) {
                for (i in 0..<program.workoutsCount) {
                  val day = program.workoutsList[(program.nextDayIndex + i) % program.workoutsCount]
                  val dayIndex = program.workoutsList.indexOfFirst { d -> d.name == day.name }

                  SuggestionChip(
                    onClick = {
                      navController.navigate(Destination.DirectDay(program.id, dayIndex))
                    },
                    label = { Text(day.name, maxLines = 1) },
                  )
                }
              }
            }
          }
        }
      }

    AnchoredFloatingActionButton(
      icon = { Icon(Icons.Filled.Add, "Create program") },
      text = { Text("Create") },
      onClick = {
        programViewModel.revertToDefault()
        navController.navigate(Destination.ProgramCreation)
      },
    )

    var showProgramImportDialog by rememberSaveable { mutableStateOf(false) }
    AnchoredFloatingActionButton(
      modifier = Modifier.padding(bottom = 72.dp),
      onClick = { showProgramImportDialog = true },
      icon = FileImportIcon,
      containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    )

    if (showProgramImportDialog) {
      var errorMessage by rememberSaveable { mutableStateOf("") }
      TextFieldAlertDialog(
        onDismissRequest = { showProgramImportDialog = false },
        label = { Text("Program code") },
        errorMessage = errorMessage,
        isError = errorMessage.isNotBlank(),
        textStyle =
          LocalTextStyle.current.copy(
            fontFamily = FontFamily(Typeface(android.graphics.Typeface.MONOSPACE)),
            fontSize = 16.sp,
          ),
        dismissButton = {
          TextButton(onClick = { showProgramImportDialog = false }) { Text("Cancel") }
        },
        confirmButton = { code ->
          TextButton(
            onClick = {
              val db =
                try {
                  if (code.isBlank()) {
                    errorMessage = "No input"
                    return@TextButton
                  }

                  val db = Db.parseFrom(Base64.decode(code.trim()))
                  if (db.programsCount == 0) {
                    errorMessage = "No program encoded"
                    return@TextButton
                  }

                  db
                } catch (e: InvalidProtocolBufferException) {
                  errorMessage = "Provided code is not a program"
                  return@TextButton
                } catch (e: Throwable) {
                  errorMessage = "Invalid code"
                  return@TextButton
                }

              var newDescriptorsCount = 0
              val program =
                db
                  .getPrograms(0)
                  .toBuilder()
                  .apply {
                    workoutsList.forEachIndexed { dayIndex, day ->
                      setWorkouts(
                        dayIndex,
                        day.toBuilder().apply {
                          exercisesList.forEachIndexed { exerciseIndex, exercise ->
                            val descriptor =
                              db.descriptorsList.first { d -> d.id == exercise.descriptorId }

                            val id =
                              exerciseDao.whereOrNull(descriptor.name)?.id
                                ?: exerciseDao.insert(descriptor).also { newDescriptorsCount++ }

                            setExercises(exerciseIndex, exercise.toBuilder().setDescriptorId(id))
                          }
                        },
                      )
                    }
                  }
                  .build()

              programDao.insert(program)

              scope.launch {
                var message = "Imported ${program.name}"
                if (newDescriptorsCount != 0) message += " and $newDescriptorsCount exercises"
                snackbarHostState.showSnackbar(message = message, withDismissAction = true)
              }

              showProgramImportDialog = false
              errorMessage = ""
            }
          ) {
            Text("Import")
          }
        },
      )
    }
  }
}
