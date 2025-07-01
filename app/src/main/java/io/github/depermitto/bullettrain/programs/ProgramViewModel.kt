package io.github.depermitto.bullettrain.programs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.depermitto.bullettrain.database.Day
import io.github.depermitto.bullettrain.database.Exercise
import io.github.depermitto.bullettrain.database.Program
import io.github.depermitto.bullettrain.util.smallListSet

class ProgramViewModel(private val baseProgram: Program) : ViewModel() {
    var programId = baseProgram.id
        private set
    var programName by mutableStateOf(baseProgram.name)
    private var days = mutableStateListOf<Day>().apply { addAll(baseProgram.days) }
    var nextDayIndex = baseProgram.nextDayIndex
        private set
    var followed by mutableStateOf(baseProgram.followed)
        private set
    var draft by mutableStateOf(baseProgram.draft)
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

    val hasChanged get() = getDays() != baseProgram.days
    fun hasContent(ignoreDay1: Boolean = false): Boolean {
        if (programName.isNotBlank()) return true
        return if (!ignoreDay1) {
            val isDefault = days.toList() == listOf(Day())
            if (!isDefault) days.isNotEmpty() else false
        } else days.isNotEmpty()
    }

    fun getProgram(): Program = Program(
        id = programId,
        name = programName,
        days = getDays(),
        followed = followed,
        draft = draft,
        nextDayIndex = nextDayIndex,
        mostRecentWorkoutDate = null
    )

    fun revertToDefault() {
        val defaultProgram = Program()
        programId = defaultProgram.id
        programName = defaultProgram.name
        days = defaultProgram.days.toMutableStateList()
        followed = defaultProgram.followed
        draft = defaultProgram.draft
        nextDayIndex = defaultProgram.nextDayIndex
    }

    companion object {
        fun Factory(program: Program) = viewModelFactory { initializer { ProgramViewModel(program) } }
    }
}
