package io.github.depermitto.bullettrain.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ListAlertDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    maxSize: DpSize = DpSize(200.dp, 300.dp),
    dismissButton: @Composable () -> Unit,
    confirmButton: @Composable (T) -> Unit,
    list: List<T>,
    content: @Composable (T) -> Unit,
) = BasicAlertDialog(modifier = Modifier, onDismissRequest = onDismissRequest) {
    assert(maxSize.height > 50.dp)

    var selected by rememberSaveable { mutableIntStateOf(0) }
    Card(
        modifier = modifier
            .width(maxSize.width)
            .heightIn(min = 0.dp, max = maxSize.height)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier.heightIn(0.dp, maxSize.height - 50.dp)) {
                itemsIndexed(list) { i, item ->
                    DropdownMenuItem(text = { content(item) }, onClick = { selected = i }, trailingIcon = {
                        RadioButton(selected = selected == i, onClick = { selected = i })
                    })
                }
            }

            Row(modifier = Modifier.align(Alignment.BottomEnd)) {
                dismissButton()
                confirmButton(list[selected])
            }
        }
    }
}