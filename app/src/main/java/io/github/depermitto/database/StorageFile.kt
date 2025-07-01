package io.github.depermitto.database

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

abstract class StorageFile<Object>(val file: File) {
    abstract fun read(): Object
    abstract fun write(obj: Object)
}

class SettingsFile(file: File) : StorageFile<Settings>(file) {
    override fun read(): Settings = Json.decodeFromString(Compressor.uncompress(file.readText()))
    override fun write(obj: Settings) = file.writeText(Compressor.compress(Json.encodeToString(obj)))
}

class HistoryFile(file: File) : StorageFile<List<HistoryRecord>>(file) {
    override fun read(): List<HistoryRecord> = Json.decodeFromString(Compressor.uncompress(file.readText()))
    override fun write(obj: List<HistoryRecord>) = file.writeText(Compressor.compress(Json.encodeToString(obj)))
}

class ProgramsFile(file: File) : StorageFile<List<Program>>(file) {
    override fun read(): List<Program> = Json.decodeFromString(Compressor.uncompress(file.readText()))
    override fun write(obj: List<Program>) = file.writeText(Compressor.compress(Json.encodeToString(obj)))
}

class ExerciseFile(file: File) : StorageFile<List<Exercise>>(file) {
    override fun read(): List<Exercise> = Json.decodeFromString(Compressor.uncompress(file.readText()))
    override fun write(obj: List<Exercise>) = file.writeText(Compressor.compress(Json.encodeToString(obj)))
}
