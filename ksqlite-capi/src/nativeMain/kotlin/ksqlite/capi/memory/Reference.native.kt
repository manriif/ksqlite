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

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.staticCFunction
import ksqlite.capi.callbacks.SqliteDestroyCallback

/**
 * C-static function disposing a [Reference] from a [kotlinx.cinterop.StableRef].
 *
 * Throws [IllegalStateException] if the [COpaquePointer] passed to the function is `null`.
 */
private val StableRefDisposer = staticCFunction { pointer: COpaquePointer? ->
    stableRefDisposable(checkNotNull(pointer)).dispose()
}

/**
 * Returns [StableRefDisposer] only if [data] != `null` or [destructor] != `null`.
 */
internal fun stableRefDisposer(
    data: Any?,
    destructor: SqliteDestroyCallback<*>? = null
) = StableRefDisposer.takeIf { data != null || destructor != null }

/**
 * Returns the [Disposable] referenced by [pointer].
 */
internal fun stableRefDisposable(pointer: COpaquePointer): Disposable =
    pointer.asStableRef<Disposable>().get()

/**
 * Returns the [DataHolder] referenced by [pointer].
 */
internal inline fun <reified Data : Any, AppData> stableRefDataHolder(
    pointer: COpaquePointer?
): DataHolder<Data, AppData> = checkNotNull(pointer) { "Pointer must not be null" }
    .asStableRef<Reference<AppData>>().get().cast()

/**
 * Returns the [Data] referenced by [pointer].
 */
internal inline fun <reified Data : Any> stableRefData(pointer: COpaquePointer?): Data =
    stableRefDataHolder<Data, Any?>(pointer).data

/**
 * Returns the [AppData] referenced by [pointer].
 */
internal fun <AppData> stableRefAppData(pointer: COpaquePointer?): AppData =
    stableRefDataHolder<Any, AppData>(pointer).appData