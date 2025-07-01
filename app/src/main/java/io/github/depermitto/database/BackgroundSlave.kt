package io.github.depermitto.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

object BackgroundSlave {
    private val scope = MainScope()
    private val queue = Channel<Job>(Channel.UNLIMITED)

    init {
        scope.launch(Dispatchers.IO) {
            for (job in queue) job.join()
        }
    }

    fun enqueue(
        context: CoroutineContext = Dispatchers.IO, action: suspend CoroutineScope.() -> Unit
    ) = synchronized(this) {
        val job = scope.launch(context, CoroutineStart.LAZY, action)
        queue.trySend(job)
    }

    fun waitForAll() = scope.launch(Dispatchers.IO) {
        for (job in queue) job.join()
    }

    fun quit() {
        queue.close()
        scope.cancel()
    }
}
