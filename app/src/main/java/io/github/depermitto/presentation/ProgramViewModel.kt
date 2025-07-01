package io.github.depermitto.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.depermitto.data.Program

class ProgramViewModel(program: Program = Program()) : ViewModel() {
    var days by mutableStateOf(program.days)
    var name by mutableStateOf(program.name)
}

class ProgramViewModelFactory(private val program: Program) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProgramViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return ProgramViewModel(program) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
