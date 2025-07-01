package io.github.depermitto.bullettrain.database.entities

import androidx.compose.runtime.Immutable
import io.github.depermitto.bullettrain.components.encodeToStringOutput
import io.github.depermitto.bullettrain.database.Compressor
import io.github.depermitto.bullettrain.database.Dao
import io.github.depermitto.bullettrain.database.Depot
import io.github.depermitto.bullettrain.database.serializers.InstantSerializer
import io.github.depermitto.bullettrain.util.BKTree
import java.io.File
import java.time.Instant
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Immutable
@Serializable
data class ExerciseDescriptor(
  @SerialName("exerciseId") override val id: Int = 0,
  val name: String,
  val obsolete: Boolean = false,
  val instructions: String = "",
) : Entity {
  override fun clone(id: Int) = copy(id = id)
}

@Immutable
@Serializable
data class WorkoutEntry(
  @SerialName("exerciseDescriptor") val descriptorId: Int,
  @SerialName("performanceVariableCategory")
  val perfVarCategory: PerfVarCategory = PerfVarCategory.Reps,
  val intensity: Intensity? = null,
  val sets: List<ExerciseSet> = listOf(),
  val superset: List<Int>? = null,
  val alternatives: List<Int>? = null,
  val notes: String = "",
) {
  val hasIntensity
    get() = intensity != null

  fun getPerformedSets(): List<ExerciseSet> = this.sets.filter { it.completed }

  fun lastPerformedSet() = this.sets.lastOrNull { it.completed }
}

@Immutable
@Serializable
data class ExerciseSet(
  @SerialName("targetPerformanceVariable") val targetPerfVar: PerfVar,
  @SerialName("actualPerformanceVariable") val actualPerfVar: Float = 0f,
  val targetIntensity: Intensity? = null,
  val actualIntensity: Float? = null,
  val weight: Float = 0f,
  @Serializable(with = InstantSerializer::class) val doneTs: Instant? = null,
) {
  val completed
    get() = doneTs != null && actualPerfVar != 0f
}

// TODO implement these as choices
@Immutable
@Serializable
enum class Intensity {
  RPE,
  RIR,
  PercentOf1RM,
}

@Immutable
@Serializable
enum class PerfVarCategory {
  Reps,
  RepRange,
  Time,
  TimeRange;

  val shortName
    get() =
      when (this) {
        Reps -> name
        RepRange -> Reps.name
        Time -> name
        TimeRange -> Time.name
      }
}

@Immutable
@Serializable
sealed class PerfVar(val category: PerfVarCategory) {
  @Immutable @Serializable data class Reps(val reps: Float = 0f) : PerfVar(PerfVarCategory.Reps)

  @Immutable @Serializable data class Time(val time: Float = 0f) : PerfVar(PerfVarCategory.Time)

  @Immutable
  @Serializable
  data class RepRange(val min: Float = 0f, val max: Float = 0f) : PerfVar(PerfVarCategory.RepRange)

  @Immutable
  @Serializable
  data class TimeRange(val min: Float = 0f, val max: Float = 0f) :
    PerfVar(PerfVarCategory.TimeRange)

  companion object {
    fun of(category: PerfVarCategory) =
      when (category) {
        PerfVarCategory.Reps -> Reps()
        PerfVarCategory.Time -> Time()
        PerfVarCategory.RepRange -> RepRange()
        PerfVarCategory.TimeRange -> TimeRange()
      }
  }

  fun encodeToStringOutput(): String =
    when (this) {
      is Reps ->
        if (this == Reps()) ""
        else reps.encodeToStringOutput() + if (reps == 1f) " rep" else " reps"
      is Time -> if (this == Time()) "" else time.encodeToStringOutput() + " min"
      is RepRange ->
        if (this == RepRange()) ""
        else "${min.encodeToStringOutput()}-${max.encodeToStringOutput()} reps"
      is TimeRange ->
        if (this == TimeRange()) ""
        else "${min.encodeToStringOutput()}-${max.encodeToStringOutput()} min"
    }
}

class ExerciseDao(file: ExerciseDepot) : Dao<ExerciseDescriptor>(file) {
  private val bkTree =
    BKTree("Press") // This is the most frequent word in our database, followed by "Dumbbell" and
  // "Barbell"

  val getSortedAlphabetically = getAll.map { it.filterNot { it.obsolete }.sortedBy { it.name } }

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

  override fun insert(item: ExerciseDescriptor): Int {
    // TODO the also block probably doesn't work since there is no state
    return super.insert(
      item.copy(name = item.name.prep().also { preppedName -> bkTree.insert(preppedName) })
    )
  }

  override fun update(item: ExerciseDescriptor): Boolean {
    return super.update(item.copy(name = item.name.prep()))
  }

  override fun upsert(item: ExerciseDescriptor): Int {
    return super.upsert(item.copy(name = item.name.prep()))
  }

  override fun delete(item: ExerciseDescriptor) {
    super.update(item.copy(obsolete = true))
  }

  /**
   * Check [name] for duplicates and emptiness.
   *
   * @return Error message if [name] is bad and null if successfully validated.
   */
  fun validateName(name: String): String? {
    val name = name.prep()
    if (name.isBlank()) {
      return "Empty Exercise Name"
    }

    if (getAll.value.any { !it.obsolete && it.name == name }) {
      return "Duplicate Exercise Name"
    }

    return null
  }

  private fun String.prep() =
    this.trim().split(' ').joinToString(" ") { it.replaceFirstChar { it.uppercaseChar() } }
}

class ExerciseDepot(file: File) : Depot<List<ExerciseDescriptor>>(file) {
  override fun retrieve(): List<ExerciseDescriptor> =
    Json.decodeFromString(Compressor.uncompress(file.readText()))

  override fun stash(obj: List<ExerciseDescriptor>) =
    file.writeText(Compressor.compress(Json.encodeToString(obj)))
}
