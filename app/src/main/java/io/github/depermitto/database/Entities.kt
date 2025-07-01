package io.github.depermitto.database

import androidx.room.*
import io.github.depermitto.components.encodeToStringOutput
import io.github.depermitto.data.InstantSerializer
import io.github.depermitto.settings.UnitSystem
import io.github.depermitto.settings.UnitSystem.Metric
import io.github.depermitto.train.WorkoutPhase
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Settings(val unitSystem: UnitSystem = Metric)

@Serializable
data class HistoryRecord(
    @PrimaryKey(autoGenerate = true) val historyEntryId: Int = 0,
    val relatedProgram: Program,
    val workout: Day,
    val workoutPhase: WorkoutPhase,
    @Serializable(with = InstantSerializer::class) val date: Instant,
    @Serializable(with = InstantSerializer::class) val workoutStartTime: Instant,
)

@Serializable
data class Program(
    @PrimaryKey(autoGenerate = true) val programId: Int = 0,
    val name: String = "",
    val days: List<Day> = listOf(Day()),
    val followed: Boolean = false,
    val nextDay: Int = 0,
    val weekStreak: Int = 1,
    @Serializable(with = InstantSerializer::class) val mostRecentWorkoutDate: Instant? = null,
)

@Serializable
data class Day(
    val name: String = "Day 1",
    val exercises: List<Exercise> = listOf(),
)

// TODO TimeRange, Intensity variations?
@Serializable
data class Exercise(
    @PrimaryKey(autoGenerate = true) val exerciseId: Int = 0,
    val name: String,
    val perfVarCategory: PerfVarCategory = PerfVarCategory.Reps,
    val intensityCategory: IntensityCategory? = null,
    val sets: List<ExerciseSet> = listOf(),
    val superset: List<Int>? = null,
    val alternatives: List<Int>? = null,
    val notes: String = "",
) {
    val hasIntensity: Boolean
        get() = intensityCategory != null

    fun lastPerformedSet(): ExerciseSet? = sets.lastOrNull { it.date != null }
}

@Serializable
data class ExerciseSet(
    val targetPerfVar: PerfVar,
    val actualPerfVar: Float = 0f,
    // val targetIntensity
    val intensity: Float? = null,
    val weight: Float = 0f,
    @Serializable(with = InstantSerializer::class) val date: Instant? = null,
)

@Serializable
enum class IntensityCategory { RPE, AMRAP, RIR }

@Serializable
enum class PerfVarCategory {
    Reps, RepRange, Time;

    val prettyName = name.split(regex = Regex("(?=[A-Z])")).joinToString(" ")
    fun trainName() = if (this == RepRange) "Reps" else name
}

@Serializable
sealed class PerfVar() {
    @Serializable
    data class Reps(val reps: Float = 0f) : PerfVar()

    @Serializable
    data class Time(val time: Float = 0f) : PerfVar()

    @Serializable
    data class RepRange(val min: Float = 0f, val max: Float = 0f) : PerfVar()

    companion object {
        fun of(category: PerfVarCategory) = when (category) {
            PerfVarCategory.Reps -> Reps()
            PerfVarCategory.Time -> Time()
            PerfVarCategory.RepRange -> RepRange()
        }
    }

    fun toText(): String = when (this) {
        is RepRange -> "${min.encodeToStringOutput()}-${max.encodeToStringOutput()}"
        is Reps -> reps.encodeToStringOutput()
        is Time -> time.encodeToStringOutput() + " min"
    }
}
