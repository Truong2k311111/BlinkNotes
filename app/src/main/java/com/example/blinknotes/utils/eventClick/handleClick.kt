package com.example.blinknotes.utils.eventClick

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun handleClick(): (CoroutineScope, suspend () -> Unit) -> Unit {
    val isHandlingClick = remember { mutableStateOf(false) }
    return { coroutineScope: CoroutineScope, action: suspend () -> Unit ->
        if (!isHandlingClick.value) {
            coroutineScope.launch {
                isHandlingClick.value = true
                action()
                isHandlingClick.value = false
            }
        }
    }
}