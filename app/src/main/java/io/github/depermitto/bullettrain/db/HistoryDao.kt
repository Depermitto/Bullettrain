package io.github.depermitto.bullettrain.db

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.depermitto.bullettrain.protos.HistoryProto.HistoryRecord
import io.github.depermitto.bullettrain.protos.ProgramsProto.Workout
import io.github.depermitto.bullettrain.util.bigListSet
import io.github.depermitto.bullettrain.util.date
import io.github.depermitto.bullettrain.util.yearMonth
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class HistoryDao(historyRecords: List<HistoryRecord>) {
  internal val items = MutableStateFlow(historyRecords)
  private var idTrack = items.value.lastOrNull()?.id ?: 0

  val getSortedByFrequency: Flow<Map<Int, Int>> =
    items.map { records ->
      records
        .flatMap { record ->
          record.workout.exercisesList.filter { entry ->
            entry.setsList.any { set -> set.hasDoneTs() }
          }
        }
        .groupingBy { entry -> entry.descriptorId }
        .eachCount()
    }

  fun mostRecent() = items.value.lastOrNull()

  fun mostRecent(yearMonth: YearMonth) = items.value.lastOrNull { it.yearMonth == yearMonth }?.date

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
      idTrack++
      state + record.toBuilder().setId(idTrack).build()
    }
    return idTrack
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

  @SuppressLint("StateFlowValueCalledInComposition")
  @Composable
  fun whereAsState(id: Int): State<HistoryRecord> =
    items
      .map { records -> records[records.binarySearch { it.id - id }] }
      .collectAsStateWithLifecycle(
        initialValue = items.value[items.value.binarySearch { it.id - id }]
      )

  fun where(id: Int): HistoryRecord = items.value[items.value.binarySearch { it.id - id }]

  fun <T> map(mapper: (records: List<HistoryRecord>) -> T): Flow<T> =
    items.map { historyRecords -> mapper(historyRecords) }
}
