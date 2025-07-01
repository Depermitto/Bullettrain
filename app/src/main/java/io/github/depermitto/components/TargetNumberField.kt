package io.github.depermitto.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.depermitto.data.ExerciseTarget

@Composable
fun TargetNumberField(
    modifier: Modifier = Modifier,
    value: ExerciseTarget,
    onValueChange: (ExerciseTarget) -> Unit,
    readOnly: Boolean = false,
) = when (value) {
    is ExerciseTarget.Reps -> NumberField(
        modifier,
        value = value.reps,
        onValueChange = { onValueChange(value.copy(it)) },
        readOnly = readOnly
    )

    is ExerciseTarget.Time -> Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        NumberField(
            modifier = modifier,
            value = value.time,
            onValueChange = { onValueChange(value.copy(it)) },
            readOnly = readOnly
        )
        Text(text = "min")
    }

    is ExerciseTarget.RepRange -> Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        NumberField(
            modifier = modifier,
            value = value.min,
            onValueChange = { onValueChange(value.copy(min = it)) },
            readOnly = readOnly
        )
        Text(text = "-")
        NumberField(
            modifier = modifier,
            value = value.max,
            onValueChange = { onValueChange(value.copy(max = it)) },
            readOnly = readOnly
        )
    }
}