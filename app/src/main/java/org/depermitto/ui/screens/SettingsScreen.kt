package org.depermitto.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.pickFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.depermitto.DB_FILENAME
import org.depermitto.database.GymDatabase
import java.io.File

@Composable
fun SettingsScreen(
    db: GymDatabase,
    dbFile: File,
    fallbackBytes: ByteArray,
    scope: CoroutineScope,
) = Box(
    modifier = Modifier.fillMaxSize(),
) {
    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = {
            scope.launch(Dispatchers.IO) {
                val dbBytes: ByteArray? = FileKit.pickFile(type = PickerType.File())?.readBytes()
                if (dbBytes != null) {
                    db.checkpoint()
                    dbFile.writeBytes(dbBytes)
                }
            }
        }) {
            Text(text = "Import")
        }

        Button(onClick = {
            scope.launch(Dispatchers.IO) {
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
    
    Button(modifier = Modifier.align(Alignment.BottomCenter), onClick = {
        scope.launch(Dispatchers.IO) {
            db.checkpoint()
            dbFile.writeBytes(fallbackBytes)
        }
    }) {
        Text(text = "Revert To Default")
    }
}

        
