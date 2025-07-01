package io.github.depermitto.bullettrain.util

fun String.splitOnUppercase(): String =
  split(regex = Regex("(?=[A-Z])"))
    .filter { it.isNotEmpty() }
    .joinToString(" ") { it.lowercase() }
    .replaceFirstChar { it.uppercaseChar() }

fun String.capwords() =
  this.trim().split(' ').joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
