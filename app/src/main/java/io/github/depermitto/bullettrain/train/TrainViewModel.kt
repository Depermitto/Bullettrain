package io.github.depermitto.bullettrain.train

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import io.github.depermitto.bullettrain.Destinations
import io.github.depermitto.bullettrain.database.BackgroundSlave
import io.github.depermitto.bullettrain.database.Day
import io.github.depermitto.bullettrain.database.Exercise
import io.github.depermitto.bullettrain.database.ExerciseSet
import io.github.depermitto.bullettrain.database.HistoryDao
import io.github.depermitto.bullettrain.database.HistoryRecord
import io.github.depermitto.bullettrain.database.Program
import io.github.depermitto.bullettrain.database.ProgramDao
import io.github.depermitto.bullettrain.util.smallListSet
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.timer
import kotlin.math.max

@Serializable
enum class WorkoutPhase { During, Completed }

data class WorkoutState(
    val historyRecord: HistoryRecord,
    val timer: Timer,
)

class TrainViewModel(
    private val historyDao: HistoryDao,
    private val programDao: ProgramDao,
    private val navController: NavController,
) : ViewModel() {
    private var workoutState: WorkoutState? = null
    private var exercises = mutableStateListOf<Exercise>()
    private var clock: Instant by mutableStateOf(Instant.ofEpochMilli(0))

    fun isWorkoutRunning(): Boolean = workoutState?.historyRecord?.workoutPhase == WorkoutPhase.During

    fun setExercise(index: Int, exercise: Exercise) = exercises.set(index, exercise)
    fun setExerciseSet(exerciseIndex: Int, setIndex: Int, set: ExerciseSet) = setExercise(
        exerciseIndex, getExercise(exerciseIndex).copy(sets = getExercise(exerciseIndex).sets.smallListSet(setIndex, set))
    )

    fun getExercise(index: Int) = exercises[index]
    fun getExercises() = exercises
    fun addExercise(exercise: Exercise) = exercises.add(exercise)
    fun removeExercise(index: Int) = exercises.removeAt(index)
    fun removeExerciseSet(exerciseIndex: Int, setIndex: Int) = setExercise(
        exerciseIndex,
        getExercise(exerciseIndex).copy(sets = getExercise(exerciseIndex).sets.filterIndexed { i, _ -> i != setIndex })
    )

    fun startWorkout(day: Day, program: Program) {
        if (workoutState != null) return

        val record = HistoryRecord(
            workout = day,
            relatedProgram = program,
            workoutPhase = WorkoutPhase.During,
            date = Instant.now(),
            workoutStartTime = Instant.now(),
        )
        createState(record)

        navController.navigate(Destinations.Training) {
            popUpTo(Destinations.Home(Destinations.Home.Tabs.Train)) { inclusive = true }
            launchSingleTop = true
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
        val record = state.historyRecord.copy(
            workoutPhase = WorkoutPhase.Completed, workout = state.historyRecord.workout.copy(exercises = exercises.toList())
        )
        val program = runBlocking { programDao.where(record.relatedProgram.id).firstOrNull() } ?: return@endWorkout
        val nextDay = (program.nextDay + 1) % program.days.size

        historyDao.upsert(record)
        programDao.upsert(
            program.copy(
                nextDay = nextDay,
                weekStreak = program.weekStreak + if (nextDay == 0) 1 else 0,
                mostRecentWorkoutDate = Instant.now()
            )
        )
    }

    fun cancelWorkout() = endWorkout { historyDao.delete(it.historyRecord) }

    private fun endWorkout(deinit: (WorkoutState) -> Unit) = workoutState?.let { state ->
        state.timer.cancel()

        deinit(state)

        workoutState = null
        exercises.clear()

        if (!navController.popBackStack(Destinations.Home(tab = Destinations.Home.Tabs.Train), false)) {
            navController.navigate(route = Destinations.Home(tab = Destinations.Home.Tabs.Train)) {
                popUpTo(Destinations.Training) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    fun elapsedSince(instant: Instant? = null): String {
        val formatter = DateTimeFormatter.ofPattern("m:ss").withZone(ZoneId.systemDefault())
        val hourFormatter = DateTimeFormatter.ofPattern("mm:ss").withZone(ZoneId.systemDefault())

        val millis = instant?.toEpochMilli() ?: workoutState?.historyRecord?.workoutStartTime?.toEpochMilli() ?: return ""
        val differenceMillis = max(0, clock.toEpochMilli() - millis)
        val hours = differenceMillis / (1000 * 60 * 60)

        return if (hours == 0L) {
            formatter.format(Instant.ofEpochMilli(differenceMillis))
        } else {
            "$hours:${hourFormatter.format(Instant.ofEpochMilli(differenceMillis))}"
        }
    }

    private fun createState(historyRecord: HistoryRecord) {
        if (workoutState == null) {
            val newId = historyDao.upsert(historyRecord)
            val record = if (newId == -1) historyRecord else historyRecord.copy(id = newId)

            clock = Instant.now()
            exercises = historyRecord.workout.exercises.toMutableStateList()
            workoutState = WorkoutState(historyRecord = record, timer = timer(initialDelay = 1000L, period = 1000L) {
                clock = Instant.now()
                BackgroundSlave.enqueue {
                    workoutState = workoutState?.let {
                        it.copy(historyRecord = it.historyRecord.copy(workout = it.historyRecord.workout.copy(exercises = exercises.toList())))
                    }

                    historyDao.upsert(workoutState?.historyRecord ?: return@enqueue)
                }
            })
        } else throw UnsupportedOperationException("WorkoutState Is Not Null")
    }

    companion object {
        fun Factory(historyDao: HistoryDao, programDao: ProgramDao, navController: NavController) =
            viewModelFactory { initializer { TrainViewModel(historyDao, programDao, navController) } }
    }
}
