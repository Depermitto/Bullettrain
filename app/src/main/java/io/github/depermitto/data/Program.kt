package io.github.depermitto.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Dao
interface ProgramDao {
    @Upsert
    suspend fun upsert(program: Program)

    @Delete
    suspend fun delete(program: Program)

    @Query("SELECT * FROM programs WHERE program_id = :id LIMIT 1")
    fun whereIdIs(id: Long): Flow<Program?>

    @Query("SELECT * FROM programs")
    fun getAllFlow(): Flow<List<Program>>

    @Query("SELECT * FROM programs WHERE active = 1 LIMIT 1")
    fun getActiveProgram(): Flow<Program?>
}

@Entity(tableName = "programs")
data class Program(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "program_id") val programId: Long = 0,
    val name: String = "",
    val days: List<Day> = listOf(Day()),
    val active: Boolean = false,
    val nextDay: Int = 0,
    val weekStreak: Int = 0,
)

@Serializable
data class Day(
    val name: String = "Day 1",
    val exercises: List<Exercise> = listOf(),
)
