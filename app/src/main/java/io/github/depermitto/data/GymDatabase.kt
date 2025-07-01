package io.github.depermitto.data

import android.database.Cursor
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import io.github.depermitto.data.entities.*

@Database(entities = [Exercise::class, HistoryRecord::class, Program::class], version = 24, exportSchema = true)
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
