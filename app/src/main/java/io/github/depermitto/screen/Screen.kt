package io.github.depermitto.screen

import io.github.depermitto.R

sealed class Screen(val route: String) {
    data object MainScreen : Screen("main") {
        enum class Tabs(val icon: Int) {
            History(R.drawable.history),
            Train(R.drawable.weight_lifter),
            Programs(R.drawable.calendar_month_outline)
        }
    }

    data object ProgramCreationScreen : Screen("programs/creation")
    data object ProgramScreen : Screen("programs/overview/{programId}") {
        fun passId(id: Long): String {
            return this.route.replace(oldValue = "{programId}", newValue = id.toString())
        }
    }

    data object SettingsScreen : Screen("settings")
}