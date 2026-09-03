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

package ksqlite.capi.memory

import ksqlite.capi.callbacks.SqliteDestroyCallback
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch

/**
 * [MemoryManagerBase] double used to test its disposable registry and locking.
 *
 * Shared by [MemoryManagerFunctionalTest] (JVM, Native, Web) and the JVM+Native concurrency suite.
 * Android does not use [MemoryManagerBase].
 */
internal class TestMemoryManager : MemoryManagerBase() {

    private val _releaseCount = AtomicInt(0)

    /**
     * Number of times a disposable's `release()` has run, across every disposable ever created by
     * this manager.
     */
    val releaseCount: Int
        get() = _releaseCount.load()

    /**
     * Registers and returns a new disposable.
     *
     * Guarded by [notClosed], like real [MemoryManagerBase] subclasses. Calling this after [close]
     * throws [IllegalStateException].
     */
    fun register(
        key: String? = null,
        destructor: SqliteDestroyCallback<Unit>? = null,
    ): RegisteredDisposable = notClosed {
        registerDisposable(key) { id -> TestDisposable(id, destructor) }.toHandle()
    }

    /**
     * Returns the disposable previously registered with [id].
     *
     * @throws NullPointerException if no disposable is associated with [id].
     * @throws ClassCastException if the disposable associated with [id] is not the type registered
     * by [register].
     */
    fun get(id: Long): RegisteredDisposable = notClosed {
        getDisposable<Unit, TestDisposable>(id).toHandle()
    }

    /**
     * Looks up the disposable associated with [id] as the wrong type, to trigger
     * [getDisposable]'s [ClassCastException].
     */
    fun getAsOther(id: Long) {
        val _ = notClosed { getDisposable<Unit, OtherDisposable>(id) }
    }

    private fun TestDisposable.toHandle() = RegisteredDisposable(disposableId, this)

    private inner class TestDisposable(
        val disposableId: Long,
        destructor: SqliteDestroyCallback<Unit>?,
    ) : AutoDisposable<Unit>(disposableId, destructor) {

        override val appData: Unit = Unit

        override fun release() {
            val _ = _releaseCount.incrementAndFetch()
        }
    }

    private inner class OtherDisposable(id: Long) : AutoDisposable<Unit>(id, null) {
        override val appData: Unit = Unit
        override fun release() = Unit
    }
}

/**
 * Handle to a disposable registered by [TestMemoryManager.register].
 *
 * Two handles are equal when they wrap the same disposable instance.
 */
internal class RegisteredDisposable(val id: Long, private val disposable: Disposable) {

    fun dispose(callDestructor: Boolean = true) = disposable.dispose(callDestructor)

    override fun equals(other: Any?): Boolean =
        other is RegisteredDisposable && disposable === other.disposable

    override fun hashCode(): Int = disposable.hashCode()
}
