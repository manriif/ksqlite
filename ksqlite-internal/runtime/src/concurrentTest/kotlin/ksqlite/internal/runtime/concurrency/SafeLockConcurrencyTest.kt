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
package ksqlite.internal.runtime.concurrency

import ksqlite.internal.test.concurrent.runConcurrently
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

private const val ThreadCount = 16
private const val OperationsPerThread = 200
private val Timeout = 15.seconds

/**
 * Concurrency stress tests for [SafeLock].
 */
class SafeLockConcurrencyTest {

    @Test
    fun concurrentWithLockCallsSerializeAccessToSharedState() {
        val lock = SafeLock()
        var counter = 0

        runConcurrently(threadCount = ThreadCount, timeout = Timeout) {
            repeat(OperationsPerThread) {
                lock.withLock { counter++ }
            }
        }

        assertEquals(ThreadCount * OperationsPerThread, counter)
    }

    @Test
    fun closeConcurrentlyWhileHoldingNeverLosesOrGainsWrites() {
        val lock = SafeLock()
        val collected = mutableListOf<Int>()
        var snapshotAtClose: List<Int>? = null

        runConcurrently(threadCount = ThreadCount, timeout = Timeout) { threadIndex ->
            if (threadIndex == 0) {
                lock.withLock {
                    snapshotAtClose = collected.toList()
                    lock.close()
                }
            } else {
                repeat(OperationsPerThread) { operation ->
                    try {
                        lock.withLock {
                            collected += threadIndex * OperationsPerThread + operation
                        }
                    } catch (_: IllegalStateException) {
                    }
                }
            }
        }

        assertEquals(collected, snapshotAtClose)
        assertEquals(collected.size, collected.toSet().size, "an item was added more than once")
        assertTrue(lock.isClosed)
        assertFailsWith<IllegalStateException> { lock.lock() }
    }

    @Test
    fun closeConcurrentlyWithoutHoldingNeverCrashes() {
        val lock = SafeLock()

        runConcurrently(threadCount = ThreadCount, timeout = Timeout) { threadIndex ->
            if (threadIndex == 0) {
                lock.close()
            } else {
                repeat(OperationsPerThread) {
                    try {
                        lock.withLock { }
                    } catch (_: IllegalStateException) {
                        // Expected once the closing thread wins the race.
                    }
                }
            }
        }

        assertTrue(lock.isClosed)
    }
}