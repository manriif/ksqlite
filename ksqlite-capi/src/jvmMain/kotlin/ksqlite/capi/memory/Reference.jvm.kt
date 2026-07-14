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
import ksqlite.capi.handlers.Handler
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

/**
 * Handler that dispose reference to object.
 */
internal class StableRefDisposerHandler : Handler(), ReferenceFunction {

    override fun allocate(arena: Arena): MemorySegment = arena.allocateReferenceFunction(this)

    override fun apply(refPointer: MemorySegment) {
        manager.stableRefDisposable(refPointer).dispose()
    }
}

/**
 * Returns the [StableRefDisposerHandler] instance of `this` [MemoryManager] only if
 * [data] != `null` or [destructor] != `null`. [NullPtr] is returned otherwise.
 */
internal fun MemoryManager.stableRefDisposer(
    data: Any?,
    destructor: SqliteDestroyCallback<*>? = null
): MemorySegment = stableRefDisposer.takeIf { data != null || destructor != null } ?: NullPtr

/**
 * Returns the [Disposable] referenced by [pointer].
 */
internal fun MemoryManager.stableRefDisposable(pointer: MemorySegment): Disposable =
    getStableRef<Nothing?>(pointer)

/**
 * Returns the object [Data] referenced by [pointer] with an optional app data pointer.
 */
internal inline fun <reified Data : Any, AppData> MemoryManager.stableRefDataHolder(
    pointer: MemorySegment
): DataHolder<Data, AppData> = getStableRef<AppData>(pointer).cast()

/**
 * Returns the [AppData] referenced by [pointer].
 */
internal fun <AppData> MemoryManager.stableRefAppData(pointer: MemorySegment): AppData =
    stableRefDataHolder<Any, AppData>(pointer).appData

/**
 * Returns the [Data] referenced by [pointer].
 */
internal inline fun <reified Data : Any> MemoryManager.stableRefData(pointer: MemorySegment): Data =
    stableRefDataHolder<Data, Any?>(pointer).data