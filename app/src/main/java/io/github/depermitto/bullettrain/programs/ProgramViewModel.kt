package io.github.depermitto.bullettrain.programs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.depermitto.bullettrain.database.entities.Program
import io.github.depermitto.bullettrain.database.entities.Workout
import io.github.depermitto.bullettrain.database.entities.WorkoutEntry
import io.github.depermitto.bullettrain.util.smallListSet

class ProgramViewModel(private val baseProgram: Program) : ViewModel() {
    var programId = baseProgram.id
        private set
    var programName by mutableStateOf(baseProgram.name)
    private var workouts = mutableStateListOf<Workout>().apply { addAll(baseProgram.workouts) }
    var nextDayIndex = baseProgram.nextDayIndex
        private set
    var followed by mutableStateOf(baseProgram.followed)
        private set
    var draft by mutableStateOf(baseProgram.draft)
        private set

    fun getDays(): List<Workout> = workouts.toList()
    fun getDay(dayIndex: Int) = workouts[dayIndex]
    fun addDay(workout: Workout) = workouts.add(workout)
    fun addDay() = addDay(Workout("Day ${workouts.size + 1}"))
    fun setDay(dayIndex: Int, workout: Workout) = workouts.set(dayIndex, workout)
    fun removeDayAt(dayIndex: Int) = workouts.removeAt(dayIndex)
    fun setExercise(dayIndex: Int, exerciseIndex: Int, workoutEntry: WorkoutEntry) = setDay(
        dayIndex, getDay(dayIndex).copy(entries = getDay(dayIndex).entries.smallListSet(exerciseIndex, workoutEntry))
    )

    fun reorderDays(fromIndex: Int, toIndex: Int) = workouts.add(toIndex, workouts.removeAt(fromIndex))

    val hasChanged get() = getDays() != baseProgram.workouts
    fun hasContent(ignoreDay1: Boolean = false): Boolean {
        if (programName.isNotBlank()) return true
        return if (!ignoreDay1) {
            val isDefault = workouts.toList() == listOf(Workout())
            if (!isDefault) workouts.isNotEmpty() else false
        } else workouts.isNotEmpty()
    }

    fun getProgram(): Program = Program(
        id = programId,
        name = programName,
        workouts = getDays(),
        followed = followed,
        draft = draft,
        nextDayIndex = nextDayIndex,
        mostRecentWorkoutDate = null
    )

    fun revertToDefault() {
        val defaultProgram = Program()
        programId = defaultProgram.id
        programName = defaultProgram.name
        workouts = defaultProgram.workouts.toMutableStateList()
        followed = defaultProgram.followed
        draft = defaultProgram.draft
        nextDayIndex = defaultProgram.nextDayIndex
    }

    companion object {
        fun Factory(program: Program) = viewModelFactory { initializer { ProgramViewModel(program) } }
    }
}
