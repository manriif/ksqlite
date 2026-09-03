package ksqlite.internal.test.concurrent

import kotlin.time.Duration

public actual class BackgroundThread(private val thread: Thread) {

    public actual fun join(timeout: Duration): Boolean {
        thread.join(timeout.inWholeMilliseconds.coerceAtLeast(1L))
        return !thread.isAlive
    }
}

public actual fun startBackgroundThread(block: () -> Unit): BackgroundThread = BackgroundThread(
    Thread(block).apply {
        isDaemon = true
        start()
    }
)