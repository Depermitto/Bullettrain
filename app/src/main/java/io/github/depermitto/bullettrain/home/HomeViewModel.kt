package io.github.depermitto.bullettrain.home

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.depermitto.bullettrain.Destination.Home.Tab
import io.github.depermitto.bullettrain.db.HistoryDao
import io.github.depermitto.bullettrain.util.getDate
import java.time.LocalDate

class HomeViewModel(initialPage: Tab, private val historyDao: HistoryDao) : ViewModel() {
  val screenPager = PagerState(initialPage.ordinal) { Tab.entries.size }

  var calendarDate: LocalDate by mutableStateOf(LocalDate.now())
  var selectedDate by mutableStateOf<LocalDate?>(null)

  fun resetDate() {
    if (historyDao.idTrack == 0) {
      calendarDate = LocalDate.now()
      selectedDate = null
    } else {
      val mostRecentDate = historyDao.where(historyDao.idTrack).getDate()
      calendarDate = mostRecentDate
      selectedDate = mostRecentDate
    }
  }

  companion object {
    fun Factory(initialPage: Tab, historyDao: HistoryDao) = viewModelFactory {
      initializer { HomeViewModel(initialPage, historyDao) }
    }
  }
}
