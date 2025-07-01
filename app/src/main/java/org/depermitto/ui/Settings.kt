package org.depermitto.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.pickFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.depermitto.DB_FILENAME
import org.depermitto.database.GymDatabase
import java.io.File

@Composable
fun Settings(
    db: GymDatabase,
    dbFile: File,
    fallbackBytes: ByteArray,
    scope: CoroutineScope,
    navController: NavController,
) = Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
    Column(modifier = Modifier.weight(1.0f)) {
        Button(onClick = {
            scope.launch {
                db.checkpoint()
                dbFile.writeBytes(fallbackBytes)
            }
        }) {
            Text(text = "revert to default")
        }

        Button(onClick = {
            scope.launch {
                val dbBytes: ByteArray? = FileKit.pickFile(type = PickerType.File())?.readBytes()
                if (dbBytes != null && db.checkpoint() == 0) {
                    dbFile.writeBytes(dbBytes)
                }
            }
        }) {
            Text(text = "Import")
        }

        Button(onClick = {
            scope.launch {
                db.checkpoint()
                FileKit.saveFile(
                    bytes = dbFile.readBytes(),
                    baseName = DB_FILENAME.substringBeforeLast('.'),
                    extension = DB_FILENAME.substringAfterLast('.')
                )
            }
        }) {
            Text(text = "Export")
        }
    }

    Button(onClick = { navController.navigate(Screen.ExercisesScreen.route) }) {
        Text("Goto exercises")
    }
}
