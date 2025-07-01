package io.github.depermitto.bullettrain.db

import io.github.depermitto.bullettrain.protos.ExercisesProto.*
import io.github.depermitto.bullettrain.protos.HistoryProto.HistoryRecord
import io.github.depermitto.bullettrain.protos.ProgramsProto.Workout
import io.github.depermitto.bullettrain.util.bigListSet
import io.github.depermitto.bullettrain.util.getDate
import io.github.depermitto.bullettrain.util.getLastCompletedSet
import java.time.Month
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class HistoryDao(historyRecords: List<HistoryRecord>) {
  internal val items = MutableStateFlow(historyRecords)
  private var newId = items.value.lastOrNull()?.id ?: 0

  val getAll: StateFlow<List<HistoryRecord>> = items.asStateFlow()
  val getSortedByFrequency: Flow<Map<Int, Int>> =
    getAll.map { records ->
      records
        .flatMap { record ->
          record.workout.exercisesList.filter { entry ->
            entry.setsList.any { set -> set.hasDoneTs() }
          }
        }
        .groupingBy { entry -> entry.descriptorId }
        .eachCount()
    }

  /** @return Boolean indicating if the operation was successful. */
  fun update(record: HistoryRecord): Boolean {
    // Binary search because the list is big
    val index = items.value.binarySearch { it.id - record.id }
    if (index < 0) return false

    items.update { state -> state.bigListSet(index, record) }
    return true
  }

  /** @return Id of the inserted record. */
  fun insert(record: HistoryRecord): Int {
    items.update { state ->
      newId++
      state + record.toBuilder().setId(newId).build()
    }
    return newId
  }

  /** @return Id of the inserted record or -1 if it was updated. */
  fun upsert(record: HistoryRecord): Int = if (update(record)) -1 else insert(record)

  fun delete(id: Int) {
    items.update { state -> state.filterNot { it.id == id } }
  }

  fun getUnfinishedBusiness(): HistoryRecord? =
    items.value.asReversed().firstOrNull { record ->
      record.workoutPhase != Workout.Phase.Completed
    }

  fun where(id: Int): HistoryRecord {
    val index = items.value.binarySearch { it.id - id }
    return items.value[index]
  }

  fun where(month: Month, year: Int): Flow<List<HistoryRecord>> =
    getAll.map { records ->
      records.filter { record ->
        val date = record.getDate()
        date.month == month && date.year == year
      }
    }

  fun where(descriptor: Exercise.Descriptor): Flow<List<Exercise>> =
    getAll.map { records ->
      records
        .flatMap { record ->
          record.workout.exercisesList.filter { exercise -> exercise.descriptorId == descriptor.id }
        }
        .sortedByDescending { exercise -> exercise.getLastCompletedSet()?.doneTs?.seconds ?: 0 }
    }
}
