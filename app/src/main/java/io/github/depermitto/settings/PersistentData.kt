package io.github.depermitto.settings

import io.github.depermitto.data.GymDatabase
import java.io.File

class PersistentData(val db: GymDatabase, val dbFile: File, val fallbackBytes: ByteArray, val settingsFile: File) 