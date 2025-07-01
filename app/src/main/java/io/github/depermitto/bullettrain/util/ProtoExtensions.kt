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
import java.time.ZoneId

fun Exercise.getLastCompletedSet(): Exercise.Set? = setsList.lastOrNull { set -> set.hasDoneTs() }

fun UnitSystem.weightUnit() = if (this == UnitSystem.Metric) "kg" else "lbs"

@Composable
fun Theme.isDarkMode(): Boolean =
  (this == Theme.Dark) || (this == Theme.FollowSystem && isSystemInDarkTheme())

fun HistoryRecord.getDate(): LocalDate = this.workoutStartTs.toLocalDate()

fun Timestamp.toLocalDate(): LocalDate =
  Instant.ofEpochSecond(this.seconds, this.nanos.toLong())
    .atZone(ZoneId.systemDefault())
    .toLocalDate()

fun Instant.toTimestamp(): Timestamp =
  Timestamp.newBuilder().setSeconds(this.epochSecond).setNanos(this.nano).build()

fun LocalDate.atTimeNow(): Instant = this.atTime(OffsetTime.now()).toInstant()
