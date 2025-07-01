package io.github.depermitto.bullettrain.train

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.google.protobuf.Timestamp
import io.github.depermitto.bullettrain.Destination
import io.github.depermitto.bullettrain.db.HistoryDao
import io.github.depermitto.bullettrain.db.ProgramDao
import io.github.depermitto.bullettrain.protos.ExercisesProto.Exercise
import io.github.depermitto.bullettrain.protos.HistoryProto.*
import io.github.depermitto.bullettrain.protos.ProgramsProto.*
import io.github.depermitto.bullettrain.protos.ProgramsProto.Workout.Phase
import io.github.depermitto.bullettrain.util.DateFormatters
import io.github.depermitto.bullettrain.util.atTimeNow
import io.github.depermitto.bullettrain.util.date
import io.github.depermitto.bullettrain.util.lastCompletedSet
import io.github.depermitto.bullettrain.util.toTimestamp
import java.time.Instant
import java.time.LocalDate
import java.util.Timer
import kotlin.concurrent.timer
import kotlin.math.max
import kotlin.properties.Delegates

/**
 * [ViewModel] for creating and editing a [Workout]. When the class is initialized, some of its
 * fields are not. To properly work and not crash the app, you must initialize [TrainViewModel]'s
 * state with [startWorkout], [restoreWorkout] or [editWorkout]. These methods use hardcoded
 * [Destination]s for navigation. Use [completeWorkout] or [cancelWorkout] to shutdown.
 *
 * [TrainViewModel] is for:
 * - Realtime training on any [LocalDate],
 * - Editing an existing [Workout].
 *
 * Both variants need a [HistoryDao] to store its data as a [HistoryRecord] and a [ProgramDao] to
 * fetch the related [Program].
 */
class TrainViewModel(private val historyDao: HistoryDao, private val programDao: ProgramDao) :
  ViewModel() {
  // HistoryRecord-related
  private var id by Delegates.notNull<Int>()
  private var relatedProgramId: Int? = null
  private lateinit var workoutStartTs: Timestamp
  private lateinit var workoutPhase: Phase

  // Workout-related
  private lateinit var workoutName: String
  private lateinit var targetWorkout: Workout
  private var exercises = mutableStateListOf<Exercise>()

  // Realtime-related
  private var timer = Timer() // ticks every second
  private var clock by mutableLongStateOf(0) // seconds since workout started
  private lateinit var date: LocalDate

  fun isWorkoutRunning(): Boolean = workoutPhase == Phase.During

  fun setExercise(index: Int, exercise: Exercise) {
    exercises[index] = exercise
    if (isWorkoutRunning()) historyDao.update(this.getRecord())
  }

  fun setExerciseSet(exerciseIndex: Int, setIndex: Int, set: Exercise.Set.Builder) =
    setExercise(exerciseIndex, exercises[exerciseIndex].toBuilder().setSets(setIndex, set).build())

  fun getExercises() = exercises.toList()

  fun addExercise(descriptor: Exercise.Descriptor) {
    val targetExercise =
      targetWorkout.exercisesList.firstOrNull { it.descriptorId == descriptor.id }
    if (targetExercise != null) exercises.add(targetExercise)
    else
      exercises.add(
        Exercise.newBuilder()
          .setDescriptorId(descriptor.id)
          .addSets(Exercise.Set.getDefaultInstance())
          .build()
      )
    if (isWorkoutRunning()) historyDao.update(this.getRecord())
  }

  fun addExerciseSet(exerciseIndex: Int) {
    val exercise = exercises[exerciseIndex]
    val targetExercise =
      targetWorkout.exercisesList.firstOrNull { it.descriptorId == exercise.descriptorId }
    if (
      targetExercise != null &&
        exercise.setsCount < targetExercise.setsCount &&
        exercise.setsList.zip(targetExercise.setsList).all { it.first.target == it.second.target }
    )
      setExercise(
        exerciseIndex,
        exercise
          .toBuilder()
          .apply {
            for (setIndex in setsCount..<targetExercise.setsCount) {
              addSets(targetExercise.getSets(setIndex))
            }
          }
          .build(),
      )
    else
      setExercise(
        exerciseIndex,
        exercise.toBuilder().addSets(Exercise.Set.getDefaultInstance()).build(),
      )
  }

  fun reorderExercises(fromIndex: Int, toIndex: Int) {
    exercises.add(toIndex, exercises.removeAt(fromIndex))
    if (isWorkoutRunning()) historyDao.update(this.getRecord())
  }

  fun removeExerciseAt(index: Int) {
    exercises.removeAt(index)
    if (isWorkoutRunning()) historyDao.update(this.getRecord())
  }

  fun getWorkoutName(): String = workoutName

  fun getRecord(): HistoryRecord =
    HistoryRecord.newBuilder()
      .setId(id)
      .setWorkoutStartTs(workoutStartTs)
      .setWorkoutPhase(workoutPhase)
      .setWorkout(Workout.newBuilder().setName(workoutName).addAllExercises(exercises.toList()))
      .apply { this@TrainViewModel.relatedProgramId?.let { setRelatedProgramId(it) } }
      .build()

  private fun init(historyRecord: HistoryRecord) {
    val record =
      when (val newId = historyDao.upsert(historyRecord)) {
        // updated -> no change, same id
        -1 -> historyRecord
        // inserted -> we need new id representing the record
        else -> historyRecord.toBuilder().setId(newId).build()
      }

    id = record.id
    if (record.hasRelatedProgramId()) relatedProgramId = record.relatedProgramId
    workoutStartTs = record.workoutStartTs
    workoutPhase = record.workoutPhase

    if (isWorkoutRunning())
      timer = timer(initialDelay = 1000L, period = 1000L) { clock = date.atTimeNow().epochSecond }
    date = record.date

    targetWorkout = record.workout
    workoutName = record.workout.name
    exercises = record.workout.exercisesList.toMutableStateList()
  }

  fun elapsed(since: Timestamp? = null): String {
    val secs = since?.seconds ?: workoutStartTs.seconds
    val diff = max(0, clock - secs)
    val hours = diff / 3600
    return if (hours == 0L) {
      DateFormatters.m_ss.format(Instant.ofEpochSecond(diff))
    } else if (hours >= 10) {
      "--:--:--"
    } else {
      "$hours:${DateFormatters.mm_ss.format(Instant.ofEpochSecond(diff))}"
    }
  }

  fun toggleCompletion(checked: Boolean, exerciseIndex: Int, setIndex: Int) {
    val lastPerformedSet = exercises[exerciseIndex].lastCompletedSet
    val set = exercises[exerciseIndex].getSets(setIndex)
    val builder =
      if (checked) {
        set
          .toBuilder()
          .setDoneTs(date.atTimeNow().toTimestamp())
          .setWeight(if (set.weight != 0F) set.weight else lastPerformedSet?.weight ?: 0F)
          .setActual(if (set.actual != 0F) set.actual else lastPerformedSet?.actual ?: 0F)
      } else {
        set.toBuilder().clearDoneTs()
      }

    setExerciseSet(exerciseIndex, setIndex, builder)
  }

  fun startWorkout(
    workout: Workout,
    programId: Int?,
    workoutStart: Instant,
    navController: NavController,
  ) {
    val record =
      HistoryRecord.newBuilder()
        .setWorkout(workout)
        .setWorkoutStartTs(workoutStart.toTimestamp())
        .setWorkoutPhase(Phase.During)
        .apply { programId?.let { setRelatedProgramId(it) } }
        .build()
    init(record)
    navController.navigate(Destination.Training)
  }

  fun restoreWorkout(): Boolean {
    val session = historyDao.getUnfinishedBusiness()
    if (session != null) {
      init(session)
      return true
    }
    return false
  }

  fun editWorkout(recordId: Int, navController: NavController) {
    init(historyDao.where(recordId))
    navController.navigate(Destination.HistoryRecord(recordId))
  }

  private fun endWorkout(navController: NavController, deinit: () -> Unit) {
    timer.cancel()
    deinit()
    exercises.clear()
    if (!navController.popBackStack(Destination.Home, false)) {
      navController.navigate(route = Destination.Home) {
        popUpTo(Destination.Training) { inclusive = true }
        launchSingleTop = true
      }
    }
  }

  fun completeWorkout(navController: NavController) =
    endWorkout(navController) {
      historyDao.upsert(getRecord().toBuilder().setWorkoutPhase(Phase.Completed).build())

      val relatedProgram = programDao.where(relatedProgramId ?: return@endWorkout)
      val nextDayIndex =
        (relatedProgram.workoutsList.indexOfFirst { workoutName == it.name } + 1) %
          relatedProgram.workoutsCount
      programDao.upsert(
        relatedProgram
          .toBuilder()
          .setNextDayIndex(nextDayIndex)
          .setLastWorkoutTs(date.atTimeNow().toTimestamp())
          .build()
      )
    }

  fun cancelWorkout(navController: NavController) =
    endWorkout(navController) { historyDao.delete(id) }

  companion object {
    fun Factory(historyDao: HistoryDao, programDao: ProgramDao) = viewModelFactory {
      initializer { TrainViewModel(historyDao, programDao) }
    }
  }
}
