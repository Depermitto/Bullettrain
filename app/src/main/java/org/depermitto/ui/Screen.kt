package org.depermitto.ui

sealed class Screen(val route: String) {
    data object PlansScreen : Screen("plans")
    data object CreatePlanScreen : Screen("plans/create_plan")
    data object ExercisesScreen : Screen("exercises")
    data object CreateExerciseScreen : Screen("exercises/create_exercise")
    data object SettingsScreen : Screen("settings")
}