package io.github.depermitto.programs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.depermitto.database.*

class ProgramViewModel(program: Program) : ViewModel() {
    private val programId = program.id
    var programName by mutableStateOf(program.name)
    var days = mutableStateListOf<Day>().apply { addAll(program.days) }
        private set
    var followed by mutableStateOf(program.followed)
        private set

    fun addDay() = days.add(Day("Day ${days.size + 1}"))
    fun addDay(day: Day) = days.add(day)
    fun setDay(dayIndex: Int, day: Day) = days.set(dayIndex, day)
    fun removeDayAt(dayIndex: Int) = days.removeAt(dayIndex)

    fun constructProgram(): Program =
        Program(id = programId, name = programName, days = days.toList(), followed = followed)

    companion object {
        fun Factory(program: Program, programDao: ProgramDao) =
            viewModelFactory { initializer { ProgramViewModel(program) } }
    }
}
