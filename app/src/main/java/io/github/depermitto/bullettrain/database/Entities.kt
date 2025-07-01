package io.github.depermitto.bullettrain.database

import io.github.depermitto.bullettrain.components.encodeToStringOutput
import io.github.depermitto.bullettrain.train.WorkoutPhase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate

interface Entity {
    val id: Int
    fun clone(id: Int): Entity
}

enum class UnitSystem { Metric, Imperial }

@Serializable
data class Settings(val unitSystem: UnitSystem = UnitSystem.Metric)

@Serializable
data class HistoryRecord(
    @SerialName("historyRecordId") override val id: Int = 0,
    val relatedProgram: Program,
    val workout: Day,
    val workoutPhase: WorkoutPhase,
    @Serializable(with = LocalDateSerializer::class) val date: LocalDate,
    @Serializable(with = InstantSerializer::class) val workoutStartTs: Instant,
) : Entity {
    override fun clone(id: Int) = copy(id = id)
}

@Serializable
data class Program(
    @SerialName("programId") override val id: Int = 0,
    val name: String = "",
    val days: List<Day> = listOf(Day()),
    val nextDayIndex: Int = 0,
    val followed: Boolean = false,
    val draft: Boolean = false,
    @Serializable(with = LocalDateSerializer::class) val mostRecentWorkoutDate: LocalDate? = null,
) : Entity {
    override fun clone(id: Int) = copy(id = id)

    infix fun corresponds(other: Program) = this.id == other.id

    fun nextDay() = days[nextDayIndex]

    companion object {
        val EmptyWorkout = Program(id = -1, name = "Empty Workout")
    }
}

@Serializable
data class Day(
    val name: String = "Day 1",
    val exercises: List<Exercise> = listOf(),
)

@Serializable
data class Exercise(
    @SerialName("exerciseId") override val id: Int = 0,
    val name: String,
    @SerialName("performanceVariableCategory") val perfVarCategory: PerfVarCategory = PerfVarCategory.Reps,
    val intensity: Intensity? = null,
    val sets: List<ExerciseSet> = listOf(),
    val superset: List<Int>? = null,
    val alternatives: List<Int>? = null,
    val notes: String = "",
) : Entity {
    val hasIntensity: Boolean
        get() = intensity != null

    fun getPerformedSets(): List<ExerciseSet> = this.sets.filter { it.completed }
    fun lastPerformedSet() = this.sets.lastOrNull { it.completed }

    override fun clone(id: Int) = copy(id = id)
}

@Serializable
data class ExerciseSet(
    @SerialName("targetPerformanceVariable") val targetPerfVar: PerfVar,
    @SerialName("actualPerformanceVariable") val actualPerfVar: Float = 0f,
    val targetIntensity: Intensity? = null,
    val actualIntensity: Float? = null,
    val weight: Float = 0f,
    @Serializable(with = InstantSerializer::class) val doneTs: Instant? = null,
) {
    val completed = doneTs != null
}

// TODO implement these as choices
@Serializable
enum class Intensity { RPE, RIR, PercentOf1RM }

@Serializable
enum class PerfVarCategory {
    Reps, RepRange, Time, TimeRange;

    val prettyName = name.split(regex = Regex("(?=[A-Z])")).joinToString(" ")
    val trainName get() = if (this == RepRange) "Reps" else name
}

@Serializable
sealed class PerfVar(val category: PerfVarCategory) {
    @Serializable
    data class Reps(val reps: Float = 0f) : PerfVar(PerfVarCategory.Reps)

    @Serializable
    data class Time(val time: Float = 0f) : PerfVar(PerfVarCategory.Time)

    @Serializable
    data class RepRange(val min: Float = 0f, val max: Float = 0f) : PerfVar(PerfVarCategory.RepRange)

    @Serializable
    data class TimeRange(val min: Float = 0f, val max: Float = 0f) : PerfVar(PerfVarCategory.TimeRange)

    companion object {
        fun of(category: PerfVarCategory) = when (category) {
            PerfVarCategory.Reps -> Reps()
            PerfVarCategory.Time -> Time()
            PerfVarCategory.RepRange -> RepRange()
            PerfVarCategory.TimeRange -> TimeRange()
        }
    }

    fun encodeToStringOutput(): String = when (this) {
        is Reps -> if (this == Reps()) "" else reps.encodeToStringOutput() + if (reps == 1f) " rep" else " reps"
        is Time -> if (this == Time()) "" else time.encodeToStringOutput() + " min"
        is RepRange -> if (this == RepRange()) "" else "${min.encodeToStringOutput()}-${max.encodeToStringOutput()} reps"
        is TimeRange -> if (this == TimeRange()) "" else "${min.encodeToStringOutput()}-${max.encodeToStringOutput()} min"
    }
}
