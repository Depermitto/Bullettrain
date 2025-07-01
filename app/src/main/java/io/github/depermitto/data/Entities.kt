package io.github.depermitto.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

// ---------------------------------------Exercises---------------------------------------------------

@Serializable
@Entity(tableName = "exercises")
data class Exercise(
    @SerialName("exercise-id") @ColumnInfo(name = "exercise_id") @PrimaryKey(autoGenerate = true) val exerciseId: Long = 0,
    var name: String,
    val targetCategory: ExerciseTargetCategory = ExerciseTargetCategory.Reps,
    val intensityCategory: IntensityCategory? = null,
    @Contextual val sets: SnapshotStateList<ExerciseSet>,
    @Contextual val superset: SnapshotStateList<Int>? = null,
    @Contextual val alternatives: SnapshotStateList<Int>? = null,
    val notes: String = "",
)

@Serializable
data class ExerciseSet(
    val target: ExerciseTarget,
    val intensity: Float? = null, // TODO make this an optional field
    val weight: Float = 0f,
    @Contextual val date: Instant? = null,
)

enum class IntensityCategory { RPE, AMRAP, RIR }

enum class ExerciseTargetCategory { Reps, Time, RepRange }

@Serializable
sealed class ExerciseTarget(val category: ExerciseTargetCategory) {
    @Serializable
    data class Reps(val reps: Int = 0) : ExerciseTarget(ExerciseTargetCategory.Reps)

    @Serializable
    data class Time(val time: Long = 0) : ExerciseTarget(ExerciseTargetCategory.Time)

    @Serializable
    data class RepRange(val min: Int = 0, val max: Int = 0) : ExerciseTarget(ExerciseTargetCategory.RepRange)

    val name = this.category.name.split("(?=[A-Z])")

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

@Entity(tableName = "history")
data class HistoryEntry(
    @ColumnInfo(name = "history_entry_id") @PrimaryKey(autoGenerate = true) val historyEntryId: Long = 0,
    val target: ExerciseTarget,
    val intensity: Float?,
    val weight: Float,
    val date: Instant,
)

// ---------------------------------------Programs---------------------------------------------------

@Serializable
@Entity(tableName = "programs")
data class Program(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "program_id") val programId: Long = 0,
    val name: String = "",
    @Contextual val days: SnapshotStateList<Day> = mutableStateListOf(),
)

@Serializable
data class Day(
    val name: String = "Day 1",
    @Contextual val exerciseSets: SnapshotStateList<Exercise> = mutableStateListOf(),
)
