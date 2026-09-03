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
package ksqlite.capi.memory

import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.close
import co.touchlab.stately.concurrency.withLock
import ksqlite.internal.test.concurrent.runConcurrently
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

private const val ThreadCount = 16
private const val DisposablesPerThread = 200
private val Timeout = 15.seconds

/**
 * Concurrency stress tests for [MemoryManagerBase].
 */
class MemoryManagerConcurrencyTest {

    @Test
    fun registerDisposableConcurrentlyAssignsUniqueIdsWithoutDeadlocking() {
        val manager = TestMemoryManager()
        val idsLock = Lock()
        val ids = mutableListOf<Long>()

        try {
            runConcurrently(threadCount = ThreadCount, timeout = Timeout) {
                val localIds = List(DisposablesPerThread) { manager.register().id }
                idsLock.withLock { ids += localIds }
            }

            assertEquals(ThreadCount * DisposablesPerThread, ids.size)
            assertEquals(
                ids.size,
                ids.toSet().size,
                "registerDisposable() handed out at least one duplicate id under contention"
            )
        } finally {
            idsLock.close()
            manager.close()
        }
    }

    @Test
    fun disposeConcurrentlyLeavesTheManagerEmpty() {
        val manager = TestMemoryManager()
        val disposables = List(ThreadCount * DisposablesPerThread) { manager.register() }

        try {
            runConcurrently(threadCount = ThreadCount, timeout = Timeout) { threadIndex ->
                val fromIndex = threadIndex * DisposablesPerThread
                val toIndex = fromIndex + DisposablesPerThread

                // Each thread disposes a disjoint slice. This races dispose() against other
                // dispose() calls, not against itself.
                disposables.subList(fromIndex, toIndex).forEach { it.dispose() }
            }

            assertTrue(manager.isEmpty, "Manager should be empty once every disposable is disposed")
            assertEquals(disposables.size, manager.releaseCount)
        } finally {
            manager.close()
        }
    }

    @Test
    fun mixedRegisterAndDisposeConcurrentlyLeavesTheManagerConsistent() {
        val manager = TestMemoryManager()

        try {
            // Each thread registers and immediately disposes its own disposables. Unlike the two
            // tests above, register() and dispose() now race each other, not just themselves.
            runConcurrently(threadCount = ThreadCount, timeout = Timeout) {
                repeat(DisposablesPerThread) {
                    manager.register().dispose()
                }
            }

            assertTrue(manager.isEmpty)
            assertEquals(ThreadCount * DisposablesPerThread, manager.releaseCount)
        } finally {
            manager.close()
        }
    }

    @Test
    fun concurrentKeyedRegistrationOnTheSameKeyLeavesExactlyOneSurvivor() {
        val manager = TestMemoryManager()
        val key = "shared"

        try {
            // Every thread repeatedly re-registers the same key. registerDisposable() is fully
            // serialized by disposableLock. At most one entry can survive. Every replaced
            // disposable must be released exactly once.
            runConcurrently(threadCount = ThreadCount, timeout = Timeout) {
                repeat(DisposablesPerThread) {
                    val _ = manager.register(key = key)
                }
            }

            val totalRegistrations = ThreadCount * DisposablesPerThread
            assertTrue(!manager.isEmpty, "Exactly one keyed disposable should survive")
            assertEquals(totalRegistrations - 1, manager.releaseCount)

            manager.clearDisposable(key)
            assertTrue(manager.isEmpty)
            assertEquals(totalRegistrations, manager.releaseCount)
        } finally {
            manager.close()
        }
    }

    @Test
    fun closeConcurrentlyWithRegisterNeverCorruptsState() {
        val manager = TestMemoryManager()

        // One thread closes the manager while every other thread races to register disposables.
        // Every register() call must either succeed or fail with the documented
        // IllegalStateException. It must never hang, crash, or corrupt the registry.
        runConcurrently(threadCount = ThreadCount, timeout = Timeout) { threadIndex ->
            if (threadIndex == 0) {
                manager.close()
            } else {
                repeat(DisposablesPerThread) {
                    try {
                        val _ = manager.register()
                    } catch (_: IllegalStateException) {
                        // Expected once the closing thread wins the race.
                    }
                }
            }
        }

        assertTrue(manager.isEmpty, "close() must have disposed everything that made it in")
        assertFailsWith<IllegalStateException> { manager.register() }
    }
}