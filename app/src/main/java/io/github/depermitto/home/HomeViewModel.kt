package io.github.depermitto.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

class HomeViewModel(startingBar: Screen.HomeScreen.Tabs) : ViewModel() {
    var activeBar by mutableStateOf(startingBar)

    companion object {
        fun Factory(startingBar: Screen.HomeScreen.Tabs) = viewModelFactory { initializer { HomeViewModel(startingBar) } }
    }
}