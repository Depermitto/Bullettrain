package io.github.depermitto.data

import android.database.Cursor
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

@Database(entities = [Exercise::class, HistoryRecord::class, Program::class], version = 23, exportSchema = true)
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

class Converters {

// ---------------------------------------Instants-----------------------------------------------------

    @TypeConverter
    fun dateFromTimestamp(value: Long): Instant = Instant.ofEpochMilli(value)

    @TypeConverter
    fun dateToTimestamp(value: Instant): Long = value.toEpochMilli()

// ---------------------------------------Custom Types-------------------------------------------------

    @TypeConverter
    fun dayFromString(value: String): Day = Json.decodeFromString(value)

    @TypeConverter
    fun dayToString(value: Day): String = Json.encodeToString(value)

// ---------------------------------------Categories---------------------------------------------------

    @TypeConverter
    fun exerciseTargetFromString(value: String?): PerfVar? = value?.let(Json::decodeFromString)

    @TypeConverter
    fun exerciseTargetToString(value: PerfVar?): String? = value?.let(Json::encodeToString)

    @TypeConverter
    fun exerciseTargetCategoryFromString(value: String): PerfVarCategory = PerfVarCategory.valueOf(value)

    @TypeConverter
    fun exerciseTargetCategoryToString(value: PerfVarCategory): String = value.name

    @TypeConverter
    fun intensityCategoryFromString(value: String?): IntensityCategory? = value?.let(IntensityCategory::valueOf)

    @TypeConverter
    fun intensityCategoryToString(value: IntensityCategory?): String? = value?.name

// ----------------------------------------Lists-------------------------------------------------------

    @TypeConverter
    fun listExerciseSetFromString(value: String): List<ExerciseSet> = Json.decodeFromString(value)

    @TypeConverter
    fun listExerciseSetToString(value: List<ExerciseSet>): String = Json.encodeToString(value)

    @TypeConverter
    fun listIntFromString(value: String): List<Int> = Json.decodeFromString(value)

    @TypeConverter
    fun listIntToString(value: List<Int>): String = Json.encodeToString(value)

    @TypeConverter
    fun listDayFromString(value: String): List<Day> = Json.decodeFromString(value)

    @TypeConverter
    fun listDayToString(value: List<Day>): String = Json.encodeToString(value)
}
