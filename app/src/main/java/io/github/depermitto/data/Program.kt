package io.github.depermitto.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

@Dao
interface ProgramDao {
    @Upsert
    suspend fun upsert(program: Program)

    @Delete
    suspend fun delete(program: Program)

    @Query("SELECT * FROM programs WHERE program_id = :id LIMIT 1")
    fun whereIdIs(id: Long): Flow<Program?>

    @Query("SELECT * FROM programs ORDER BY mostRecentWorkoutDate DESC")
    fun getAll(): Flow<List<Program>>

    @Query("SELECT * FROM programs WHERE followed = 1")
    fun getFollowed(): Flow<List<Program>>
}

@Serializable
@Entity(tableName = "programs")
data class Program(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "program_id") val programId: Long = 0,
    val name: String = "",
    val days: List<Day> = listOf(Day()),
    val followed: Boolean = false,
    val nextDay: Int = 1,
    val weekStreak: Int = 1,
    @Contextual val mostRecentWorkoutDate: Instant? = null,
)

@Serializable
data class Day(
    val name: String = "Day 1",
    val exercises: List<Exercise> = listOf(),
)
