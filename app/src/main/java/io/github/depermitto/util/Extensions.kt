package io.github.depermitto.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

fun <T> List<T>.set(index: Int, value: T) = List(size) { if (it == index) value else this[it] }

fun <T> Flow<T>.blockingFirstOrNull() = runBlocking { this@blockingFirstOrNull.firstOrNull() }
fun <T> Flow<List<T>>.blockingFirstOrEmpty() = runBlocking { this@blockingFirstOrEmpty.firstOrNull() ?: emptyList() }