package io.github.depermitto.bullettrain.database.daos

import io.github.depermitto.bullettrain.database.entities.ExerciseDescriptor
import io.github.depermitto.bullettrain.util.BKTree
import io.github.depermitto.bullettrain.util.bigListSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Abstraction representing a Data Access Object. Every method executes synchronously. */
class ExerciseDao(state: List<ExerciseDescriptor>) {
  internal val items = MutableStateFlow(state)
  private var newId = items.value.maxOfOrNull { it.id } ?: 0
  private val bkTree =
    BKTree("Press") // Most frequent word in our database, followed by "Dumbbell" and "Barbell"

  val getAll: StateFlow<List<ExerciseDescriptor>> = items.asStateFlow()
  val getSortedAlphabetically =
    getAll.map { exerciseDescriptors ->
      exerciseDescriptors.filterNot { it.obsolete }.sortedBy { it.name }
    }

  init {
    // fill BKTree with words from ExerciseDescriptors
    getAll.value.forEach { exercise ->
      exercise.name
        .trim()
        .split(' ')
        .filter { word -> word.all { char -> char.isLetter() } }
        .forEach(bkTree::insert)
    }
  }

  /** @return Boolean indicating if the operation was successful. */
  fun update(item: ExerciseDescriptor): Boolean {
    val existingIndex = items.value.indexOfFirst { it.id == item.id }
    if (existingIndex == -1) return false

    items.update { state ->
      state.bigListSet(existingIndex, item.copy(name = item.name.capitalizeWords()))
    }
    return true
  }

  /** @return Id of the inserted item. */
  fun insert(item: ExerciseDescriptor): Int {
    items.update { state ->
      newId += 1
      state +
        item.copy(
          name = item.name.capitalizeWords().also { preppedName -> bkTree.insert(preppedName) },
          id = newId,
        )
    }
    return newId
  }

  fun delete(item: ExerciseDescriptor) = update(item.copy(obsolete = true))

  fun where(id: Int): ExerciseDescriptor = items.value.first { it.id == id }

  /**
   * Filter out exercises by name. This function provides an autocorrect/typo correcting algorithm
   * that is controlled with the [errorTolerance] and [ignoreCase] parameters.
   */
  fun where(name: String, errorTolerance: Int = 0, ignoreCase: Boolean = false) =
    getSortedAlphabetically.map { exercises ->
      val words = name.trim().split(' ')
      val predictedWords = words.mapNotNull { bkTree.search(it, errorTolerance, ignoreCase) }

      exercises.filter { exercise ->
        words.all {
          exercise.name.contains(it, ignoreCase)
        } || // not checking for empty string will show all exercises
          (predictedWords.isNotEmpty() &&
            predictedWords.all { exercise.name.contains(it, ignoreCase) })
      }
    }

  /**
   * Check [name] for duplicates and emptiness.
   *
   * @return Error message if [name] is bad and null if successfully validated.
   */
  fun validateName(name: String): String? {
    if (name.isBlank()) {
      return "Empty Exercise Name"
    }

    if (getAll.value.any { !it.obsolete && it.name == name.capitalizeWords() }) {
      return "Duplicate Exercise Name"
    }

    return null
  }

  private fun String.capitalizeWords() =
    this.trim().split(' ').joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
}
