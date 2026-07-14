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
import java.lang.foreign.MemorySegment

/**
 * Pointer to a static function disposing a [Disposable] registered with [registerGlobalDisposable].
 */
private val GlobalDisposer: MemorySegment =
    StaticMemoryAllocator.allocateReferenceFunction { disposeGlobal(it.orNull?.address()) }

/**
 * Returns [GlobalDisposer] or [NullPtr] if [data] is `null`.
 */
internal fun globalDisposer(data: Any?) =
    GlobalDisposer.takeIf { data != null } ?: NullPtr

/**
 * Registers a [Disposable] which will invoke [destructor] when disposed.
 * If [destructor] is `null` then `null` is returned.
 */
internal fun bufferDisposer(
    buffer: Buffer,
    destructor: SqliteDestroyCallback<Buffer>?
) = instanceDisposer(
    disposer = GlobalDisposer,
    instance = buffer,
    address = buffer.address,
    destructor = destructor,
).notNull