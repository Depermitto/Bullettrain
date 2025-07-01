package io.github.depermitto.bullettrain.train

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destination
import io.github.depermitto.bullettrain.database.daos.HistoryDao
import io.github.depermitto.bullettrain.database.daos.ProgramDao
import io.github.depermitto.bullettrain.database.entities.*
import io.github.depermitto.bullettrain.util.smallListSet
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Timer
import kotlin.concurrent.timer
import kotlin.math.max
import kotlinx.serialization.Serializable

@Immutable
@Serializable
enum class WorkoutPhase {
  During,
  Completed,
  Editing,
}

class TrainViewModel(
  private val historyDao: HistoryDao,
  private val programDao: ProgramDao,
  private val navController: NavController,
) : ViewModel() {
  private lateinit var timer: Timer
  private var workoutState: HistoryRecord? = null
  private var workoutEntries = mutableStateListOf<WorkoutEntry>()
  private var clock: Instant by mutableStateOf(Instant.ofEpochMilli(0))

  fun isWorkoutRunning(): Boolean = workoutState?.workoutPhase != WorkoutPhase.Completed

  fun isWorkoutEditing(): Boolean = workoutState?.workoutPhase == WorkoutPhase.Editing

  fun setWorkoutEntry(index: Int, workoutEntry: WorkoutEntry) {
    workoutEntries[index] = workoutEntry
    backup()
  }

  fun setExerciseSet(exerciseIndex: Int, setIndex: Int, set: ExerciseSet) =
    workoutEntries.set(
      exerciseIndex,
      getWorkoutEntry(exerciseIndex)
        .copy(sets = getWorkoutEntry(exerciseIndex).sets.smallListSet(setIndex, set)),
    )

  fun getWorkoutEntry(index: Int) = workoutEntries[index]

  fun getWorkoutEntries() = workoutEntries

  fun addWorkoutEntry(workoutEntry: WorkoutEntry) {
    workoutEntries.add(workoutEntry)
    backup()
  }

  fun removeWorkoutEntryAt(index: Int) {
    workoutEntries.removeAt(index)
    backup()
  }

  fun toggleCompletion(checked: Boolean, exerciseIndex: Int, setIndex: Int) =
    workoutState?.let { state ->
      val lastPerformedSet = getWorkoutEntry(exerciseIndex).lastPerformedSet()
      var set = getWorkoutEntry(exerciseIndex).sets[setIndex]
      set =
        if (checked)
          set.copy(
            doneTs = state.date.atTimeNow(),
            weight = if (set.weight != 0f) set.weight else lastPerformedSet?.weight ?: 0f,
            actualPerfVar =
              if (set.actualPerfVar != 0f) set.actualPerfVar
              else lastPerformedSet?.actualPerfVar ?: 0f,
          )
        else set.copy(doneTs = null)

      setExerciseSet(exerciseIndex, setIndex, set)
      backup()
    }

  fun startWorkout(workout: Workout, programId: Int, date: LocalDate = LocalDate.now()) {
    if (workoutState != null) return

    val record =
      HistoryRecord(
        workout = workout,
        relatedProgramId = programId,
        date = date,
        workoutPhase = WorkoutPhase.During,
        workoutStartTs = date.atTimeNow(),
      )
    createState(record)

    navController.navigate(Destination.Training) { popUpTo(0) }
  }

  fun editWorkout(record: HistoryRecord) {
    if (workoutState != null) return

    createState(record.copy(workoutPhase = WorkoutPhase.Editing))

    navController.navigate(Destination.Training) { popUpTo(0) }
  }

  fun restoreWorkout(): Boolean {
    val session = historyDao.getUnfinishedBusiness()
    if (session != null && workoutState == null) {
      createState(session)
      return true
    }
    return session != null
  }

  fun completeWorkout() = endWorkout { state ->
    val record =
      state.copy(
        workoutPhase = WorkoutPhase.Completed,
        workout = state.workout.copy(entries = workoutEntries.toList()),
      )
    val relatedProgram = programDao.where(record.relatedProgramId)
    val nextDayIndex =
      (relatedProgram.workouts.indexOf(record.workout) + 1) % relatedProgram.workouts.size

    historyDao.upsert(record)
    programDao.upsert(
      relatedProgram.copy(nextDayIndex = nextDayIndex, mostRecentWorkoutDate = record.date)
    )
  }

  fun cancelWorkout() = endWorkout { state ->
    if (isWorkoutEditing()) {
      historyDao.update(state.copy(workoutPhase = WorkoutPhase.Completed))
    } else {
      historyDao.delete(state)
    }
  }

  private fun endWorkout(
    destination: Destination = Destination.Home,
    deinit: (HistoryRecord) -> Unit,
  ) =
    workoutState?.let { state ->
      timer.cancel()

      deinit(state)

      workoutState = null
      workoutEntries.clear()

      if (!navController.popBackStack(destination, false)) {
        navController.navigate(route = destination) {
          popUpTo(Destination.Training) { inclusive = true }
          launchSingleTop = true
        }
      }
    }

  fun elapsedSince(instant: Instant? = null): String {
    val formatter = DateTimeFormatter.ofPattern("m:ss").withZone(ZoneId.systemDefault())
    val hourFormatter = DateTimeFormatter.ofPattern("mm:ss").withZone(ZoneId.systemDefault())

    val millis =
      instant?.toEpochMilli() ?: workoutState?.workoutStartTs?.toEpochMilli() ?: return ""
    val differenceMillis = max(0, clock.toEpochMilli() - millis)
    val hours = differenceMillis / (1000 * 60 * 60)

    return if (hours == 0L) {
      formatter.format(Instant.ofEpochMilli(differenceMillis))
    } else {
      "$hours:${hourFormatter.format(Instant.ofEpochMilli(differenceMillis))}"
    }
  }

  private fun backup() {
    workoutState =
      workoutState?.let { state ->
        state.copy(workout = state.workout.copy(entries = workoutEntries.toList())).also {
          historyDao.upsert(it)
        }
      }
  }

  private fun createState(historyRecord: HistoryRecord) {
    if (workoutState == null) {
      val record =
        when (val newId = historyDao.upsert(historyRecord)) {
          // updated -> no change, same id
          -1 -> historyRecord
          // inserted -> we need new id representing the record
          else -> historyRecord.copy(id = newId)
        }

      timer = timer(initialDelay = 1000L, period = 1000L) { clock = record.date.atTimeNow() }
      workoutEntries = record.workout.entries.toMutableStateList()
      workoutState = record
    } else throw UnsupportedOperationException("WorkoutState Is Not Null")
  }

  companion object {
    fun Factory(historyDao: HistoryDao, programDao: ProgramDao, navController: NavController) =
      viewModelFactory {
        initializer { TrainViewModel(historyDao, programDao, navController) }
      }
  }
}

fun LocalDate.atTimeNow(): Instant =
  this.atTime(OffsetTime.ofInstant(Instant.now(), ZoneId.systemDefault())).toInstant()
