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
        value = value.reps.toFloat(),
        onValueChange = { onValueChange(value.copy(it.toInt())) },
        readOnly = readOnly
    )

    is ExerciseTarget.Time -> Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        NumberField(
            modifier = modifier,
            value = value.time.toFloat(),
            onValueChange = { onValueChange(value.copy(it.toLong())) },
            readOnly = readOnly
        )
        Text(text = "min")
    }

    is ExerciseTarget.RepRange -> Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        NumberField(
            modifier = modifier,
            value = value.min.toFloat(),
            onValueChange = { onValueChange(value.copy(min = it.toInt())) },
            readOnly = readOnly
        )
        Text(text = "-")
        NumberField(
            modifier = modifier,
            value = value.max.toFloat(),
            onValueChange = { onValueChange(value.copy(max = it.toInt())) },
            readOnly = readOnly
        )
    }
}