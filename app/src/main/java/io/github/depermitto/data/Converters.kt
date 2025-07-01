package io.github.depermitto.data

import androidx.room.TypeConverter
import io.github.depermitto.data.entities.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

class Converters {
    @TypeConverter
    fun instantFromTimestamp(value: String): Instant = Instant.parse(value)

    @TypeConverter
    fun instantToTimestamp(instant: Instant): String = instant.toString()

    @TypeConverter
    fun dayFromString(value: String): Day = Json.decodeFromString(value)

    @TypeConverter
    fun dayToString(day: Day): String = Json.encodeToString(day)

    @TypeConverter
    fun programFromString(value: String): Program = Json.decodeFromString(value)

    @TypeConverter
    fun programToString(program: Program): String = Json.encodeToString(program)

// ---------------------------------------Categories---------------------------------------------------

    @TypeConverter
    fun exerciseTargetCategoryFromString(value: String): PerfVarCategory = PerfVarCategory.valueOf(value)

    @TypeConverter
    fun exerciseTargetCategoryToString(perfVarCategory: PerfVarCategory): String = perfVarCategory.name

    @TypeConverter
    fun intensityCategoryFromString(value: String?): IntensityCategory? = value?.let(IntensityCategory::valueOf)

    @TypeConverter
    fun intensityCategoryToString(intensityCategory: IntensityCategory?): String? = intensityCategory?.name

// ----------------------------------------Lists-------------------------------------------------------

    @TypeConverter
    fun listExerciseSetFromString(value: String): List<ExerciseSet> = Json.decodeFromString(value)

    @TypeConverter
    fun listExerciseSetToString(exerciseSets: List<ExerciseSet>): String = Json.encodeToString(exerciseSets)

    @TypeConverter
    fun listIntFromString(value: String): List<Int> = Json.decodeFromString(value)

    @TypeConverter
    fun listIntToString(ints: List<Int>): String = Json.encodeToString(ints)

    @TypeConverter
    fun listDayFromString(value: String): List<Day> = Json.decodeFromString(value)

    @TypeConverter
    fun listDayToString(days: List<Day>): String = Json.encodeToString(days)
}