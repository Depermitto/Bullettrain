package io.github.depermitto.bullettrain.database.daos

import io.github.depermitto.bullettrain.database.entities.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsDao(state: Settings) {
  internal val item = MutableStateFlow(state)
  val getSettings = item.asStateFlow()

  fun update(function: (Settings) -> Settings) {
    item.update { state -> function(state) }
  }
}
