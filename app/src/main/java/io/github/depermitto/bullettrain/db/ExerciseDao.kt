package io.github.depermitto.bullettrain.db

import io.github.depermitto.bullettrain.protos.ExercisesProto.Exercise
import io.github.depermitto.bullettrain.util.BKTree
import io.github.depermitto.bullettrain.util.bigListSet
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Abstraction representing a Data Access Object. Every method executes synchronously. */
class ExerciseDao(exerciseDescriptors: List<Exercise.Descriptor>) {
  internal val items = MutableStateFlow(exerciseDescriptors)
  private var newId = items.value.maxOfOrNull { it.id } ?: 0
  private val bkTree =
    BKTree("Press") // Most frequent word in our database, followed by "Dumbbell" and "Barbell"

  val getAll = items.asStateFlow()
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
  fun update(descriptor: Exercise.Descriptor): Boolean {
    if (descriptor.id >= items.value.size) return false

    items.update { state ->
      state.bigListSet(
        // Index lookup because the list has no gaps
        descriptor.id - 1,
        descriptor.toBuilder().setName(descriptor.name.capitalizeWords()).build(),
      )
    }
    return true
  }

  /** @return Id of the inserted descriptor. */
  fun insert(descriptor: Exercise.Descriptor): Int {
    items.update { state ->
      newId += 1
      state +
        descriptor
          .toBuilder()
          .setName(descriptor.name.capitalizeWords().also { name -> bkTree.insert(name) })
          .setId(newId)
          .build()
    }
    return newId
  }

  fun delete(descriptor: Exercise.Descriptor) =
    update(descriptor.toBuilder().setObsolete(true).build())

  fun where(id: Int): Exercise.Descriptor = items.value.first { it.id == id }

  /**
   * Filter out exercises by name. This function provides an autocorrect/typo correcting algorithm
   * that is controlled with the [errorTolerance] and [ignoreCase] parameters.
   */
  fun where(name: String, errorTolerance: Int = 0, ignoreCase: Boolean = false) =
    getSortedAlphabetically.map { exercises ->
      val words = name.trim().split(' ')
      val predictedWords = words.mapNotNull { bkTree.search(it, errorTolerance, ignoreCase) }

      exercises.filter { exercise ->
        val matches = words.all { word -> exercise.name.contains(word, ignoreCase) }
        // not checking for empty string will show all exercises
        val matchesPrediction =
          (predictedWords.isNotEmpty() &&
            predictedWords.all { word -> exercise.name.contains(word, ignoreCase) })
        matches || matchesPrediction
      }
    }

  /** Check [name] for duplicates and emptiness. */
  fun validateName(name: String): Result<Unit> {
    if (name.isBlank()) {
      return failure(Throwable(message = "Empty Exercise Name"))
    }

    if (getAll.value.any { !it.obsolete && it.name == name.capitalizeWords() }) {
      return failure(Throwable(message = "Duplicate Exercise Name"))
    }

    return success(Unit)
  }

  private fun String.capitalizeWords() =
    this.trim().split(' ').joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
}
