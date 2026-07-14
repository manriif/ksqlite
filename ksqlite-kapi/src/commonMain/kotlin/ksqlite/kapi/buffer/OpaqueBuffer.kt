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

package ksqlite.kapi.buffer

import ksqlite.kapi.throwSQLiteException
import ksqlite.types.SqliteResultCode
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import ksqlite.capi.memory.OpaqueBuffer as CapiOpaqueBuffer

/**
 * Platform managed memory region that is not intended to be read nor written by the application.
 *
 * [OpaqueBuffer] does not provide any kind of thread-safety and external synchronization is
 * required if concurrent access is needed.
 *
 * The [OpaqueBuffer] must be closed once no longer needed to release allocated resources.
 */
public class OpaqueBuffer internal constructor(internal val buffer: CapiOpaqueBuffer) :
    AutoCloseable {

    /**
     * Size of the buffer in bytes.
     */
    public val byteSize: Long
        get() = buffer.byteSize

    /**
     * Frees this buffer memory.
     * Does nothing is the buffer is already freed.
     */
    override fun close() {
        buffer.close()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Companion
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Provides factory functions for allocating a [OpaqueBuffer].
     */
    public companion object {

        /**
         * Allocates a native memory region of [size] bytes and returns a [OpaqueBuffer].
         *
         * @throws ksqlite.kapi.SQLiteException if allocation fails.
         */
        public fun allocate(size: Long): OpaqueBuffer = OpaqueBuffer(
            CapiOpaqueBuffer.allocate(size) ?: throwSQLiteException(
                message = "Failed to allocate $size bytes of memory",
                result = SqliteResultCode.NOMEM
            )
        )
    }
}