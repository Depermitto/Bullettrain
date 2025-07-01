package io.github.depermitto.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.depermitto.data.Day
import io.github.depermitto.data.Exercise
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import kotlin.math.min

@RequiresApi(Build.VERSION_CODES.O)
class TrainViewModel(day: Day) : ViewModel() {
    var name by mutableStateOf(day.name)
    var sets = mutableStateListOf<List<Exercise>>()
    var now by mutableStateOf(Instant.now())
        private set

    init {
        viewModelScope.launch {
            while (true) {
                now = now.plusSeconds(1)
                delay(1000)
            }
        }
    }

    fun instantSince(instant: Instant): Instant = now.minusMillis(min(instant.toEpochMilli(), now.toEpochMilli()))
}

class TrainViewModelFactory(private val day: Day) : ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return TrainViewModel(day) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
