package io.github.depermitto.bullettrain

import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination {
  @Serializable
  data object Home : Destination {
    enum class Tab(val icon: Int) {
      Exercises(R.drawable.database_search),
      History(R.drawable.history),
      Train(R.drawable.weight_lifter),
      Programs(R.drawable.notebook_multiple),
    }
  }

  @Serializable data object ProgramCreation : Destination

  @Serializable data object Training : Destination

  @Serializable data object Settings : Destination

  @Serializable data class Day(val dayIndex: Int) : Destination

  @Serializable data class Program(val programId: Int) : Destination

  @Serializable data class Exercise(val descriptorId: Int) : Destination

  @Serializable data class Workout(val recordId: Int) : Destination

  @Serializable data class HistoryRecord(val recordId: Int) : Destination
}
