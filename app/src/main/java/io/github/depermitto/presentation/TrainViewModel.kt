package io.github.depermitto.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.depermitto.data.Day
import io.github.depermitto.data.Exercise
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.min

enum class WorkoutState { NotStartedYet, Started, Done }

@RequiresApi(Build.VERSION_CODES.O)
class TrainViewModel(day: Day) : ViewModel() {
    var name by mutableStateOf(day.name)
    var sets = mutableStateListOf<List<Exercise>>()

    private lateinit var countingJob: Job
    private lateinit var start: Instant
    private var now by mutableStateOf(Instant.ofEpochMilli(0))
    var workoutState by mutableStateOf(WorkoutState.NotStartedYet)
        private set

    fun startWorkoutOnce() {
        if (workoutState != WorkoutState.Started) {
            now = Instant.now()
            if (!this::start.isInitialized) start = now
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
        countingJob.cancel("Workout Finished")
        workoutState = WorkoutState.Done
    }

    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("m:ss").withZone(ZoneId.systemDefault())
    fun elapsedSince(instant: Instant = start): String {
        return if (workoutState == WorkoutState.NotStartedYet) ""
        else formatter.format(now.minusMillis(min(instant.toEpochMilli(), now.toEpochMilli())))
    }
}

class TrainViewModelFactory(private val day: Day) : ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return TrainViewModel(day) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
