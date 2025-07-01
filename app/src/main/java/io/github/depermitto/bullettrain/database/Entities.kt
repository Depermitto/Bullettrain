package io.github.depermitto.bullettrain.database

import io.github.depermitto.bullettrain.components.encodeToStringOutput
import io.github.depermitto.bullettrain.train.WorkoutPhase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

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
    @Serializable(with = InstantSerializer::class) val date: Instant,
    @Serializable(with = InstantSerializer::class) val workoutStartTime: Instant,
) : Entity {
    override fun clone(id: Int) = copy(id = id)
}

@Serializable
data class Program(
    @SerialName("programId") override val id: Int = 0,
    val name: String = "",
    val days: List<Day> = listOf(Day()),
    val followed: Boolean = false,
    val nextDay: Int = 0,
    val weekStreak: Int = 1,
    @Serializable(with = InstantSerializer::class) val mostRecentWorkoutDate: Instant? = null,
) : Entity {
    override fun clone(id: Int) = copy(id = id)
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
    val perfVarCategory: PerfVarCategory = PerfVarCategory.Reps,
    val intensityCategory: IntensityCategory? = null,
    val sets: List<ExerciseSet> = listOf(),
    val superset: List<Int>? = null,
    val alternatives: List<Int>? = null,
    val notes: String = "",
) : Entity {
    val hasIntensity: Boolean
        get() = intensityCategory != null

    val lastPerformedSet: ExerciseSet?
        get() = sets.lastOrNull { it.date != null }

    override fun clone(id: Int) = copy(id = id)
}

@Serializable
data class ExerciseSet(
    val targetPerfVar: PerfVar,
    val actualPerfVar: Float = 0f,
    // val targetIntensity
    val intensity: Float? = null,
    val weight: Float = 0f,
    @Serializable(with = InstantSerializer::class) val date: Instant? = null,
) {
    val completed = date != null
}

@Serializable
enum class IntensityCategory { RPE, AMRAP, RIR }

@Serializable
enum class PerfVarCategory {
    Reps, RepRange, Time;

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

    companion object {
        fun of(category: PerfVarCategory) = when (category) {
            PerfVarCategory.Reps -> Reps()
            PerfVarCategory.Time -> Time()
            PerfVarCategory.RepRange -> RepRange()
        }
    }

    fun encodeToStringOutput(): String = when (this) {
        is RepRange -> if (this == RepRange()) "" else "${min.encodeToStringOutput()}-${max.encodeToStringOutput()} reps"
        is Reps -> if (this == Reps()) "" else reps.encodeToStringOutput() + " reps"
        is Time -> if (this == Time()) "" else time.encodeToStringOutput() + " min"
    }
}
