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
import io.github.depermitto.bullettrain.util.date
import io.github.depermitto.bullettrain.util.yearMonth
import java.time.LocalDate
import java.time.YearMonth

class HomeViewModel(initialPage: Tab, private val historyDao: HistoryDao) : ViewModel() {
  val screenPager = PagerState(initialPage.ordinal) { Tab.entries.size }

  var calendarPage: YearMonth by mutableStateOf(YearMonth.now())
  var selectedDate by mutableStateOf<LocalDate?>(null)

  fun resetDate() {
    calendarPage = YearMonth.now()
    selectedDate = historyDao.mostRecent(calendarPage)
  }

  fun mostRecentWorkout() {
    val record = historyDao.mostRecent()
    if (record == null) {
      resetDate()
      return
    }
    calendarPage = record.yearMonth
    selectedDate = record.date
  }

  companion object {
    fun Factory(initialPage: Tab, historyDao: HistoryDao) = viewModelFactory {
      initializer { HomeViewModel(initialPage, historyDao) }
    }
  }
}
