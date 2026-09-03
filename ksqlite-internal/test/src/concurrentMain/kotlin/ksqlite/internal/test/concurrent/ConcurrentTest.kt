/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:OptIn(ExperimentalAtomicApi::class)

package ksqlite.internal.test.concurrent

import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.close
import co.touchlab.stately.concurrency.withLock
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * Runs [action] concurrently on [threadCount] real background threads. Blocks the calling thread
 * until every thread returns or [timeout] elapses.
 *
 * Threads wait at a barrier until all of them have started, then start together. This maximizes
 * contention on shared state, which is what exposes real races and deadlocks.
 *
 * A [Throwable] raised by [action] on any thread is captured instead of crashing that thread. It
 * is re-thrown from this function once every thread finishes. Extra failures are attached via
 * [Throwable.addSuppressed].
 *
 * @throws IllegalStateException if a thread is still running when [timeout] elapses. This usually
 * means a deadlock.
 */
public fun runConcurrently(
    threadCount: Int,
    timeout: Duration = 10.seconds,
    action: (threadIndex: Int) -> Unit,
) {
    require(threadCount > 0) { "threadCount must be positive, was $threadCount" }

    val readyCount = AtomicInt(0)
    val release = AtomicBoolean(false)
    val failuresLock = Lock()
    val failures = mutableListOf<Throwable>()
    val start = TimeSource.Monotonic.markNow()

    try {
        val threads = List(threadCount) { index ->
            startBackgroundThread {
                val _ = readyCount.incrementAndFetch()
                while (!release.load()) {
                    // Spin until every thread has started.
                }

                try {
                    action(index)
                } catch (t: Throwable) {
                    failuresLock.withLock { failures += t }
                }
            }
        }

        while (readyCount.load() < threadCount && start.elapsedNow() < timeout) {
            // Wait for every thread to reach the barrier.
        }
        release.store(true)

        val stillRunning = threads.count { thread ->
            val remaining = (timeout - start.elapsedNow()).coerceAtLeast(Duration.ZERO)
            !thread.join(remaining)
        }

        check(stillRunning == 0) {
            "$stillRunning of $threadCount thread(s) did not complete within $timeout"
        }

        if (failures.isNotEmpty()) {
            throw failures[0].apply { failures.drop(1).forEach(::addSuppressed) }
        }
    } finally {
        failuresLock.close()
    }
}
