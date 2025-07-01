package io.github.depermitto.programs

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.depermitto.data.Day
import io.github.depermitto.data.ExerciseSet
import io.github.depermitto.data.Program
import io.github.depermitto.data.ProgramDao
import kotlinx.coroutines.launch

data class MutableDay(val name: String, val exercises: SnapshotStateList<SnapshotStateList<ExerciseSet>>) {
    fun toDay() = Day(name, exercises.map { it.toList() })

    companion object {
        fun of(day: Day): MutableDay =
            MutableDay(day.name, day.exerciseSets.map { it.toMutableStateList() }.toMutableStateList())
    }
}

class ProgramViewModel(program: Program, private val programDao: ProgramDao) : ViewModel() {
    private val programId = program.programId
    var days = mutableStateListOf<MutableDay>().apply {
        addAll(program.days.map { day -> MutableDay.of(day) })
    }
        private set
    var name by mutableStateOf(program.name)

    fun addDay() = days.add(MutableDay("Day ${days.size + 1}", mutableStateListOf()))
    fun setDay(index: Int, day: MutableDay) {
        days[index] = day
    }

    fun removeDayAt(index: Int) = days.removeAt(index)
    fun getDays() = days.map { it.toDay() }

    fun upsert() = viewModelScope.launch {
        programDao.upsert(Program(programId = programId, name = name, days = getDays()))
        name = ""
        days.clear()
    }

    companion object {
        fun Factory(program: Program, programDao: ProgramDao) =
            viewModelFactory { initializer { ProgramViewModel(program, programDao) } }
    }
}
