package org.depermitto

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
import io.github.vinceglb.filekit.core.FileKit
import org.depermitto.data.GymDatabase
import org.depermitto.ui.theme.GymAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FileKit.init(this)

        setContent {
            GymAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val dbFile = application.getDatabasePath(DB_FILENAME)
                    App(
                        dbFile = dbFile,
                        fallbackBytes = resources.openRawResource(R.raw.firetent).readBytes(),
                        db = Room.databaseBuilder<GymDatabase>(context = applicationContext, name = dbFile.absolutePath)
                            .openHelperFactory(FrameworkSQLiteOpenHelperFactory())
                            .fallbackToDestructiveMigration(true)
                            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                            .build(),
                    )
                }
            }
        }
    }
}