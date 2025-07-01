package io.github.depermitto.bullettrain.database

interface Entity {
    val id: Int
    fun clone(id: Int): Entity
}