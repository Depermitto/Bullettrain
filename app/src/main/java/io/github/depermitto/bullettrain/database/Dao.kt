package io.github.depermitto.bullettrain.database

import android.util.Log
import io.github.depermitto.bullettrain.database.entities.Entity
import io.github.depermitto.bullettrain.util.bigListSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet

/**
 * Abstraction representing a Data Access Object. Every method executes synchronously.
 * @param depot instance of [Depot] governing data of interest.
 */
abstract class Dao<T : Entity>(protected val depot: Depot<List<T>>) {
    internal val items = MutableStateFlow(depot.retrieve())
    internal var newId = items.value.maxOfOrNull { it.id } ?: 0

    val getAll: StateFlow<List<T>> = items.asStateFlow()

    /**
     * @return Boolean indicating if the operation was successful.
     */
    open fun update(item: T): Boolean {
        val existingIndex = items.value.indexOfFirst { it.id == item.id }
        if (existingIndex == -1) return false

        val state = items.updateAndGet { state -> state.bigListSet(existingIndex, item) }
        depot.stash(state)
        Log.i("db-${depot.file.name}", state.toString())
        return true
    }

    /**
     * @return Id of the inserted item.
     */
    @Suppress("UNCHECKED_CAST")
    open fun insert(item: T): Int {
        val state = items.updateAndGet { state ->
            newId += 1
            state + item.clone(id = newId) as T
        }
        depot.stash(state)
        Log.i("db-${depot.file.name}", state.toString())
        return newId
    }

    /**
     * @return Id of the inserted item or -1 if it was updated.
     */
    open fun upsert(item: T): Int = if (update(item)) -1 else insert(item)

    open fun delete(item: T) {
        val state = items.updateAndGet { state -> state - item }
        depot.stash(state)
        Log.i("db-${depot.file.name}", state.toString())
    }

    open fun where(id: Int): T = items.value.first { it.id == id }
}