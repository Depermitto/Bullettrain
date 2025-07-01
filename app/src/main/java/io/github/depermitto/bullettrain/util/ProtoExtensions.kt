package io.github.depermitto.bullettrain.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.google.protobuf.Timestamp
import io.github.depermitto.bullettrain.protos.ExercisesProto.Exercise
import io.github.depermitto.bullettrain.protos.HistoryProto.HistoryRecord
import io.github.depermitto.bullettrain.protos.ProgramsProto.*
import io.github.depermitto.bullettrain.protos.SettingsProto.Theme
import io.github.depermitto.bullettrain.protos.SettingsProto.UnitSystem
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetTime
import java.time.YearMonth
import java.time.ZoneId

/** Compute last set which has [Exercise.Set.doneTs_] initialized and return it if exists. */
val Exercise.lastCompletedSet: Exercise.Set?
  get() = setsList.lastOrNull { set -> set.hasDoneTs() }

fun UnitSystem.weightUnit() = if (this == UnitSystem.Metric) "kg" else "lbs"

@Composable
fun Theme.isDarkMode(): Boolean =
  (this == Theme.Dark) || (this == Theme.FollowSystem && isSystemInDarkTheme())

/** Extract [LocalDate] from [HistoryRecord.workoutStartTs_]. */
val HistoryRecord.date: LocalDate
  get() = this.workoutStartTs.toLocalDate()

/** Extract [YearMonth] from [HistoryRecord.workoutStartTs_]. */
val HistoryRecord.yearMonth: YearMonth
  get() = YearMonth.from(this.workoutStartTs.toLocalDate())

/** Convert [com.google.protobuf.Timestamp] to [java.time.LocalDate]. */
fun Timestamp.toLocalDate(): LocalDate =
  Instant.ofEpochSecond(this.seconds, this.nanos.toLong())
    .atZone(ZoneId.systemDefault())
    .toLocalDate()

/** Convert [java.time.Instant] to [com.google.protobuf.Timestamp]. */
fun Instant.toTimestamp(): Timestamp =
  Timestamp.newBuilder().setSeconds(this.epochSecond).setNanos(this.nano).build()

/** Convert this [LocalDate] to [Instant] at the same time as right now. */
fun LocalDate.atTimeNow(): Instant = this.atTime(OffsetTime.now()).toInstant()
