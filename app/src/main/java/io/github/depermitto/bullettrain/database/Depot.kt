package io.github.depermitto.bullettrain.database

import java.io.File

/** Abstraction over a [java.io.File] with custom [retrieve] (read) and [stash] (write) methods. */
abstract class Depot<T>(val file: File) {
  abstract fun retrieve(): T

  abstract fun stash(obj: T)
}
