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

import ksqlite.foreign.JniPointer

private typealias JniStruct = ksqlite.structs.Struct<*, *, Long>

public actual open class Struct internal constructor(internal val pointer: JniPointer) :
    StructBase() {

    actual override val address: Long
        get() = pointer

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Struct) return false

        return pointer == other.pointer
    }

    override fun hashCode(): Int {
        return pointer.hashCode()
    }
}

public actual open class CloseableStruct internal constructor(
    private val jniStruct: JniStruct,
    private val owner: PointerOwner
) : Struct(jniStruct.pointer),
    AutoCloseable {

    public actual override fun close() {
        owner.handleClose(jniStruct::free)
    }
}