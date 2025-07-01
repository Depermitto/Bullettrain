package io.github.depermitto.bullettrain.database.entities

interface Entity {
    val id: Int
    fun clone(id: Int): Entity
}