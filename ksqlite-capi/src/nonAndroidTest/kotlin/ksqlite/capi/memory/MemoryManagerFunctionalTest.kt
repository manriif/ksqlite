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

import ksqlite.capi.callbacks.SqliteDestroyCallback
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Single-threaded correctness tests for [MemoryManagerBase].
 */
class MemoryManagerFunctionalTest {

    ///////////////////////////////////////////////////////////////////////////
    // isEmpty
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun newManagerIsEmpty() {
        val manager = TestMemoryManager()
        assertTrue(manager.isEmpty)
    }

    @Test
    fun registeringADisposableMakesTheManagerNonEmpty() {
        val manager = TestMemoryManager()
        val _ = manager.register()
        assertFalse(manager.isEmpty)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Registration
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun eachUnkeyedRegistrationGetsAUniqueMonotonicId() {
        val manager = TestMemoryManager()
        val ids = List(5) { manager.register().id }

        assertEquals(ids.sorted(), ids, "ids should be handed out in increasing order")
        assertEquals(ids.toSet().size, ids.size, "ids should be unique")
    }

    @Test
    fun clearingDoesNotResetTheIdCounter() {
        val manager = TestMemoryManager()
        val firstId = manager.register().id
        manager.clear()
        val secondId = manager.register().id

        assertNotEquals(firstId, secondId)
        assertTrue(secondId > firstId, "ids must not be reused after clear()")
    }

    @Test
    fun keyedRegistrationWithANewKeyBehavesLikeAnUnkeyedOne() {
        val manager = TestMemoryManager()
        val disposable = manager.register(key = "a")

        assertEquals(disposable, manager.get(disposable.id))
        assertFalse(manager.isEmpty)
    }

    @Test
    fun keyedRegistrationWithTheSameKeyReusesTheSameIdAndReplacesTheDisposable() {
        val manager = TestMemoryManager()
        val first = manager.register(key = "shared")
        val second = manager.register(key = "shared")

        assertEquals(first.id, second.id, "re-registering a key must reuse its id")
        assertEquals(1, manager.releaseCount, "the replaced disposable must be released exactly once")
        assertEquals(second, manager.get(second.id), "the surviving entry must be the new one")
    }

    @Test
    fun keyedRegistrationWithDifferentKeysGetsDifferentIds() {
        val manager = TestMemoryManager()
        val a = manager.register(key = "a")
        val b = manager.register(key = "b")

        assertNotEquals(a.id, b.id)
        assertEquals(0, manager.releaseCount)
    }

    ///////////////////////////////////////////////////////////////////////////
    // getDisposable
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun getDisposableReturnsTheRegisteredInstance() {
        val manager = TestMemoryManager()
        val disposable = manager.register()

        assertEquals(disposable, manager.get(disposable.id))
    }

    @Test
    fun getDisposableThrowsForAnUnknownId() {
        val manager = TestMemoryManager()
        assertFailsWith<NullPointerException> { manager.get(1234L) }
    }

    @Test
    fun getDisposableThrowsForAWrongType() {
        val manager = TestMemoryManager()
        val disposable = manager.register()

        assertFailsWith<ClassCastException> { manager.getAsOther(disposable.id) }
    }

    ///////////////////////////////////////////////////////////////////////////
    // dispose
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun disposeRemovesTheDisposableAndCallsRelease() {
        val manager = TestMemoryManager()
        val disposable = manager.register()

        disposable.dispose()

        assertTrue(manager.isEmpty)
        assertEquals(1, manager.releaseCount)
    }

    @Test
    fun disposeCallsTheDestructorByDefault() {
        val manager = TestMemoryManager()
        var destructorCalls = 0
        val disposable = manager.register(destructor = SqliteDestroyCallback { destructorCalls++ })

        disposable.dispose()

        assertEquals(1, destructorCalls)
    }

    @Test
    fun disposeWithCallDestructorFalseSkipsTheDestructorButStillReleases() {
        val manager = TestMemoryManager()
        var destructorCalls = 0
        val disposable = manager.register(destructor = SqliteDestroyCallback { destructorCalls++ })

        disposable.dispose(callDestructor = false)

        assertEquals(0, destructorCalls)
        assertEquals(1, manager.releaseCount)
    }

    @Test
    fun disposingTheSameDisposableTwiceThrows() {
        val manager = TestMemoryManager()
        val disposable = manager.register()
        disposable.dispose()

        assertFailsWith<IllegalStateException> { disposable.dispose() }
    }

    ///////////////////////////////////////////////////////////////////////////
    // clearDisposable
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun clearDisposableRemovesTheKeyedEntryAndCallsRelease() {
        val manager = TestMemoryManager()
        val _ = manager.register(key = "a")

        manager.clearDisposable("a")

        assertTrue(manager.isEmpty)
        assertEquals(1, manager.releaseCount)
    }

    @Test
    fun clearDisposableIsANoOpForAnUnknownKey() {
        val manager = TestMemoryManager()
        manager.clearDisposable("missing")
        assertEquals(0, manager.releaseCount)
    }

    @Test
    fun clearDisposableAllowsReRegisteringTheSameKeyAfterward() {
        val manager = TestMemoryManager()
        val first = manager.register(key = "a")
        manager.clearDisposable("a")
        val second = manager.register(key = "a")

        assertNotEquals(first.id, second.id, "the key's slot must be free again")
    }

    ///////////////////////////////////////////////////////////////////////////
    // clear
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun clearDisposesEveryOutstandingDisposable() {
        val manager = TestMemoryManager()
        repeat(5) { val _ = manager.register() }
        val _ = manager.register(key = "keyed")

        manager.clear()

        assertTrue(manager.isEmpty)
        assertEquals(6, manager.releaseCount)
    }

    @Test
    fun clearLeavesTheManagerUsable() {
        val manager = TestMemoryManager()
        val _ = manager.register()
        manager.clear()

        val disposable = manager.register()
        assertFalse(manager.isEmpty)
        assertEquals(disposable, manager.get(disposable.id))
    }

    ///////////////////////////////////////////////////////////////////////////
    // close
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun closeDisposesEveryOutstandingDisposable() {
        val manager = TestMemoryManager()
        repeat(3) { val _ = manager.register() }

        manager.close()

        assertEquals(3, manager.releaseCount)
    }

    @Test
    fun closeIsIdempotent() {
        val manager = TestMemoryManager()
        val _ = manager.register()

        manager.close()
        manager.close()

        assertEquals(1, manager.releaseCount, "a second close() must not re-dispose anything")
    }

    @Test
    fun operationsAfterCloseThrow() {
        val manager = TestMemoryManager()
        manager.close()

        assertFailsWith<IllegalStateException> { manager.register() }
    }
}
