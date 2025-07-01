package io.github.depermitto.bullettrain.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.depermitto.bullettrain.Destinations.Home.Tabs
import io.github.depermitto.bullettrain.database.HistoryRecord
import java.time.LocalDate

class HomeViewModel(startingTab: Tabs) : ViewModel() {
    var activeBar by mutableStateOf(startingTab)
        private set
    var date by mutableStateOf(LocalDate.now())
    var selectedRecord by mutableStateOf<HistoryRecord?>(null)

    fun switchTab(tab: Tabs) {
        activeBar = tab
    }

    companion object {
        fun Factory(tab: Tabs) = viewModelFactory { initializer { HomeViewModel(tab) } }
    }
}