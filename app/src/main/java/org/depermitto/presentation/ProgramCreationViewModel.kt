package org.depermitto.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.depermitto.data.Day
import org.depermitto.set

data class ProgramCreationState(
    val workoutName: String = "",
    val days: List<Day> = listOf(Day("Day 1")),
)

class ProgramCreationViewModel : ViewModel() {
    var state by mutableStateOf(ProgramCreationState())
        private set

    fun reset() {
        state = ProgramCreationState()
    }

    fun setWorkoutName(name: String) {
        state = state.copy(workoutName = name)
    }

    fun addDay(day: Day) {
        state = state.copy(days = state.days + day)
    }

    fun removeDay(day: Day) {
        state = state.copy(days = state.days - day)
    }

    fun setDayAt(index: Int, new: Day) {
        state = state.copy(days = state.days.set(index, new))
    }
}