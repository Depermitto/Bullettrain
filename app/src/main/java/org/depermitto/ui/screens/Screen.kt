package org.depermitto.ui.screens

sealed class Screen(val route: String) {
    data object MainScreen : Screen("main") {
        enum class Tabs {
            History, Train, Programs
        }
    }

    data object ProgramsCreationScreen : Screen("programs/creation")

    data object ExercisesScreen : Screen("exercises")
    data object ExercisesCreationScreen : Screen("exercises/creation")

    data object SettingsScreen : Screen("settings")
}