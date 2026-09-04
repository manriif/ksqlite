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

package ksqlite.internal.runtime.concurrency

import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.close
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.decrementAndFetch
import kotlin.concurrent.atomics.incrementAndFetch

/**
 * On Linux and mingw, `Lock.close()` calls `pthread_mutex_destroy` unconditionally. That is
 * undefined behavior while another thread is still locking or unlocking it. Stately's [Lock]
 * does not check for this, which caused test failures on Linux. Apple and mingw silently
 * ignore the same misuse.
 *
 * This tracks how many [lock]/[unlock] pairs are currently in flight. It only destroys the
 * underlying lock once none are left. The last matching [unlock] after [close] does it.
 */
public actual class SafeLock: AutoCloseable {

    private val lock = Lock()
    private val waiterCount = AtomicInt(0)
    private val lockClosed = AtomicBoolean(false)
    private val closed = AtomicBoolean(false)

    public actual val isClosed: Boolean
        get() = closed.load()

    public actual fun lock() {
        val _ = waiterCount.incrementAndFetch()

        // The underlying lock has already been destroyed. It must not be touched at all.
        if (lockClosed.load()) {
            leave()
            throw IllegalStateException("Lock is closed")
        }

        lock.lock()

        // Re-checked only now that the real lock is actually held. A call already blocked here
        // when close() ran must still see it, the same way it would on any other mutex.
        if (closed.load()) {
            lock.unlock()
            leave()
            throw IllegalStateException("Lock is closed")
        }
    }

    public actual fun unlock() {
        lock.unlock()
        leave()
    }

    public actual override fun close() {
        if (!closed.compareAndSet(expectedValue = false, newValue = true)) {
            return
        }

        if (waiterCount.load() == 0) {
            destroy()
        }
    }

    /**
     * Un-counts a [lock]/[unlock] pair, destroying the underlying lock if [close] was called and
     * this was the last one still in flight.
     */
    private fun leave() {
        val count = waiterCount.decrementAndFetch()

        if (count == 0 && closed.load()) {
            destroy()
        }
    }

    private fun destroy() {
        if (lockClosed.compareAndSet(expectedValue = false, newValue = true)) {
            lock.close()
        }
    }
}
