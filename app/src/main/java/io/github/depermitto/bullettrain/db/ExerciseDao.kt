package io.github.depermitto.bullettrain.db

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.depermitto.bullettrain.protos.ExercisesProto.Exercise
import io.github.depermitto.bullettrain.util.BKTree
import io.github.depermitto.bullettrain.util.bigListSet
import io.github.depermitto.bullettrain.util.capwords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Abstraction representing a Data Access Object. Every method executes synchronously. */
class ExerciseDao(descriptors: List<Exercise.Descriptor>) {
  internal val items = MutableStateFlow(descriptors)
  private var idTrack = items.value.maxOfOrNull { it.id } ?: 0

  private val bkTree =
    BKTree("Press") // Most frequent word in our database, followed by "Dumbbell" and "Barbell"

  val getVisible = items.map { it.filterNot { d -> d.obsolete } }

  /**
   * Filter out exercises by name. This function provides an autocorrect/typo correcting algorithm
   * that is controlled with the [errorTolerance] and [ignoreCase] parameters.
   */
  fun getByName(name: String, errorTolerance: Int = 0, ignoreCase: Boolean = false) =
    getVisible.map { descriptors ->
      val words = name.trim().split(' ')
      val predictedWords = words.map { bkTree.search(it, errorTolerance, ignoreCase) }

      descriptors
        .filter { d ->
          val matches = words.all { word -> d.name.contains(word, ignoreCase) }
          // not checking for empty string will show all exercises
          val matchesPrediction =
            predictedWords.all { word -> word != null && d.name.contains(word, ignoreCase) }
          matches || matchesPrediction
        }
        .sortedBy { d -> d.name }
    }

  init {
    // fill BKTree with words from ExerciseDescriptors
    items.value.forEach { d ->
      d.name
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
        descriptor,
      )
    }
    return true
  }

  /** @return Id of the inserted descriptor. */
  fun insert(descriptor: Exercise.Descriptor): Int {
    items.update { state ->
      idTrack += 1
      state +
        descriptor
          .toBuilder()
          .setName(descriptor.name.capwords().also { name -> bkTree.insert(name) })
          .setId(idTrack)
          .build()
    }
    return idTrack
  }

  fun delete(descriptor: Exercise.Descriptor) =
    update(descriptor.toBuilder().setObsolete(true).build())

  @SuppressLint("StateFlowValueCalledInComposition")
  @Composable
  fun whereAsState(id: Int): State<Exercise.Descriptor> =
    items
      .map { descriptors -> descriptors[id - 1] }
      .collectAsStateWithLifecycle(items.value[id - 1])
}
