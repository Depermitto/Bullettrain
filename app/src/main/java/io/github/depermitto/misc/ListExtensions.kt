package io.github.depermitto.misc

operator fun <T> List<T>.set(index: Int, value: T): List<T> {
    return slice(0 until index) + value + slice(index + 1 until size)
}
