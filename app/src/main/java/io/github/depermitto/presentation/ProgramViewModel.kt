package io.github.depermitto.presentation

import androidx.compose.runtime.getValue
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
    var days by mutableStateOf(program.days)
    var name by mutableStateOf(program.name)

    fun upsert() = viewModelScope.launch {
        programDao.upsert(Program(programId = programId, name = name, days = days))
        name = ""
        days = listOf(Day())
    }

    companion object {
        fun Factory(program: Program, programDao: ProgramDao) =
            viewModelFactory { initializer { ProgramViewModel(program, programDao) } }
    }
}
