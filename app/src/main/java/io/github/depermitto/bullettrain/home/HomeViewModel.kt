package io.github.depermitto.bullettrain.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.depermitto.bullettrain.Destination.Home.Tab
import java.time.LocalDate

class HomeViewModel(startingTab: Tab) : ViewModel() {
    var activeTab by mutableStateOf(startingTab)
        private set
    var calendarDate by mutableStateOf(LocalDate.now())
    var selectedDate by mutableStateOf<LocalDate?>(null)

    fun switchTab(tab: Tab) {
        activeTab = tab
    }

    companion object {
        fun Factory(tab: Tab) = viewModelFactory { initializer { HomeViewModel(tab) } }
    }
}