package io.github.depermitto.programs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.depermitto.data.Day
import io.github.depermitto.data.Program
import io.github.depermitto.data.ProgramDao
import kotlinx.coroutines.launch

class ProgramViewModel(program: Program, private val programDao: ProgramDao) : ViewModel() {
    private val programId = program.programId
    var programName by mutableStateOf(program.name)
        private set
    var days = mutableStateListOf<Day>().apply { addAll(program.days) }
        private set
    var followed by mutableStateOf(program.followed)
        private set

    fun setName(name: String) {
        programName = name
    }

    fun addDay() = days.add(Day("Day ${days.size + 1}"))
    fun setDay(dayIndex: Int, day: Day) = days.set(dayIndex, day)
    fun removeDayAt(dayIndex: Int) = days.removeAt(dayIndex)

    fun upsert() = viewModelScope.launch {
        programDao.upsert(Program(programId = programId, name = programName, days = days, followed = followed))
        programName = ""
        days.clear()
    }

    companion object {
        fun Factory(program: Program, programDao: ProgramDao) =
            viewModelFactory { initializer { ProgramViewModel(program, programDao) } }
    }
}
