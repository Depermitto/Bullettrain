package io.github.depermitto.data.entities

import androidx.room.*
import io.github.depermitto.data.InstantSerializer
import io.github.depermitto.train.WorkoutPhase
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.Month

@Dao
interface HistoryDao {
    @Upsert
    suspend fun upsert(record: HistoryRecord): Long

    @Update
    suspend fun update(record: HistoryRecord)

    @Delete
    suspend fun delete(vararg records: HistoryRecord)

    @Query("SELECT * FROM history")
    fun getAllFlow(): Flow<List<HistoryRecord>>

    @Query("SELECT * FROM history")
    suspend fun getAll(): List<HistoryRecord>

    @Query("SELECT * FROM history WHERE workoutPhase = 'During' LIMIT 1")
    suspend fun getUnfinishedBusiness(): HistoryRecord?
}

@Serializable
@Entity(tableName = "history")
data class HistoryRecord(
    @PrimaryKey(autoGenerate = true) val historyEntryId: Long = 0,
    val relatedProgram: Program,
    val workout: Day,
    val workoutPhase: WorkoutPhase,
    @Serializable(with = InstantSerializer::class) val date: Instant,
    @Serializable(with = InstantSerializer::class) val workoutStartTime: Instant,
)
