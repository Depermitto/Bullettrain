package io.github.depermitto.bullettrain.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.LocalDate

class HomeViewModel() : ViewModel() {
  var calendarDate by mutableStateOf(LocalDate.now())
  var selectedDate by mutableStateOf<LocalDate?>(null)

  companion object {
    fun Factory() = viewModelFactory { initializer { HomeViewModel() } }
  }
}
