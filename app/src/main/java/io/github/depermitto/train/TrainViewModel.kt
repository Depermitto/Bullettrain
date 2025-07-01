package io.github.depermitto.train

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.depermitto.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.min

enum class WorkoutPhase { NotStartedYet, During, Completed }

class TrainViewModel(day: Day, private val historyDao: HistoryDao) : ViewModel() {
    val name by mutableStateOf(day.name)
    val targetExercises = mutableStateListOf<Exercise>().apply {
        viewModelScope.launch {
            val session = historyDao.getUnfinishedBusiness()
            addAll(session?.targetDay?.exercises ?: day.exercises)
        }
    }
    val exercises = mutableStateListOf<Exercise>().apply {
        viewModelScope.launch {
            val session = historyDao.getUnfinishedBusiness()
            if (session != null) {
                addAll(session.day.exercises)
            } else {
                addAll(day.exercises.map { exercise ->
                    val trainingTargetCategory = when (exercise.targetCategory) {
                        ExerciseTargetCategory.RepRange -> ExerciseTargetCategory.Reps
                        else -> exercise.targetCategory
                    }

                    exercise.copy(targetCategory = trainingTargetCategory, sets = exercise.sets.map {
                        it.copy(
                            target = ExerciseTarget.of(trainingTargetCategory),
                            weight = 0f,
                            date = null,
                        )
                    })
                })
            }
        }
    }

    private lateinit var countingJob: Job
    private lateinit var start: Instant
    private var now by mutableStateOf(Instant.ofEpochMilli(0)) // This is just to initialize by mutableStateOf
    var workoutPhase by mutableStateOf(WorkoutPhase.NotStartedYet)
        private set

    init {
        viewModelScope.launch {
            val session = historyDao.getUnfinishedBusiness()
            if (session?.workoutPhase == WorkoutPhase.During) {
                startWorkoutOnce(session.workoutStartTime)
            }
        }
    }

    fun startWorkoutOnce(startTime: Instant? = null) {
        if (workoutPhase != WorkoutPhase.During) {
            now = Instant.now()
            start = startTime ?: now
            workoutPhase = WorkoutPhase.During

            countingJob = viewModelScope.launch {
                while (true) {
                    now = now.plusSeconds(1)
                    saveProgress()

                    delay(1000)
                }
            }
        }
    }

    fun stopWorkoutOnce() {
        if (workoutPhase == WorkoutPhase.During) {
            countingJob.cancel("Workout Finished")
            workoutPhase = WorkoutPhase.Completed
            saveProgress()
        }
    }

    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("m:ss").withZone(ZoneId.systemDefault())
    fun elapsedSince(instant: Instant = start): String {
        return if (workoutPhase == WorkoutPhase.NotStartedYet) ""
        else formatter.format(now.minusMillis(min(instant.toEpochMilli(), now.toEpochMilli())))
    }

    private fun saveProgress() = viewModelScope.launch {
        val session = historyDao.getUnfinishedBusiness()
        if (session != null) {
            historyDao.upsert(
                session.copy(
                    day = Day(name = name, exercises = exercises.toList()),
                    targetDay = Day(name = name, exercises = targetExercises.toList()),
                    workoutPhase = workoutPhase,
                )
            )
        } else {
            historyDao.upsert(
                HistoryRecord(
                    date = Instant.now(),
                    day = Day(name = name, exercises = exercises.toList()),
                    targetDay = Day(name = name, exercises = targetExercises.toList()),
                    workoutPhase = workoutPhase,
                    workoutStartTime = start
                )
            )
        }
    }

    companion object {
        fun Factory(day: Day, historyDao: HistoryDao) =
            viewModelFactory { initializer { TrainViewModel(day, historyDao) } }
    }
}
