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
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CVariable
import kotlinx.cinterop.convert
import kotlinx.cinterop.pointed
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toLong
import ksqlite.foreign.sqlite3_free
import ksqlite.foreign.sqlite3_malloc64

public actual open class Struct internal constructor(internal open val pointer: COpaquePointer) :
    StructBase() {

    actual override val address: Long
        get() = pointer.toLong()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Struct) return false

        return pointer == other.pointer
    }

    override fun hashCode(): Int {
        return pointer.hashCode()
    }
}

public actual open class ClosableStruct internal constructor(
    pointer: COpaquePointer,
    private val owner: PointerOwner,
) : Struct(pointer),
    AutoCloseable {

    public actual override fun close() {
        owner.handleClose {
            sqlite3_free(pointer)
        }
    }

    internal companion object {

        /**
         * Allocates [S] using SQLite's allocator.
         */
        protected inline fun <reified S : CVariable> allocate(size: Long? = null): CPointer<S> =
            checkNotNull(sqlite3_malloc64(checkStructSize(sizeOf<S>(), size).convert())) {
                "Failed to allocate an instance of ${S::class}"
            }.reinterpret()

        /**
         * Allocates [S] using SQLite's allocator and invokes [configure] with the allocated [S]
         * as receiver.
         */
        protected inline fun <reified S : CVariable> allocate(
            size: Long? = null,
            configure: S.() -> Unit
        ): CPointer<S> = allocate<S>(size).apply {
            pointed.configure()
        }
    }
}