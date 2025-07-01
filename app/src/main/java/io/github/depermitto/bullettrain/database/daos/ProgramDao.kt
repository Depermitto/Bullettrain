package io.github.depermitto.bullettrain.database.daos

import io.github.depermitto.bullettrain.database.entities.Program
import io.github.depermitto.bullettrain.util.smallListSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class ProgramDao(state: List<Program>) {
  internal val items = MutableStateFlow(state)
  private var newId = items.value.maxOfOrNull { it.id } ?: 0

  val getAll: StateFlow<List<Program>> = items.asStateFlow()
  val getUserPrograms =
    getAll.map { programs ->
      programs
        .filter { it correspondsNot Program.EmptyWorkout && !it.obsolete }
        .sortedByDescending { it.mostRecentWorkoutDate }
    }
  val getPerformable =
    getAll.map { programs ->
      programs.filterNot { it.obsolete }.sortedByDescending { it.mostRecentWorkoutDate }
    }

  /** @return Boolean indicating if the operation was successful. */
  fun update(item: Program): Boolean {
    val existingIndex = items.value.indexOfFirst { it.id == item.id }
    if (existingIndex == -1) return false

    items.update { state -> state.smallListSet(existingIndex, item) }
    return true
  }

  /** @return Id of the inserted item. */
  fun insert(item: Program): Int {
    items.update { state ->
      newId += 1
      state + item.copy(id = newId)
    }
    return newId
  }

  /** @return Id of the inserted item or -1 if it was updated. */
  fun upsert(item: Program): Int = if (update(item)) -1 else insert(item)

  fun delete(item: Program) = update(item.copy(obsolete = true))

  fun where(id: Int): Program = items.value.first { it.id == id }
}
