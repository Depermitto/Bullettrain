package io.github.depermitto.bullettrain.database.entities

import androidx.compose.runtime.Immutable
import io.github.depermitto.bullettrain.database.serializers.InstantSerializer
import io.github.depermitto.bullettrain.database.serializers.LocalDateSerializer
import io.github.depermitto.bullettrain.train.WorkoutPhase
import java.time.Instant
import java.time.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class HistoryRecord(
  @SerialName("historyRecordId") val id: Int = 0,
  val relatedProgramId: Int,
  val workout: Workout,
  val workoutPhase: WorkoutPhase,
  @Serializable(with = LocalDateSerializer::class) val date: LocalDate,
  @Serializable(with = InstantSerializer::class) val workoutStartTs: Instant,
)
