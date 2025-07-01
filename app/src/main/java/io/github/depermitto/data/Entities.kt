package io.github.depermitto.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

operator fun <T> List<T>.set(index: Int, value: T): List<T> {
    return slice(0 until index) + value + slice(index + 1 until size)
}

// ---------------------------------------Exercises---------------------------------------------------

@Serializable
@Entity(tableName = "exercises")
data class Exercise(
    @ColumnInfo(name = "exercise_id") @PrimaryKey(autoGenerate = true) val exerciseId: Long = 0,
    val name: String,
    val targetCategory: ExerciseTargetCategory = ExerciseTargetCategory.Reps,
    val intensityCategory: IntensityCategory? = null,
    val sets: List<ExerciseSet> = listOf(),
    val superset: List<Int>? = null,
    val alternatives: List<Int>? = null,
    val notes: String = "",
) {
    val hasIntensity: Boolean
        get() = intensityCategory != null
}

@Serializable
data class ExerciseSet(
    val target: ExerciseTarget,
    val intensity: Float? = null,
    val weight: Float = 0f,
    @Contextual val date: Instant? = null,
)

@Serializable
enum class IntensityCategory { RPE, AMRAP, RIR }

@Serializable
enum class ExerciseTargetCategory {
    Reps, Time, RepRange;

    val prettyName = name.split(regex = Regex("(?=[A-Z])")).joinToString(" ")
}

@Serializable
sealed class ExerciseTarget(val category: ExerciseTargetCategory) {
    @Serializable
    data class Reps(val reps: Int = 0) : ExerciseTarget(ExerciseTargetCategory.Reps)

    @Serializable
    data class Time(val time: Long = 0) : ExerciseTarget(ExerciseTargetCategory.Time)

    @Serializable
    data class RepRange(val min: Int = 0, val max: Int = 0) : ExerciseTarget(ExerciseTargetCategory.RepRange)

    companion object {
        fun of(category: ExerciseTargetCategory) = when (category) {
            ExerciseTargetCategory.Reps -> Reps()
            ExerciseTargetCategory.Time -> Time()
            ExerciseTargetCategory.RepRange -> RepRange()
        }
    }

    fun toText(): String = when (this) {
        is RepRange -> "$min - $max"
        is Reps -> reps.toString()
        is Time -> "${time / 60}:${time % 60}"
    }
}

// ---------------------------------------History----------------------------------------------------

@Serializable
@Entity(tableName = "history")
data class HistoryEntry(
    @ColumnInfo(name = "history_entry_id") @PrimaryKey(autoGenerate = true) val historyEntryId: Long = 0,
    val target: Float,
    val intensity: Float?,
    val weight: Float,
    @Contextual val date: Instant,
)

// ---------------------------------------Programs---------------------------------------------------

@Entity(tableName = "programs")
data class Program(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "program_id") val programId: Long = 0,
    val name: String = "",
    val days: List<Day> = listOf(),
)

@Serializable
data class Day(
    val name: String = "Day 1",
    val exercises: List<Exercise> = listOf(),
)
