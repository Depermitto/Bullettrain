package io.github.depermitto.data

import android.database.Cursor
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

@Database(entities = [ExerciseSet::class, HistoryEntry::class, Program::class], version = 10, exportSchema = true)
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
data class ExerciseSet(
    @SerialName("exercise-id") @ColumnInfo(name = "exercise_id") @PrimaryKey(autoGenerate = true) val exerciseId: Long = 0,
    var name: String,
    val reps: Float = 0f,
    val rpe: Float = 0f,
    val superset: ExerciseSet? = null,
    val alternatives: List<ExerciseSet>? = null,
    val notes: String = "",
    @Contextual val date: Instant? = null,
)

@Dao
interface ExerciseDao {
    @Upsert
    suspend fun upsert(listing: ExerciseSet)

    @Delete
    suspend fun delete(listing: ExerciseSet)

    @Query("SELECT * FROM exercises")
    fun getAllFlow(): Flow<List<ExerciseSet>>
}

@Entity(tableName = "history")
data class HistoryEntry(
    @SerialName("history-entry-id") @ColumnInfo(name = "history_entry_id") @PrimaryKey(autoGenerate = true) val historyEntryId: Long = 0,
    val reps: Float,
    val rpe: Float,
    val date: Instant,
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
    val name: String = "",
    val days: List<Day> = listOf(Day()),
)

@Serializable
data class Day(
    val name: String = "Day 1",
    val exerciseSets: List<List<ExerciseSet>> = listOf(),
)

@Dao
interface ProgramDao {
    @Upsert
    suspend fun upsert(program: Program)

    @Delete
    suspend fun delete(program: Program)

    @Query("SELECT * FROM programs WHERE program_id = :id LIMIT 1")
    fun whereId(id: Long): Flow<Program?>

    @Query("SELECT * FROM programs")
    fun getAllFlow(): Flow<List<Program>>
}

class Converters {
    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun dateFromTimestamp(value: Long): Instant = Instant.ofEpochMilli(value)

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun dateToTimestamp(date: Instant): Long = date.toEpochMilli()

    @TypeConverter
    fun dayFromString(value: String): List<Day> = Json.decodeFromString(value)

    @TypeConverter
    fun daysToString(days: List<Day>): String = Json.encodeToString(days)

    @TypeConverter
    fun exerciseFromString(value: String): ExerciseSet = Json.decodeFromString(value)

    @TypeConverter
    fun exerciseToString(exerciseSet: ExerciseSet): String = Json.encodeToString(exerciseSet)

    @TypeConverter
    fun listOfExerciseFromString(value: String): List<ExerciseSet> = Json.decodeFromString(value)

    @TypeConverter
    fun listOfExerciseToString(exerciseSets: List<ExerciseSet>): String = Json.encodeToString(exerciseSets)
}
