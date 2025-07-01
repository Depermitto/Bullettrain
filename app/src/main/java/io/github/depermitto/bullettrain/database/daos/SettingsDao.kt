package io.github.depermitto.bullettrain.database.daos

import android.util.Log
import io.github.depermitto.bullettrain.database.entities.Settings
import io.github.depermitto.bullettrain.util.loadAndUncompressData
import io.github.depermitto.bullettrain.util.saveAndCompressData
import java.nio.file.Path
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet

class SettingsDao(private val filepath: Path) {
  internal val item = MutableStateFlow<Settings>(loadAndUncompressData(filepath))
  val getSettings = item.asStateFlow()

  fun update(function: (Settings) -> Settings) {
    val state = item.updateAndGet { state -> function(state) }
    saveAndCompressData(filepath, state)
    Log.i("db-${filepath}", state.toString())
  }
}
