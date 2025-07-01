package io.github.depermitto.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.depermitto.data.Day
import io.github.depermitto.data.Program
import io.github.depermitto.set

class ProgramViewModel : ViewModel() {
    var state by mutableStateOf(Program())
        private set

    fun reset(state: Program = Program()): ProgramViewModel {
        this.state = state
        return this
    }

    fun setWorkoutName(name: String) {
        state = state.copy(name = name)
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