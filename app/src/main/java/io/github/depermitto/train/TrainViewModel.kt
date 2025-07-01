package io.github.depermitto.train

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.depermitto.data.entities.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.min

@Serializable
enum class WorkoutPhase { During, Completed }

class TrainViewModel(private val historyDao: HistoryDao, private val programDao: ProgramDao) : ViewModel() {
    var name by mutableStateOf("")
        private set
    var exercises = mutableStateListOf<Exercise>()
        private set
    var workoutPhase by mutableStateOf(WorkoutPhase.Completed)
        private set
    private var relatedProgramId: Long = 0
    private lateinit var start: Instant
    private var countingJob: Job? = null
    private var now by mutableStateOf(Instant.ofEpochMilli(0)) // This is just to initialize by mutableStateOf

    suspend fun restoreWorkout(): Boolean {
        val session = historyDao.getUnfinishedBusiness()
        if (session != null && session.workoutPhase == WorkoutPhase.During) {
            name = session.day.name
            exercises = session.day.exercises.toMutableStateList()
            relatedProgramId = session.relatedProgramId
            workoutPhase = session.workoutPhase
            start = session.workoutStartTime

            startCounting()
            return true
        }
        return false
    }

    fun startWorkoutOnce(day: Day, programId: Long) {
        if (workoutPhase == WorkoutPhase.Completed) {
            name = day.name
            exercises = day.exercises.toMutableStateList()
            relatedProgramId = programId
            workoutPhase = WorkoutPhase.During
            start = Instant.now()

            startCounting()
        }
    }

    fun stopWorkoutOnce() {
        if (workoutPhase == WorkoutPhase.During) {
            countingJob?.cancel("Workout Finished")
            countingJob = null
            workoutPhase = WorkoutPhase.Completed

            viewModelScope.launch {
                val program = programDao.whereId(relatedProgramId) ?: return@launch
                val nextDay = (program.nextDay + 1) % program.days.size
                programDao.upsert(
                    program.copy(
                        nextDay = nextDay,
                        weekStreak = program.weekStreak + if (nextDay == 0) 1 else 0
                    )
                )
                saveProgress()
            }
        }
    }

    private fun startCounting() {
        if (countingJob == null) {
            now = Instant.now()
            countingJob = viewModelScope.launch {
                while (true) {
                    now = now.plusSeconds(1)

                    if (now.epochSecond % 10 == 0L) {
                        launch(Dispatchers.IO) {
                            saveProgress()
                        }
                    }

                    delay(1000)
                }
            }
        }
    }

    private suspend fun saveProgress() {
        val session = historyDao.getUnfinishedBusiness()
        if (session != null) {
            historyDao.upsert(
                session.copy(
                    day = Day(name = name, exercises = exercises.toList()),
                    workoutPhase = workoutPhase,
                )
            )
        } else {
            historyDao.upsert(
                HistoryRecord(
                    day = Day(name = name, exercises = exercises.toList()),
                    relatedProgramId = relatedProgramId,
                    workoutPhase = workoutPhase,
                    date = Instant.now(),
                    workoutStartTime = start
                )
            )
        }
    }

    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("m:ss").withZone(ZoneId.systemDefault())
    fun elapsedSince(instant: Instant = start): String {
        return if (workoutPhase == WorkoutPhase.Completed) ""
        else formatter.format(now.minusMillis(min(instant.toEpochMilli(), now.toEpochMilli())))
    }

    companion object {
        fun Factory(historyDao: HistoryDao, programDao: ProgramDao) =
            viewModelFactory { initializer { TrainViewModel(historyDao, programDao) } }
    }
}
