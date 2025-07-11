package io.github.depermitto.bullettrain.db

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.depermitto.bullettrain.protos.ProgramsProto.*
import io.github.depermitto.bullettrain.util.smallListSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class ProgramDao(programs: List<Program>) {
  internal val items = MutableStateFlow(programs)
  private var idTrack = items.value.maxOfOrNull { it.id } ?: 0

  val getUserPrograms =
    items.map { programs ->
      programs
        .filterNot { it.id == -1 || it.obsolete }
        .sortedByDescending { it.lastWorkoutTs.seconds }
    }
  val getPerformable =
    items.map { programs ->
      programs.filterNot { it.obsolete }.sortedByDescending { it.lastWorkoutTs.seconds }
    }

  /** @return Boolean indicating if the operation was successful. */
  fun update(program: Program): Boolean {
    // Naive iteration because the list is small
    val index = items.value.indexOfFirst { it.id == program.id }
    if (index == -1) return false

    items.update { state -> state.smallListSet(index, program) }
    return true
  }

  /** @return Id of the inserted program. */
  fun insert(program: Program): Int {
    items.update { state ->
      idTrack += 1
      state + program.toBuilder().setId(idTrack).build()
    }
    return idTrack
  }

  /** @return Id of the inserted program or -1 if it was updated. */
  fun upsert(program: Program): Int = if (update(program)) -1 else insert(program)

  fun delete(program: Program) = update(program.toBuilder().setObsolete(true).build())

  @SuppressLint("StateFlowValueCalledInComposition")
  @Composable
  fun whereAsState(id: Int): State<Program> =
    items
      .map { descriptors -> descriptors.first { it.id == id } }
      .collectAsStateWithLifecycle(items.value.first { it.id == id })

  fun where(id: Int): Program = items.value.first { it.id == id }
}
