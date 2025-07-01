package io.github.depermitto

sealed class Screen(val route: String) {
    data object MainScreen : Screen("main/{tab}") {
        enum class Tabs(val icon: Int) {
            History(R.drawable.history),
            Train(R.drawable.weight_lifter),
            Programs(R.drawable.calendar_month_outline)
        }

        fun passTab(tab: Tabs): String {
            return this.route.replace(oldValue = "{tab}", newValue = tab.name)
        }
    }

    data object ProgramCreationScreen : Screen("programs/creation")
    data object ProgramScreen : Screen("programs/overview/{programId}") {
        fun passId(id: Int): String {
            return this.route.replace(oldValue = "{programId}", newValue = id.toString())
        }
    }

    data object TrainingScreen : Screen("training")

    data object SettingsScreen : Screen("settings")
}