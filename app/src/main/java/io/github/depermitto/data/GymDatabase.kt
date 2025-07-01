package io.github.depermitto.data

import android.database.Cursor
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

@Database(entities = [ExerciseSet::class, HistoryEntry::class, Program::class], version = 14, exportSchema = true)
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

@Dao
interface ExerciseDao {
    @Upsert
    suspend fun upsert(listing: ExerciseSet)

    @Delete
    suspend fun delete(listing: ExerciseSet)

    @Query("SELECT * FROM exercises")
    fun getAllFlow(): Flow<List<ExerciseSet>>
}

@Dao
interface HistoryDao {
    @Upsert
    suspend fun upsert(entry: HistoryEntry)

    @Delete
    suspend fun delete(entry: HistoryEntry)

    @Query("SELECT * FROM history")
    fun getAllFlow(): Flow<List<HistoryEntry>>
}

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
    @TypeConverter
    fun dateFromTimestamp(value: Long): Instant = Instant.ofEpochMilli(value)

    @TypeConverter
    fun dateToTimestamp(date: Instant): Long = date.toEpochMilli()

    @TypeConverter
    fun exerciseTargetFromString(value: String?): ExerciseTarget? = value?.let(Json::decodeFromString)

    @TypeConverter
    fun exerciseTargetToString(target: ExerciseTarget?): String? = target?.let(Json::encodeToString)

    @TypeConverter
    fun targetCategoryFromString(value: String): ExerciseTargetCategory = ExerciseTargetCategory.valueOf(value)

    @TypeConverter
    fun targetCategoryToString(target: ExerciseTargetCategory): String = target.name
}
