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

  fun addDay() = addDay(Workout.newBuilder().setName("Day ${workouts.size + 1}").build())

  fun setDay(dayIndex: Int, workout: Workout) = workouts.set(dayIndex, workout)

  fun removeDayAt(dayIndex: Int) = workouts.removeAt(dayIndex)

  fun setExercise(dayIndex: Int, exerciseIndex: Int, exercise: Exercise) =
    setDay(dayIndex, getDay(dayIndex).toBuilder().setExercises(exerciseIndex, exercise).build())

  fun setExercise(dayIndex: Int, exerciseIndex: Int, exercise: Exercise.Builder) =
    setDay(dayIndex, getDay(dayIndex).toBuilder().setExercises(exerciseIndex, exercise).build())

  fun reorderDays(fromIndex: Int, toIndex: Int) =
    workouts.add(toIndex, workouts.removeAt(fromIndex))

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

  companion object {
    fun Factory(program: Program) = viewModelFactory { initializer { ProgramViewModel(program) } }
  }
}
