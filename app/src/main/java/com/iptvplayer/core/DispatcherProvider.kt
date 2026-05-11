package com.iptvplayer.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlin.coroutines.CoroutineContext

data class DispatcherProvider(
    val io: CoroutineDispatcher,
    val default: CoroutineDispatcher,
    val main: CoroutineDispatcher
) {
    companion object {
        fun mainThread() = DispatcherProvider(
            io = kotlinx.coroutines.Dispatchers.Main,
            default = kotlinx.coroutines.Dispatchers.Main,
            main = kotlinx.coroutines.Dispatchers.Main
        )
    }
}
