package io.github.depermitto.bullettrain

import kotlinx.serialization.Serializable

sealed interface Destination {
    @Serializable
    data class Home(val tab: Tab) : Destination {
        enum class Tab(val icon: Int) {
            Exercises(R.drawable.database_search),
            History(R.drawable.history),
            Train(R.drawable.weight_lifter),
            Programs(R.drawable.notebook_multiple);

            fun next(): Tab = entries.getOrElse(this.ordinal + 1) { this }
            fun prev(): Tab = entries.getOrElse(this.ordinal - 1) { this }
        }
    }

    @Serializable
    data object ProgramCreation : Destination

    @Serializable
    data object Training : Destination

    @Serializable
    data object Settings : Destination

    @Serializable
    data class Day(val dayIndex: Int) : Destination

    @Serializable
    data class Program(val programId: Int) : Destination

    @Serializable
    data class Exercise(val exerciseId: Int) : Destination
}