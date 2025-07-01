package org.depermitto

/**
 * Returns a list containing all elements of the original collection with element at [index] set to [new].
 */
fun <T> List<T>.set(index: Int, new: T): List<T> {
    return slice(0 until index) + new + slice(index + 1 until size)
}