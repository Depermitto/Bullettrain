package io.github.depermitto.bullettrain.database

import io.github.depermitto.bullettrain.util.bigListSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet

abstract class Dao<T : Entity>(protected val storageFile: StorageFile<List<T>>) {
    internal val items = MutableStateFlow(storageFile.read())
    internal var newId = items.value.maxOfOrNull { it.id } ?: 0

    val getAll: StateFlow<List<T>> = items.asStateFlow()

    /**
     * @return Boolean indicating if the operation was successful.
     */
    open fun update(item: T): Boolean {
        val existingIndex = items.value.indexOfFirst { it.id == item.id }
        if (existingIndex == -1) return false

        val state = items.updateAndGet { state -> state.bigListSet(existingIndex, item) }
        BackgroundSlave.enqueue { storageFile.writeLog(state) }
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
        BackgroundSlave.enqueue { storageFile.writeLog(state) }
        return newId
    }

    /**
     * @return Id of the inserted item or -1 if it was updated.
     */
    open fun upsert(item: T): Int = if (update(item)) -1 else insert(item)

    open fun delete(item: T) {
        val state = items.updateAndGet { state -> state - item }
        BackgroundSlave.enqueue { storageFile.writeLog(state) }
    }

    open fun where(id: Int): T = items.value.first { it.id == id }
}