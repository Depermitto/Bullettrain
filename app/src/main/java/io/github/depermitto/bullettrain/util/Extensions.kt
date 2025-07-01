package io.github.depermitto.bullettrain.util

import io.github.depermitto.bullettrain.database.Compressor
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Copy the list with item at [index] set to [value]. Preferred for lists smaller than ~50 elements.
 */
fun <T> List<T>.smallListSet(index: Int, value: T) =
  List(size) { if (it == index) value else this[it] }

/**
 * Copy the list with item at [index] set to [value]. Generally performs better than [smallListSet]
 * for highly repeated calls and big or unknown sized lists.
 */
fun <T> List<T>.bigListSet(index: Int, value: T): List<T> =
  this.toMutableList().apply { set(index, value) }

fun <T> List<T>.reorder(fromIndex: Int, toIndex: Int): List<T> =
  this.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }

fun String.splitOnUppercase(): String =
  split(regex = Regex("(?=[A-Z])"))
    .filter { it.isNotEmpty() }
    .joinToString(" ") { it.lowercase() }
    .replaceFirstChar { it.uppercaseChar() }

inline fun <reified T> loadAndUncompressData(filepath: Path): T =
  Json.decodeFromString<T>(Compressor.uncompress(filepath.readText()))

inline fun <reified T> saveAndCompressData(filepath: Path, data: T) =
  filepath.writeText(Compressor.compress(Json.encodeToString(data)))
