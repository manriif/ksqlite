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

import ksqlite.foreign.sqlite3.sqlite3_free
import ksqlite.foreign.sqlite3.sqlite3_malloc64
import java.lang.foreign.Arena
import java.lang.foreign.GroupLayout
import java.lang.foreign.MemorySegment

public actual open class Struct internal constructor(internal val pointer: MemorySegment) :
    StructBase() {

    actual override val address: Long
        get() = pointer.address()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Struct) return false

        return pointer == other.pointer
    }

    override fun hashCode(): Int {
        return pointer.hashCode()
    }
}

public actual open class ClosableStruct private constructor(
    private val originalPointer: MemorySegment,
    reinterpretedPointer: MemorySegment,
    private val owner: PointerOwner,
    private val arena: Arena,
) : Struct(reinterpretedPointer),
    AutoCloseable {

    internal constructor(
        layout: GroupLayout,
        pointer: MemorySegment,
        owner: PointerOwner,
        arena: Arena = Arena.ofShared()
    ) : this(pointer, pointer.reinterpret(layout.byteSize(), arena, null), owner, arena)

    internal constructor(
        layout: GroupLayout,
        pointer: MemorySegment?, // null = allocate new, non-null = reinterpret it
        owner: PointerOwner,
        size: Long? = null,
        arena: Arena = Arena.ofShared()
    ) : this(
        layout = layout,
        pointer = pointer ?: sqlite3_malloc64(checkStructSize(layout.byteSize(), size)),
        owner = owner.also { check(it != Application || pointer == null) },
        arena = arena
    )

    public actual override fun close() {
        arena.close()

        owner.handleClose {
            sqlite3_free(originalPointer)
        }
    }
}