package io.github.depermitto.train

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.depermitto.data.Day
import io.github.depermitto.data.Exercise
import io.github.depermitto.data.ExerciseTarget
import io.github.depermitto.data.ExerciseTargetCategory
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.min

enum class WorkoutState { NotStartedYet, Started, Done }

class TrainViewModel(day: Day) : ViewModel() {
    val name = mutableStateOf(day.name)
    val targetExercises = mutableStateListOf<Exercise>().apply { addAll(day.exercises) }
    val exercises = mutableStateListOf<Exercise>().apply {
        addAll(day.exercises.map { exercise ->
            exercise.copy(targetCategory = when (exercise.targetCategory) {
                ExerciseTargetCategory.RepRange -> ExerciseTargetCategory.Reps
                else -> exercise.targetCategory
            }, sets = exercise.sets.map {
                it.copy(
                    target = ExerciseTarget.of(exercise.targetCategory),
                    weight = 0f,
                    date = null,
                )
            })
        })
    }

    private lateinit var countingJob: Job
    private lateinit var start: Instant
    private var now by mutableStateOf(Instant.ofEpochMilli(0))
    var workoutState by mutableStateOf(WorkoutState.NotStartedYet)
        private set

    fun startWorkoutOnce() {
        if (workoutState != WorkoutState.Started) {
            now = Instant.now()
            start = now
            workoutState = WorkoutState.Started

            countingJob = viewModelScope.launch {
                while (true) {
                    now = now.plusSeconds(1)
                    delay(1000)
                }
            }
        }
    }

    fun stopWorkoutOnce() {
        if (workoutState == WorkoutState.Started) {
            countingJob.cancel("Workout Finished")
            workoutState = WorkoutState.NotStartedYet
        }
    }

    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("m:ss").withZone(ZoneId.systemDefault())
    fun elapsedSince(instant: Instant = start): String {
        return if (workoutState == WorkoutState.NotStartedYet) ""
        else formatter.format(now.minusMillis(min(instant.toEpochMilli(), now.toEpochMilli())))
    }

    companion object {
        fun Factory(day: Day) = viewModelFactory { initializer { TrainViewModel(day) } }
    }
}
