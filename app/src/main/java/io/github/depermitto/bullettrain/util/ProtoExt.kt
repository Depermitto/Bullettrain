package io.github.depermitto.bullettrain.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.google.protobuf.Timestamp
import io.github.depermitto.bullettrain.protos.ExercisesProto.Exercise
import io.github.depermitto.bullettrain.protos.HistoryProto.HistoryRecord
import io.github.depermitto.bullettrain.protos.SettingsProto.Theme
import io.github.depermitto.bullettrain.protos.SettingsProto.UnitSystem
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Computes the last set that has [Exercise.Set.doneTs_] initialized and returns it if it exists.
 */
val Exercise.lastCompletedSet: Exercise.Set?
  get() = setsList.lastOrNull { set -> set.hasDoneTs() }

fun UnitSystem.weightUnit() = if (this == UnitSystem.Metric) "kg" else "lbs"

@Composable
fun Theme.isDarkMode(): Boolean =
  (this == Theme.Dark) || (this == Theme.FollowSystem && isSystemInDarkTheme())

/** Extracts [LocalDate] from [HistoryRecord.workoutStartTs_]. */
val HistoryRecord.date: LocalDate
  get() = this.workoutStartTs.toLocalDate()

/** Extracts [YearMonth] from [HistoryRecord.workoutStartTs_]. */
val HistoryRecord.yearMonth: YearMonth
  get() = YearMonth.from(this.workoutStartTs.toLocalDate())

/** Converts [com.google.protobuf.Timestamp] to [java.time.Instant]. */
fun Timestamp.toInstant(): Instant = Instant.ofEpochSecond(this.seconds, this.nanos.toLong())

/** Converts [com.google.protobuf.Timestamp] to [java.time.LocalDate]. */
fun Timestamp.toZonedDateTime(): ZonedDateTime = this.toInstant().atZone(ZoneId.systemDefault())

/** Converts [com.google.protobuf.Timestamp] to [java.time.LocalDate]. */
fun Timestamp.toLocalDate(): LocalDate = this.toZonedDateTime().toLocalDate()

/** Converts [java.time.Instant] to [com.google.protobuf.Timestamp]. */
fun Instant.toTimestamp(): Timestamp =
  Timestamp.newBuilder().setSeconds(this.epochSecond).setNanos(this.nano).build()

/** Converts this [LocalDate] to [Instant] at the same time as right now. */
fun LocalDate.atTimeNow(): Instant = this.atTime(OffsetTime.now()).toInstant()
