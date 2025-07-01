package io.github.depermitto.data

import androidx.room.*
import io.github.depermitto.train.WorkoutPhase
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

@Dao
interface HistoryDao {
    @Upsert
    suspend fun upsert(entry: HistoryRecord)

    @Delete
    suspend fun delete(entry: HistoryRecord)

    @Query("SELECT * FROM history")
    fun getAllFlow(): Flow<List<HistoryRecord>>

    @Query("SELECT * FROM history WHERE workoutPhase = 'During'")
    suspend fun getUnfinishedBusiness(): HistoryRecord?
}

@Serializable
@Entity(tableName = "history")
data class HistoryRecord(
    @ColumnInfo(name = "history_entry_id") @PrimaryKey(autoGenerate = true) val historyEntryId: Long = 0,
    @Contextual val date: Instant,
    val day: Day,
    val workoutPhase: WorkoutPhase,
    @Contextual val workoutStartTime: Instant,
)
