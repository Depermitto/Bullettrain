package io.github.depermitto.bullettrain.train

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destination
import io.github.depermitto.bullettrain.database.BackgroundSlave
import io.github.depermitto.bullettrain.database.Day
import io.github.depermitto.bullettrain.database.Exercise
import io.github.depermitto.bullettrain.database.ExerciseSet
import io.github.depermitto.bullettrain.database.HistoryDao
import io.github.depermitto.bullettrain.database.HistoryRecord
import io.github.depermitto.bullettrain.database.Program
import io.github.depermitto.bullettrain.database.ProgramDao
import io.github.depermitto.bullettrain.util.smallListSet
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Timer
import kotlin.concurrent.timer
import kotlin.math.max

@Serializable
enum class WorkoutPhase { During, Completed, Editing }

class TrainViewModel(
    private val historyDao: HistoryDao,
    private val programDao: ProgramDao,
    private val navController: NavController,
) : ViewModel() {
    private lateinit var timer: Timer
    private var workoutState: HistoryRecord? = null
    private var exercises = mutableStateListOf<Exercise>()
    private var clock: Instant by mutableStateOf(Instant.ofEpochMilli(0))

    fun isWorkoutRunning(): Boolean = workoutState?.workoutPhase != WorkoutPhase.Completed
    fun isWorkoutEditing(): Boolean = workoutState?.workoutPhase == WorkoutPhase.Editing

    fun setExercise(index: Int, exercise: Exercise) {
        exercises[index] = exercise
        backup()
    }

    fun setExerciseSet(exerciseIndex: Int, setIndex: Int, set: ExerciseSet) = setExercise(
        exerciseIndex, getExercise(exerciseIndex).copy(sets = getExercise(exerciseIndex).sets.smallListSet(setIndex, set))
    )

    fun getExercise(index: Int) = exercises[index]
    fun getExercises() = exercises

    fun addExercise(exercise: Exercise) {
        exercises.add(exercise)
        backup()
    }

    fun removeExercise(index: Int) {
        exercises.removeAt(index)
        backup()
    }

    fun removeExerciseSet(exerciseIndex: Int, setIndex: Int) = setExercise(
        exerciseIndex,
        getExercise(exerciseIndex).copy(sets = getExercise(exerciseIndex).sets.filterIndexed { i, _ -> i != setIndex })
    )

    fun startWorkout(day: Day, program: Program, date: LocalDate = LocalDate.now()) {
        if (workoutState != null) return

        val record = HistoryRecord(
            workout = day,
            relatedProgram = program,
            date = date,
            workoutPhase = WorkoutPhase.During,
            workoutStartTs = Instant.now(),
        )
        createState(record)

        navController.navigate(Destination.Training) {
            popUpTo(0)
        }
    }

    fun editWorkout(record: HistoryRecord) {
        if (workoutState != null) return

        createState(record.copy(workoutPhase = WorkoutPhase.Editing))

        navController.navigate(Destination.Training) {
            popUpTo(0)
        }
    }

    suspend fun restoreWorkout(): Boolean {
        val session = historyDao.getUnfinishedBusiness()
        if (session != null && workoutState == null) {
            createState(session)
            return true
        }
        return session != null
    }

    fun completeWorkout() = endWorkout { state ->
        val record = state.copy(
            workoutPhase = WorkoutPhase.Completed, workout = state.workout.copy(exercises = exercises.toList())
        )
        val nextDay = (record.relatedProgram.nextDayIndex + 1) % record.relatedProgram.days.size

        historyDao.upsert(record)
        programDao.upsert(record.relatedProgram.copy(nextDayIndex = nextDay, mostRecentWorkoutDate = LocalDate.now()))
    }

    fun cancelWorkout() {
        if (isWorkoutEditing()) {
            endWorkout(Destination.Home) { state -> historyDao.update(state.copy(workoutPhase = WorkoutPhase.Completed)) }
        } else {
            endWorkout { state -> historyDao.delete(state) }
        }
    }

    private fun endWorkout(
        destination: Destination = Destination.Home, deinit: (HistoryRecord) -> Unit
    ) = workoutState?.let { state ->
        timer.cancel()

        deinit(state)

        workoutState = null
        exercises.clear()

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

        if (isWorkoutEditing()) return "Editing"

        val millis = instant?.toEpochMilli() ?: workoutState?.workoutStartTs?.toEpochMilli() ?: return ""
        val differenceMillis = max(0, clock.toEpochMilli() - millis)
        val hours = differenceMillis / (1000 * 60 * 60)

        return if (hours == 0L) {
            formatter.format(Instant.ofEpochMilli(differenceMillis))
        } else {
            "$hours:${hourFormatter.format(Instant.ofEpochMilli(differenceMillis))}"
        }
    }

    private fun backup() = BackgroundSlave.enqueue {
        workoutState = workoutState?.let { state ->
            state.copy(workout = state.workout.copy(exercises = exercises.toList())).also { historyDao.upsert(it) }
        }
    }

    private fun createState(historyRecord: HistoryRecord) {
        if (workoutState == null) {
            val newId = historyDao.upsert(historyRecord)
            val record = if (newId == -1) historyRecord else historyRecord.copy(id = newId)

            timer = timer(initialDelay = 1000L, period = 1000L) {
                clock = Instant.now()
            }
            exercises = record.workout.exercises.toMutableStateList()
            workoutState = record
            backup()
        } else throw UnsupportedOperationException("WorkoutState Is Not Null")
    }

    companion object {
        fun Factory(historyDao: HistoryDao, programDao: ProgramDao, navController: NavController) =
            viewModelFactory { initializer { TrainViewModel(historyDao, programDao, navController) } }
    }
}
