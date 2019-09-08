package xyz.neopandu.moov.helper

import kotlinx.coroutines.*

fun debounce(delayMs: Long = 500L, f: () -> Unit): Job {
    return GlobalScope.launch(Dispatchers.Main) {
        delay(delayMs)
        f.invoke()
    }
}