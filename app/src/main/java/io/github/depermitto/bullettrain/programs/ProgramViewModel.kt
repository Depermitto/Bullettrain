package io.github.depermitto.bullettrain.programs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.depermitto.bullettrain.protos.ExercisesProto.Exercise
import io.github.depermitto.bullettrain.protos.ProgramsProto.*

class ProgramViewModel(private val base: Program) : ViewModel() {
  private var programId = base.id
  var programName: String by mutableStateOf(base.name)
  private var workouts = mutableStateListOf<Workout>().apply { addAll(base.workoutsList) }
  private var nextDayIndex = base.nextDayIndex
  private var lastWorkoutTs = base.lastWorkoutTs

  fun getDays(): List<Workout> = workouts.toList()

  fun getDay(dayIndex: Int) = workouts[dayIndex]

  fun addDay(workout: Workout) = workouts.add(workout)

  fun addDay() = addDay(Workout.newBuilder().setName(generateUniqueDayName()).build())

  fun setDay(dayIndex: Int, workout: Workout) {
    workouts[dayIndex] = workout
  }

  fun removeDayAt(dayIndex: Int) = workouts.removeAt(dayIndex)

  fun setExercise(dayIndex: Int, exerciseIndex: Int, exercise: Exercise) =
    setDay(dayIndex, getDay(dayIndex).toBuilder().setExercises(exerciseIndex, exercise).build())

  fun getExercises(dayIndex: Int): List<Exercise> = getDay(dayIndex).exercisesList.toList()

  fun reorderDays(fromIndex: Int, toIndex: Int) =
    workouts.add(toIndex, workouts.removeAt(fromIndex))

  fun reorderExercises(dayIndex: Int, fromIndex: Int, toIndex: Int) =
    setDay(
      dayIndex,
      getDay(dayIndex)
        .toBuilder()
        .apply {
          val exercise = getExercises(fromIndex)
          removeExercises(fromIndex)
          addExercises(toIndex, exercise)
        }
        .build(),
    )

  fun getProgram(): Program =
    Program.newBuilder()
      .setId(programId)
      .setName(programName)
      .addAllWorkouts(getDays())
      .setNextDayIndex(nextDayIndex)
      .setLastWorkoutTs(lastWorkoutTs)
      .build()

  fun hasContent(ignoreDay1: Boolean = false): Boolean {
    if (programName.isNotBlank()) return true
    return if (!ignoreDay1) {
      val isDefault = workouts.toList() == listOf(Workout.getDefaultInstance())
      if (!isDefault) workouts.isNotEmpty() else false
    } else workouts.isNotEmpty()
  }

  fun revertToDefault() {
    val defaultProgram = Program.getDefaultInstance()
    programId = defaultProgram.id
    programName = defaultProgram.name
    workouts.clear()
    nextDayIndex = defaultProgram.nextDayIndex
    lastWorkoutTs = defaultProgram.lastWorkoutTs
  }

  fun generateUniqueDayName(): String {
    val maxDayNumber =
      if (getDays().isEmpty()) 0
      else
        getDays().maxOf { day ->
          if (day.name.startsWith("Day ")) day.name.drop(4).toIntOrNull() ?: 0 else 0
        }
    return "Day ${maxDayNumber + 1}"
  }

  companion object {
    fun Factory(program: Program) = viewModelFactory { initializer { ProgramViewModel(program) } }
  }
}
