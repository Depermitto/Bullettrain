package io.github.depermitto.bullettrain.database.daos

import io.github.depermitto.bullettrain.database.entities.ExerciseDescriptor
import io.github.depermitto.bullettrain.database.entities.HistoryRecord
import io.github.depermitto.bullettrain.database.entities.WorkoutEntry
import io.github.depermitto.bullettrain.train.WorkoutPhase
import io.github.depermitto.bullettrain.util.bigListSet
import java.time.Month
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class HistoryDao(state: List<HistoryRecord>) {
  internal val items = MutableStateFlow(state)
  private var newId = items.value.maxOfOrNull { it.id } ?: 0

  val getAll: StateFlow<List<HistoryRecord>> = items.asStateFlow()

  /** @return Boolean indicating if the operation was successful. */
  fun update(item: HistoryRecord): Boolean {
    val existingIndex = items.value.indexOfFirst { it.id == item.id }
    if (existingIndex == -1) return false

    items.update { state -> state.bigListSet(existingIndex, item) }
    return true
  }

  /** @return Id of the inserted item. */
  fun insert(item: HistoryRecord): Int {
    items.update { state ->
      newId += 1
      state + item.copy(id = newId)
    }
    return newId
  }

  /** @return Id of the inserted item or -1 if it was updated. */
  fun upsert(item: HistoryRecord): Int = if (update(item)) -1 else insert(item)

  fun delete(item: HistoryRecord) {
    items.update { state -> state - item }
  }

  fun where(id: Int): HistoryRecord = items.value.first { it.id == id }

  fun getUnfinishedBusiness(): HistoryRecord? =
    items.value.firstOrNull { record -> record.workoutPhase != WorkoutPhase.Completed }

  fun where(month: Month, year: Int): Flow<List<HistoryRecord>> =
    getAll.map { records ->
      records.filter { record -> record.date.month == month && record.date.year == year }
    }

  fun where(descriptor: ExerciseDescriptor): Flow<List<WorkoutEntry>> =
    getAll.map { records ->
      records
        .flatMap { record ->
          record.workout.entries.filter { entry -> entry.descriptorId == descriptor.id }
        }
        .sortedByDescending { exercise -> exercise.lastPerformedSet()?.doneTs }
    }
}
