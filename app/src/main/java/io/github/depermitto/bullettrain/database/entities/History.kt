package io.github.depermitto.bullettrain.database.entities

import androidx.compose.runtime.Immutable
import io.github.depermitto.bullettrain.database.Compressor
import io.github.depermitto.bullettrain.database.Dao
import io.github.depermitto.bullettrain.database.Depot
import io.github.depermitto.bullettrain.database.serializers.InstantSerializer
import io.github.depermitto.bullettrain.database.serializers.LocalDateSerializer
import io.github.depermitto.bullettrain.train.WorkoutPhase
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Immutable
@Serializable
data class HistoryRecord(
  @SerialName("historyRecordId") override val id: Int = 0,
  val relatedProgramId: Int,
  val workout: Workout,
  val workoutPhase: WorkoutPhase,
  @Serializable(with = LocalDateSerializer::class) val date: LocalDate,
  @Serializable(with = InstantSerializer::class) val workoutStartTs: Instant,
) : Entity {
  override fun clone(id: Int) = copy(id = id)
}

class HistoryDao(file: HistoryDepot) : Dao<HistoryRecord>(file) {
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

class HistoryDepot(file: File) : Depot<List<HistoryRecord>>(file) {
  override fun retrieve(): List<HistoryRecord> =
    Json.decodeFromString(Compressor.uncompress(file.readText()))

  override fun stash(obj: List<HistoryRecord>) =
    file.writeText(Compressor.compress(Json.encodeToString(obj)))
}
