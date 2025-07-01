package io.github.depermitto.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.depermitto.DB_FILENAME
import io.github.depermitto.data.GymDatabase
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.pickFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SettingsScreen(
    db: GymDatabase,
    dbFile: File,
    fallbackBytes: ByteArray,
) = Box(modifier = Modifier.fillMaxSize()) {
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val context = LocalContext.current
    var toast by remember { mutableStateOf("") }

    LaunchedEffect(toast) {
        if (toast.isNotEmpty()) {
            Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = {
            scope.launch {
                val pickedFile = FileKit.pickFile(type = PickerType.File())
                if (pickedFile != null) {
                    db.checkpoint()
                    dbFile.writeBytes(pickedFile.readBytes())
                    toast = "Successfully Imported \"${pickedFile.name}\""
                }
            }
        }) {
            Text(text = "Import")
        }

        Button(onClick = {
            scope.launch {
                db.checkpoint()
                val pickedFile = FileKit.saveFile(
                    bytes = dbFile.readBytes(),
                    baseName = DB_FILENAME.substringBeforeLast('.'),
                    extension = DB_FILENAME.substringAfterLast('.')
                )
                if (pickedFile != null) {
                    toast = "Successfully Saved To \"${pickedFile.name}\""
                }
            }
        }) {
            Text(text = "Export")
        }
    }

    Button(modifier = Modifier.align(Alignment.BottomCenter), onClick = {
        scope.launch {
            db.checkpoint()
            dbFile.writeBytes(fallbackBytes)
            toast = "Factory Reset Complete"
        }
    }) {
        Text(text = "Factory Reset")
    }
}
