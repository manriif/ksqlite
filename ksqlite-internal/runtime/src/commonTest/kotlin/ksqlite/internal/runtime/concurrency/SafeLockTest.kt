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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Single-threaded correctness tests for [SafeLock].
 */
class SafeLockTest {

    @Test
    fun newLockIsNotClosed() {
        assertFalse(SafeLock().isClosed)
    }

    @Test
    fun lockThenUnlockSucceeds() {
        val lock = SafeLock()
        lock.lock()
        lock.unlock()
    }

    @Test
    fun withLockReturnsTheBlockResult() {
        val lock = SafeLock()
        assertEquals(42, lock.withLock { 42 })
    }

    @Test
    fun withLockNestsOnTheSameThread() {
        val lock = SafeLock()
        val result = lock.withLock { lock.withLock { "nested" } }
        assertEquals("nested", result)
    }

    @Test
    fun withLockSerializesSequentialUpdatesToSharedState() {
        val lock = SafeLock()
        var counter = 0
        repeat(100) { lock.withLock { counter++ } }
        assertEquals(100, counter)
    }

    @Test
    fun closeMarksTheLockClosed() {
        val lock = SafeLock()
        lock.close()
        assertTrue(lock.isClosed)
    }

    @Test
    fun closeIsIdempotent() {
        val lock = SafeLock()
        lock.close()
        lock.close()
        assertTrue(lock.isClosed)
    }

    @Test
    fun lockThrowsAfterClose() {
        val lock = SafeLock()
        lock.close()
        assertFailsWith<IllegalStateException> { lock.lock() }
    }

    @Test
    fun withLockThrowsAfterClose() {
        val lock = SafeLock()
        lock.close()
        assertFailsWith<IllegalStateException> { lock.withLock { } }
    }

    @Test
    fun closeFromWithinTheLockLetsTheCurrentHolderFinish() {
        val lock = SafeLock()

        val result = lock.withLock {
            lock.close()
            "still finished"
        }

        assertEquals("still finished", result)
        assertTrue(lock.isClosed)
        assertFailsWith<IllegalStateException> { lock.lock() }
    }
}
