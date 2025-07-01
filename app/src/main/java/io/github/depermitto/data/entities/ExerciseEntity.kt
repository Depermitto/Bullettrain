package io.github.depermitto.data.entities

import androidx.room.*
import io.github.depermitto.components.encodeToStringOutput
import io.github.depermitto.data.InstantSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import java.time.Instant

// TODO TimeRange, Intensity variations?
@Dao
interface ExerciseDao {
    @Upsert
    suspend fun upsert(exercise: Exercise)

    @Delete
    suspend fun delete(exercise: Exercise)

    @Query("SELECT * FROM exercises")
    fun getAllFlow(): Flow<List<Exercise>>
}

@Serializable
@Entity(tableName = "exercises")
data class Exercise(
    @ColumnInfo(name = "exercise_id") @PrimaryKey(autoGenerate = true) val exerciseId: Long = 0,
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

    fun toText(): String = when (this) {
        is RepRange -> "${min.encodeToStringOutput()}-${max.encodeToStringOutput()}"
        is Reps -> reps.encodeToStringOutput()
        is Time -> time.encodeToStringOutput() + " min"
    }
}
