package io.github.depermitto.bullettrain.database.daos

import android.util.Log
import io.github.depermitto.bullettrain.database.entities.ExerciseDescriptor
import io.github.depermitto.bullettrain.database.entities.HistoryRecord
import io.github.depermitto.bullettrain.database.entities.WorkoutEntry
import io.github.depermitto.bullettrain.train.WorkoutPhase
import io.github.depermitto.bullettrain.util.bigListSet
import io.github.depermitto.bullettrain.util.loadAndUncompressData
import io.github.depermitto.bullettrain.util.saveAndCompressData
import java.nio.file.Path
import java.time.Month
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.updateAndGet

class HistoryDao(private val filepath: Path) {
  internal val items = MutableStateFlow<List<HistoryRecord>>(loadAndUncompressData(filepath))
  private var newId = items.value.maxOfOrNull { it.id } ?: 0

  val getAll: StateFlow<List<HistoryRecord>> = items.asStateFlow()

  /** @return Boolean indicating if the operation was successful. */
  fun update(item: HistoryRecord): Boolean {
    val existingIndex = items.value.indexOfFirst { it.id == item.id }
    if (existingIndex == -1) return false

    val state = items.updateAndGet { state -> state.bigListSet(existingIndex, item) }
    saveAndCompressData(filepath, state)
    Log.i("db-${filepath}", state.toString())
    return true
  }

  /** @return Id of the inserted item. */
  fun insert(item: HistoryRecord): Int {
    val state =
      items.updateAndGet { state ->
        newId += 1
        state + item.copy(id = newId)
      }
    saveAndCompressData(filepath, state)
    Log.i("db-${filepath}", state.toString())
    return newId
  }

  /** @return Id of the inserted item or -1 if it was updated. */
  fun upsert(item: HistoryRecord): Int = if (update(item)) -1 else insert(item)

  fun delete(item: HistoryRecord) {
    val state = items.updateAndGet { state -> state - item }
    saveAndCompressData(filepath, state)
    Log.i("db-${filepath}", state.toString())
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
