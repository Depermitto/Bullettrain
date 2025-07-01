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
    var nextDay = program.nextDay
        private set
    var weekStreak = program.weekStreak
        private set

    fun getDays(): List<Day> = days.toList()
    fun getDay(dayIndex: Int) = days[dayIndex]
    fun addDay(day: Day) = days.add(day)
    fun addDay() = addDay(Day("Day ${days.size + 1}"))
    fun setDay(dayIndex: Int, day: Day) = days.set(dayIndex, day)
    fun removeDayAt(dayIndex: Int) = days.removeAt(dayIndex)
    fun setExercise(dayIndex: Int, exerciseIndex: Int, exercise: Exercise) = setDay(
        dayIndex, getDay(dayIndex).copy(exercises = getDay(dayIndex).exercises.smallListSet(exerciseIndex, exercise))
    )

    fun reorderDays(fromIndex: Int, toIndex: Int) = days.add(toIndex, days.removeAt(fromIndex))

    fun areDaysEqual(program: Program): Boolean = getDays() == program.days
    fun hasContent(ignoreDay1: Boolean = false): Boolean {
        if (programName.isNotBlank()) return true
        return if (!ignoreDay1) {
            val isDefault = days.toList() == listOf(Day())
            if (!isDefault) days.isNotEmpty() else false
        } else days.isNotEmpty()
    }

    fun constructProgram(): Program = Program(
        id = programId,
        name = programName,
        days = getDays(),
        followed = followed,
        nextDay = nextDay,
        weekStreak = weekStreak,
        mostRecentWorkoutDate = null
    )

    fun clear() {
        programName = ""
        days.clear()
        days.add(Day())
        followed = false
        nextDay = 0
        weekStreak = 1
    }

    companion object {
        fun Factory(program: Program) = viewModelFactory { initializer { ProgramViewModel(program) } }
    }
}
