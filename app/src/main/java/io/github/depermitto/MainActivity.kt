package io.github.depermitto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import io.github.depermitto.data.GymDatabase
import io.github.depermitto.settings.DB_FILENAME
import io.github.depermitto.settings.SETTINGS_FILENAME
import io.github.depermitto.settings.Settings
import io.github.depermitto.theme.GymAppTheme
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FileKit.init(this)

        setContent {
            GymAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val dbFile = application.getDatabasePath(DB_FILENAME)

                    val settingsFile = File(application.filesDir, SETTINGS_FILENAME)
                    if (!settingsFile.exists()) {
                        application.openFileOutput(SETTINGS_FILENAME, MODE_PRIVATE).run {
                            write(Json.encodeToString(Settings()).encodeToByteArray())
                        }
                    }

                    App(
                        dbFile = dbFile,
                        fallbackBytes = resources.openRawResource(R.raw.firetent).readBytes(),
                        db = Room.databaseBuilder<GymDatabase>(context = applicationContext, name = dbFile.absolutePath)
                            .openHelperFactory(FrameworkSQLiteOpenHelperFactory()).fallbackToDestructiveMigration(true)
                            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE).build(),
                        settingsFile = application.getFileStreamPath(SETTINGS_FILENAME),
                    )
                }
            }
        }
    }
}