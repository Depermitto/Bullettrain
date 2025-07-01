package io.github.depermitto.bullettrain.database.entities

import androidx.compose.runtime.Immutable
import io.github.depermitto.bullettrain.database.serializers.LocalDateSerializer
import java.time.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Program(
  @SerialName("programId") val id: Int = 0,
  val obsolete: Boolean = false,
  val name: String = "",
  val workouts: List<Workout> = listOf(Workout()),
  val nextDayIndex: Int = 0,
  val followed: Boolean = false,
  val draft: Boolean = false,
  @Serializable(with = LocalDateSerializer::class) val mostRecentWorkoutDate: LocalDate? = null,
) {
  /**
   * Essentially a comparison between this.[id] and [other].[id]
   *
   * @see [correspondsNot]
   */
  infix fun corresponds(other: Program) = this.id == other.id

  /**
   * Essentially a comparison if this.[id] is not equal to [other].[id].
   *
   * @see [corresponds]
   */
  infix fun correspondsNot(other: Program) = this.id != other.id

  fun nextDay() = workouts[nextDayIndex]

  companion object {
    val EmptyWorkout = Program(id = -1, name = "Impromptu Workout")
  }
}

@Immutable
@Serializable
data class Workout(val name: String = "Day 1", val entries: List<WorkoutEntry> = listOf())
