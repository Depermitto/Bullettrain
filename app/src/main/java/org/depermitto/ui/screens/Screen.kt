package org.depermitto.ui.screens

import org.depermitto.R

sealed class Screen(val route: String) {
    data object MainScreen : Screen("main") {
        enum class Tabs(val icon: Int) {
            History(R.drawable.history),
            Train(R.drawable.weight_lifter),
            Programs(R.drawable.calendar_month_outline)
        }
    }

    data object ProgramsCreationScreen : Screen("programs/creation")

    data object ExercisesScreen : Screen("exercises")
    data object ExercisesCreationScreen : Screen("exercises/creation")

    data object SettingsScreen : Screen("settings")
}