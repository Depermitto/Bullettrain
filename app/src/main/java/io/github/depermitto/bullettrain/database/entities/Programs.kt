package io.github.depermitto.bullettrain.database.entities

import androidx.compose.runtime.Immutable
import io.github.depermitto.bullettrain.database.Dao
import io.github.depermitto.bullettrain.database.serializers.LocalDateSerializer
import java.nio.file.Path
import java.time.LocalDate
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Program(
  @SerialName("programId") override val id: Int = 0,
  val obsolete: Boolean = false,
  val name: String = "",
  val workouts: List<Workout> = listOf(Workout()),
  val nextDayIndex: Int = 0,
  val followed: Boolean = false,
  val draft: Boolean = false,
  @Serializable(with = LocalDateSerializer::class) val mostRecentWorkoutDate: LocalDate? = null,
) : Entity {
  override fun clone(id: Int) = copy(id = id)

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

class ProgramDao(filepath: Path) : Dao<Program>(filepath) {
  val getUserPrograms =
    getAll.map { programs ->
      programs
        .filter { it correspondsNot Program.EmptyWorkout && !it.obsolete }
        .sortedByDescending { it.mostRecentWorkoutDate }
    }
  val getPerformable =
    getAll.map { programs ->
      programs.filterNot { it.obsolete }.sortedByDescending { it.mostRecentWorkoutDate }
    }

  override fun delete(item: Program) {
    super.update(item.copy(obsolete = true))
  }
}
