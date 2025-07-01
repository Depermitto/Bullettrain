package io.github.depermitto.bullettrain.database

import android.util.Log
import java.io.File
import java.io.IOException

abstract class StorageFile<Object>(val file: File) {
    abstract fun read(): Object
    abstract fun writeNoLog(obj: Object)

    fun writeLog(obj: Object) {
        try {
            this.writeNoLog(obj)
            Log.i("db-${file.name}", "backed up '$obj'")
        } catch (err: IOException) {
            Log.wtf("db-${file.name}", err.toString())
        }
    }
}