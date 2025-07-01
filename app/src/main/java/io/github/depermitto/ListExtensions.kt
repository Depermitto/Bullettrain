package io.github.depermitto

/**
 * Returns a list containing all elements of the original collection with element at [index] set to [new].
 */
fun <T> List<T>.replaceAt(index: Int, new: T): List<T> = slice(0 until index) + new + slice(index + 1 until size)
