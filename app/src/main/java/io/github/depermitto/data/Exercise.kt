package io.github.depermitto.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

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
    data class Reps(val reps: Float = 0f) : ExerciseTarget(ExerciseTargetCategory.Reps)

    @Serializable
    data class Time(val time: Float = 0f) : ExerciseTarget(ExerciseTargetCategory.Time)

    @Serializable
    data class RepRange(val min: Float = 0f, val max: Float = 0f) : ExerciseTarget(ExerciseTargetCategory.RepRange)

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
        is Time -> "%.2f".format(time) + " min"
    }
}
