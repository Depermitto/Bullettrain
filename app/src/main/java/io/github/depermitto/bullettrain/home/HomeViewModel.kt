package io.github.depermitto.bullettrain.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.depermitto.bullettrain.database.HistoryRecord
import java.time.LocalDate

class HomeViewModel(startingBar: Screen.HomeScreen.Tabs) : ViewModel() {
    var activeBar by mutableStateOf(startingBar)
    var date by mutableStateOf(LocalDate.now())
    var selectedRecord by mutableStateOf<HistoryRecord?>(null)

    companion object {
        fun Factory(startingBar: Screen.HomeScreen.Tabs) = viewModelFactory { initializer { HomeViewModel(startingBar) } }
    }
}