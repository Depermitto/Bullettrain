package io.github.depermitto.bullettrain

import kotlinx.serialization.Serializable

sealed interface Destinations {
    @Serializable
    data class Home(val tab: Tabs) : Destinations {
        enum class Tabs(val icon: Int) {
            History(R.drawable.history), Train(R.drawable.weight_lifter), Programs(R.drawable.notebook_multiple)
        }
    }

    @Serializable
    data object ProgramCreation : Destinations

    @Serializable
    data object Training : Destinations

    @Serializable
    data object Settings : Destinations

    @Serializable
    data class Day(val dayIndex: Int) : Destinations

    @Serializable
    data class Program(val program: io.github.depermitto.bullettrain.database.Program) : Destinations
}