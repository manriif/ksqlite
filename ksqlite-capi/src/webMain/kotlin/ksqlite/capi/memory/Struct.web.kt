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
@file:Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")

package ksqlite.capi.memory

import ksqlite.capi.wasm
import ksqlite.foreign.structs.StructType
import ksqlite.foreign.wasm.WasmPointer
import kotlin.js.toLong

public actual open class Struct internal constructor(internal val pointer: WasmPointer) :
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

public actual open class ClosableStruct(private val struct: StructType) :
    Struct(struct.pointer),
    AutoCloseable {

    public actual override fun close() {
        struct.dispose()
    }
}

/**
 * For [StructType] that does not own its pointer and thus is not responsible for freeing it in
 * [StructType.dispose].
 */
public open class PointerOwnedStruct(struct: StructType) : ClosableStruct(struct) {

    public override fun close() {
        super.close()
        wasm.dealloc(pointer)
    }
}