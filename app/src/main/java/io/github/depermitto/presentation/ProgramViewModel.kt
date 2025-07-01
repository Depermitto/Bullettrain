package io.github.depermitto.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.depermitto.data.Program

class ProgramViewModel(program: Program) : ViewModel() {
    var days by mutableStateOf(program.days)
    var name by mutableStateOf(program.name)

    companion object {
        fun Factory(program: Program) = viewModelFactory { initializer { ProgramViewModel(program) } }
    }
}
