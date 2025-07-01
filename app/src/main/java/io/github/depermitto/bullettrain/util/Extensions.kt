package io.github.depermitto.bullettrain.util

/**
 * Copy the list with item at [index] set to [value]. Preferred for lists smaller than ~50 elements.
 */
fun <T> List<T>.smallListSet(index: Int, value: T) = List(size) { if (it == index) value else this[it] }

/**
 * Copy the list with item at [index] set to [value]. Generally performs better than [smallListSet]
 * for highly repeated calls and big or unknown sized lists.
 */
fun <T> List<T>.bigListSet(index: Int, value: T): List<T> = this.toMutableList().apply { set(index, value) }
fun <T> List<T>.reorder(fromIndex: Int, toIndex: Int): List<T> = this.toMutableList().apply {
    add(toIndex, removeAt(fromIndex))
}