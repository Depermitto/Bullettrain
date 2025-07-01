package org.depermitto.data

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
    entities = [Exercise::class, HistoryEntry::class, Program::class], version = 6, exportSchema = true
)
@TypeConverters(Converters::class)
abstract class GymDatabase : RoomDatabase() {
    abstract fun getGymDao(): GymDao
    abstract fun getExerciseDao(): ExerciseDao
    abstract fun getHistoryDao(): HistoryDao
    abstract fun getProgramDao(): ProgramDao

    fun checkpoint() = getGymDao().rawQuery(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))
}

@Dao
interface GymDao {
    @RawQuery
    fun rawQuery(query: SimpleSQLiteQuery): Cursor
}

@Entity(tableName = "exercises")
@Serializable
data class Exercise(
    @SerialName("exercise-id") @ColumnInfo(name = "exercise_id") @PrimaryKey(autoGenerate = true) val exerciseId: Long = 0,
    var name: String,
    val reps: Float = 0f,
    val rpe: Float = 0f,
    val superset: Exercise? = null,
    val alternatives: List<Exercise>? = null,
    val notes: String = "",
)

@Dao
interface ExerciseDao {
    @Upsert
    suspend fun upsert(listing: Exercise)

    @Delete
    suspend fun delete(listing: Exercise)

    @Query("SELECT * FROM exercises")
    fun getAllFlow(): Flow<List<Exercise>>
}

@Entity(tableName = "history")
data class HistoryEntry(
    @SerialName("history-entry-id") @ColumnInfo(name = "history_entry_id") @PrimaryKey(autoGenerate = true) val historyEntryId: Long = 0,
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
    val trainingWork: List<Day>,
)

@Serializable
data class Day(
    val name: String,
    val exercises: List<List<Exercise>> = listOf(),
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
    fun dateFromTimestamp(value: Long): Date = Date(value)

    @TypeConverter
    fun dateToTimestamp(date: Date): Long = date.time

    @TypeConverter
    fun dayFromString(value: String): List<Day> = Json.decodeFromString(value)

    @TypeConverter
    fun daysToString(work: List<Day>): String = Json.encodeToString(work)

    @TypeConverter
    fun exerciseFromString(value: String?): Exercise? = null

    @TypeConverter
    fun exerciseToString(exercise: Exercise?): String? = null

    @TypeConverter
    fun listOfExerciseFromString(value: String?): List<Exercise>? = null

    @TypeConverter
    fun listOfExerciseToString(exercises: List<Exercise>?): String? = null
}
