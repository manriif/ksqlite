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

import co.touchlab.stately.concurrency.Lock

/**
 * A [Lock] that can be closed safely even while another thread might still be using it.
 *
 * [close] is only really meaningful on platforms whose underlying lock frees native resources
 * (destroying one while another thread is locking or unlocking it is undefined behavior there).
 * Regardless of platform, [close] guarantees that every lock owner already holding this at the
 * time it is called can still safely finish, and that every call to [lock] made after it throws
 * [IllegalStateException].
 */
public expect class SafeLock(): AutoCloseable {

    /**
     * Whether [close] has been called.
     */
    public val isClosed: Boolean

    /**
     * Acquires this lock, blocking until it is available.
     *
     * @throws IllegalStateException if [close] has already been called.
     */
    public fun lock()

    /**
     * Releases this lock. Must be called exactly once for every successful call to [lock].
     */
    public fun unlock()

    /**
     * Marks this lock closed. Calling this again has no effect.
     */
    public override fun close()
}

/**
 * Runs [block] with this lock held.
 *
 * @throws IllegalStateException if [close] has already been called.
 */
public inline fun <T> SafeLock.withLock(block: () -> T): T {
    lock()

    return try {
        block()
    } finally {
        unlock()
    }
}
