package org.depermitto.ui

sealed class Screen(val route: String) {
    data object HistoryScreen : Screen("history")

    data object TrainScreen : Screen("train")

    data object PlansScreen : Screen("plans")
    data object PlansCreationScreen : Screen("plans/creation")

    data object ExercisesScreen : Screen("exercises")
    data object ExercisesCreationScreen : Screen("exercises/creation")

    data object SettingsScreen : Screen("settings")
}