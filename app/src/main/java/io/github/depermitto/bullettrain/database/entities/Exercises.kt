package io.github.depermitto.bullettrain.database.entities

import androidx.compose.runtime.Immutable
import io.github.depermitto.bullettrain.components.encodeToStringOutput
import io.github.depermitto.bullettrain.database.serializers.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class ExerciseDescriptor(
  @SerialName("exerciseId") val id: Int = 0,
  val name: String,
  val obsolete: Boolean = false,
  val instructions: String = "",
)

@Immutable
@Serializable
data class WorkoutEntry(
  @SerialName("exerciseDescriptor") val descriptorId: Int,
  @SerialName("performanceVariableCategory")
  val perfVarCategory: PerfVarCategory = PerfVarCategory.Reps,
  val intensity: Intensity? = null,
  val sets: List<ExerciseSet> = listOf(),
  val superset: List<Int>? = null,
  val alternatives: List<Int>? = null,
  val notes: String = "",
) {
  val hasIntensity
    get() = intensity != null

  fun getPerformedSets(): List<ExerciseSet> = this.sets.filter { it.completed }

  fun lastPerformedSet() = this.sets.lastOrNull { it.completed }
}

@Immutable
@Serializable
data class ExerciseSet(
  @SerialName("targetPerformanceVariable") val targetPerfVar: PerfVar,
  @SerialName("actualPerformanceVariable") val actualPerfVar: Float = 0f,
  val targetIntensity: Intensity? = null,
  val actualIntensity: Float? = null,
  val weight: Float = 0f,
  @Serializable(with = InstantSerializer::class) val doneTs: Instant? = null,
) {
  val completed
    get() = doneTs != null && actualPerfVar != 0f
}

// TODO implement these as choices
@Immutable
@Serializable
enum class Intensity {
  RPE,
  RIR,
  PercentOf1RM,
}

@Immutable
@Serializable
enum class PerfVarCategory {
  Reps,
  RepRange,
  Time,
  TimeRange;

  val shortName
    get() =
      when (this) {
        Reps -> name
        RepRange -> Reps.name
        Time -> name
        TimeRange -> Time.name
      }
}

@Immutable
@Serializable
sealed class PerfVar(val category: PerfVarCategory) {
  @Immutable @Serializable data class Reps(val reps: Float = 0f) : PerfVar(PerfVarCategory.Reps)

  @Immutable @Serializable data class Time(val time: Float = 0f) : PerfVar(PerfVarCategory.Time)

  @Immutable
  @Serializable
  data class RepRange(val min: Float = 0f, val max: Float = 0f) : PerfVar(PerfVarCategory.RepRange)

  @Immutable
  @Serializable
  data class TimeRange(val min: Float = 0f, val max: Float = 0f) :
    PerfVar(PerfVarCategory.TimeRange)

  companion object {
    fun of(category: PerfVarCategory) =
      when (category) {
        PerfVarCategory.Reps -> Reps()
        PerfVarCategory.Time -> Time()
        PerfVarCategory.RepRange -> RepRange()
        PerfVarCategory.TimeRange -> TimeRange()
      }
  }

  fun encodeToStringOutput(): String =
    when (this) {
      is Reps ->
        if (this == Reps()) ""
        else reps.encodeToStringOutput() + if (reps == 1f) " rep" else " reps"
      is Time -> if (this == Time()) "" else time.encodeToStringOutput() + " min"
      is RepRange ->
        if (this == RepRange()) ""
        else "${min.encodeToStringOutput()}-${max.encodeToStringOutput()} reps"
      is TimeRange ->
        if (this == TimeRange()) ""
        else "${min.encodeToStringOutput()}-${max.encodeToStringOutput()} min"
    }
}
