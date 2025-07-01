package io.github.depermitto.bullettrain

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
sealed interface Destination {
  @Immutable
  @Serializable
  data object Home : Destination {
    enum class Tab(val icon: Int) {
      Exercises(R.drawable.database_search),
      History(R.drawable.history),
      Train(R.drawable.weight_lifter),
      Programs(R.drawable.notebook_multiple);

      fun next(): Tab = entries.getOrElse(this.ordinal + 1) { this }

      fun prev(): Tab = entries.getOrElse(this.ordinal - 1) { this }
    }
  }

  @Immutable @Serializable data object ProgramCreation : Destination

  @Immutable @Serializable data object Training : Destination

  @Immutable @Serializable data object Settings : Destination

  @Immutable @Serializable data class Day(val dayIndex: Int) : Destination

  @Immutable @Serializable data class Program(val programId: Int) : Destination

  @Immutable @Serializable data class Exercise(val exerciseId: Int) : Destination
}
