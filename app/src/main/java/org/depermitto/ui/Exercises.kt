package org.depermitto.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.depermitto.database.ExerciseDao

@Composable
fun Exercises(exerciseDao: ExerciseDao, scope: CoroutineScope, navController: NavController) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        val exercises by exerciseDao.getAllFlow().collectAsState(emptyList())
        LazyColumn(modifier = Modifier.weight(1.0f)) {
            items(exercises) { exercise ->
                Text(text = "${exercise.exerciseId}      ${exercise.name}", modifier = Modifier.clickable {
                    scope.launch {
                        exerciseDao.delete(exercise)
                    }
                })
            }
        }

        Button(onClick = { navController.navigate(Screen.CreateExerciseScreen.route) }) {
            Text("Create Exercise")
        }

        Button(onClick = { navController.navigate(Screen.SettingsScreen.route) }) {
            Text("Goto settings")
        }
    }
}
