package io.github.depermitto.util

fun <T> List<T>.set(index: Int, value: T) = List(size) { if (it == index) value else this[it] }
