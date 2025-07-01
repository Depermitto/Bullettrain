package org.depermitto.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import org.depermitto.database.ProgramDao

@Composable
fun PlansScreen(programDao: ProgramDao, navController: NavController) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        val programs by programDao.getAllFlow().collectAsState(emptyList())

        LazyColumn(modifier = Modifier.weight(1.0f)) {
            items(programs) { program ->
                Text(text = program.name)
            }
        }

        Button(onClick = { navController.navigate(Screen.PlansCreationScreen.route) }) {
            Text(text = "Create Program")
        }
    }
}