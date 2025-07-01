package io.github.depermitto.bullettrain

sealed class Screen(val route: String) {
    data object HomeScreen : Screen("home/{tab}") {
        enum class Tabs(val icon: Int) {
            History(R.drawable.history), Train(R.drawable.weight_lifter), Programs(R.drawable.notebook_multiple)
        }

        fun passTab(tab: Tabs): String {
            return this.route.replace(oldValue = "{tab}", newValue = tab.name)
        }
    }

    data object ProgramCreationScreen : Screen("programs/creation")

    data object TrainingScreen : Screen("training")

    data object SettingsScreen : Screen("settings")
}