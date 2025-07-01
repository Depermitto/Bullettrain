package org.depermitto.database

import android.database.Cursor
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

@Database(
    entities = [ExerciseListing::class, HistoryEntry::class, Program::class], version = 5, exportSchema = true
)
@TypeConverters(Converters::class)
abstract class GymDatabase : RoomDatabase() {
    abstract fun getGymDao(): GymDao
    abstract fun getExerciseDao(): ExerciseDao
    abstract fun getHistoryDao(): HistoryDao
    abstract fun getProgramDao(): ProgramDao

    fun checkpoint() = getGymDao().rawQuery(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))
}

@Entity(tableName = "exercises")
@Serializable
data class ExerciseListing(
    @SerialName("exercise-id") @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "exercise_id") val exerciseId: Long = 0,
    val name: String,
)

@Dao
interface GymDao {
    @RawQuery
    fun rawQuery(query: SimpleSQLiteQuery): Cursor
}

@Dao
interface ExerciseDao {
    @Upsert
    suspend fun upsert(listing: ExerciseListing)

    @Delete
    suspend fun delete(listing: ExerciseListing)

    @Query("SELECT * FROM exercises")
    fun getAllFlow(): Flow<List<ExerciseListing>>
}

@Entity(tableName = "history")
data class HistoryEntry(
    @SerialName("history-entry-id") @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "history_entry_id") val historyEntryId: Long = 0,
    val reps: Float,
    val rpe: Float,
    val date: Date,
)

@Dao
interface HistoryDao {
    @Upsert
    suspend fun upsert(entry: HistoryEntry)

    @Delete
    suspend fun delete(entry: HistoryEntry)

    @Query("SELECT * FROM history")
    fun getAllFlow(): Flow<List<HistoryEntry>>
}

@Entity(tableName = "programs")
data class Program(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "program_id") val programId: Long = 0,
    val name: String,
    val trainingWork: TrainingWork,
)

typealias TrainingWork = List<Day>

@Serializable
data class Day(
    val name: String,
    val exercises: List<WorkoutEntry>,
)

@Serializable
data class WorkoutEntry(
    val exercise: ExerciseListing,
    @SerialName("target-reps") val targetReps: Float? = null,
    @SerialName("target-rpe") val targetRPE: Float? = null,
    val superset: WorkoutEntry? = null,
    val alternatives: List<WorkoutEntry>? = null,
    val notes: String = "",
)

@Dao
interface ProgramDao {
    @Upsert
    suspend fun upsert(program: Program)

    @Delete
    suspend fun delete(program: Program)

    @Query("SELECT * FROM programs")
    fun getAllFlow(): Flow<List<Program>>
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long): Date = Date(value)

    @TypeConverter
    fun dateToTimestamp(date: Date): Long = date.time

    @TypeConverter
    fun fromString(value: String): TrainingWork = Json.decodeFromString(value)

    @TypeConverter
    fun daysToString(work: TrainingWork): String = Json.encodeToString(work)
}
