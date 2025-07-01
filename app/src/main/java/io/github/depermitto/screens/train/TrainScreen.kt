package io.github.depermitto.screens.train

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import io.github.depermitto.components.DropdownButton
import io.github.depermitto.components.NumberField
import io.github.depermitto.components.RibbonScaffold
import io.github.depermitto.data.ExerciseDao
import io.github.depermitto.presentation.TrainViewModel
import io.github.depermitto.replaceAt
import io.github.depermitto.screens.exercises.ExerciseChooser
import io.github.depermitto.theme.ItemPadding
import io.github.depermitto.theme.ItemSpacing
import io.github.depermitto.theme.filledContainerColor
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TrainScreen(viewModel: TrainViewModel, exerciseDao: ExerciseDao) {
    val start = Instant.now()
    RibbonScaffold(ribbon = {
        OutlinedCard(modifier = Modifier.padding(start = ItemPadding, end = ItemPadding, bottom = ItemPadding)) {
            Row(modifier = Modifier.padding(horizontal = ItemPadding), verticalAlignment = Alignment.CenterVertically) {
                Text(text = viewModel.instantSince(start).format("m:ss"), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = { TODO() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(text = "Finish")
                }
            }
        }
    }) {
        LazyColumn(
            modifier = Modifier.padding(horizontal = ItemPadding),
            verticalArrangement = Arrangement.spacedBy(ItemSpacing)
        ) {
            itemsIndexed(viewModel.sets) { i, set ->
                Card(colors = CardDefaults.cardColors(containerColor = filledContainerColor())) {
                    Column(
                        modifier = Modifier.padding(ItemPadding),
                        verticalArrangement = Arrangement.spacedBy(2 * ItemSpacing)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = "${i + 1}. ${set.first().name}",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            set.lastOrNull { it.done != null }?.let { exercise ->
                                Card {
                                    Text(
                                        modifier = Modifier.padding(4.dp),
                                        text = if (set.all { it.done != null }) "Done"
                                        else viewModel.instantSince(exercise.done!!).format("m:ss"),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                            DropdownButton {
                                // TODO add swap, etc..
                            }
                        }

                        set.forEachIndexed { j, exercise ->
                            Column {
                                Text(text = "Set ${j + 1}\t", style = MaterialTheme.typography.titleSmall)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(ItemSpacing)
                                ) {
                                    NumberField(
                                        modifier = Modifier.weight(0.4f), value = exercise.reps, onValueChange = {
                                            viewModel.sets[i] = viewModel.sets[i].replaceAt(j, exercise.copy(reps = it))
                                        }, label = "Reps"
                                    )
                                    NumberField(
                                        modifier = Modifier.weight(0.4f), value = exercise.rpe, onValueChange = {
                                            viewModel.sets[i] = viewModel.sets[i].replaceAt(j, exercise.copy(rpe = it))
                                        }, label = "RPE"
                                    )
                                    Checkbox(modifier = Modifier.weight(0.1f),
                                        checked = viewModel.sets[i][j].done != null,
                                        onCheckedChange = {
                                            viewModel.sets[i] = viewModel.sets[i].replaceAt(
                                                j, exercise.copy(done = if (it) Instant.now() else null)
                                            )
                                        })
                                }
                            }
                        }

                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors()
                                .copy(contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
                            onClick = {
                                viewModel.sets[i] =
                                    viewModel.sets[i] + set.first().copy(rpe = 0f, reps = 0f, done = null)
                            },
                        ) {
                            Text(text = "Add Set")
                        }
                    }
                }
            }


            item {
                ExerciseChooser(exerciseDao = exerciseDao, onChoose = { viewModel.sets.add(listOf(it)) })
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun Instant.format(pattern: String): String =
    LocalDateTime.ofInstant(this, TimeZone.getDefault().toZoneId()).format(DateTimeFormatter.ofPattern(pattern))