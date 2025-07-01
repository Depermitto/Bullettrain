package io.github.depermitto.bullettrain.programs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.depermitto.bullettrain.database.Day
import io.github.depermitto.bullettrain.database.Exercise
import io.github.depermitto.bullettrain.database.Program
import io.github.depermitto.bullettrain.util.smallListSet

class ProgramViewModel(program: Program) : ViewModel() {
    val programId = program.id
    var programName by mutableStateOf(program.name)
    private var days = mutableStateListOf<Day>().apply { addAll(program.days) }
    var followed by mutableStateOf(program.followed)
        private set

    fun getDays(): List<Day> = days
    fun getDay(dayIndex: Int) = days[dayIndex]
    fun addDay(day: Day) = days.add(day)
    fun addDay() = addDay(Day("Day ${days.size + 1}"))
    fun setDay(dayIndex: Int, day: Day) = days.set(dayIndex, day)
    fun removeDayAt(dayIndex: Int) = days.removeAt(dayIndex)
    fun setExercise(dayIndex: Int, exerciseIndex: Int, exercise: Exercise) = setDay(
        dayIndex, getDay(dayIndex).copy(exercises = getDay(dayIndex).exercises.smallListSet(exerciseIndex, exercise))
    )

    fun areDaysEqual(program: Program): Boolean = getDays().toList() == program.days.toList()
    fun hasContent(ignoreDay1: Boolean = false): Boolean {
        if (programName.isNotBlank()) return true
        return if (!ignoreDay1) {
            val isDefault = days.toList() == listOf(Day())
            if (!isDefault) days.isNotEmpty() else false
        } else days.isNotEmpty()
    }

    fun constructProgram(): Program = Program(id = programId, name = programName, days = days.toList(), followed = followed)
    fun clear() {
        programName = ""
        days.clear()
        days.add(Day())
    }

    companion object {
        fun Factory(program: Program) = viewModelFactory { initializer { ProgramViewModel(program) } }
    }
}
