package io.github.depermitto.bullettrain.db

import io.github.depermitto.bullettrain.protos.SettingsProto.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet

class SettingsDao(settings: Settings) {
  internal val item = MutableStateFlow(settings)
  val get = item.asStateFlow()

  fun update(settings: Settings) = item.updateAndGet { settings }

  fun update(settings: Settings.Builder) = item.updateAndGet { settings.build() }
}
