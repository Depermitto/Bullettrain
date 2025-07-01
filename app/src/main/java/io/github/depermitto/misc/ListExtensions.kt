package io.github.depermitto.misc

fun <T> List<T>.set(index: Int, value: T) = List(size) { if (it == index) value else this[it] }
//    return slice(0 until index) + value + slice(index + 1 until size)