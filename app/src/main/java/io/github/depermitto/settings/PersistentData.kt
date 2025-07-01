package io.github.depermitto.settings

import io.github.depermitto.data.GymDatabase
import java.io.File

// NOTE maybe move settings from json to database?
class PersistentData(val db: GymDatabase, val dbFile: File, val fallbackBytes: ByteArray, val settingsFile: File) 