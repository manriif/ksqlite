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
package ksqlite.capi.cipher

import ksqlite.capi.memory.ClosableStruct
import ksqlite.capi.memory.NullPtr
import ksqlite.capi.memory.PointerOwner
import ksqlite.capi.memory.StructLayout
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.sqlite3_mprintf
import ksqlite.foreign.ksqlite_cipher_params
import ksqlite.foreign.sqlite3
import ksqlite.types.cipher.SqliteMcCipherParams
import java.lang.foreign.GroupLayout
import java.lang.foreign.MemorySegment

public actual class CipherParams private constructor(
    pointer: MemorySegment?,
    owner: PointerOwner
) : ClosableStruct(layout, pointer, owner),
    SqliteMcCipherParams {

    public actual constructor() : this(null, Application)

    init {
        ksqlite_cipher_params.m_name(pointer, NullPtr)
    }

    actual override var m_name: String
        get() = ksqlite_cipher_params.m_name(pointer).toKStringFromUtf8()
        set(value) {
            sqlite3.sqlite3_free(ksqlite_cipher_params.m_name(pointer))
            ksqlite_cipher_params.m_name(pointer, sqlite3_mprintf(value))
        }

    actual override var m_value: Int
        get() = ksqlite_cipher_params.m_value(pointer)
        set(value) = ksqlite_cipher_params.m_value(pointer, value)

    actual override var m_default: Int
        get() = ksqlite_cipher_params.m_default(pointer)
        set(value) = ksqlite_cipher_params.m_default(pointer, value)

    actual override var m_minValue: Int
        get() = ksqlite_cipher_params.m_minValue(pointer)
        set(value) = ksqlite_cipher_params.m_minValue(pointer, value)

    actual override var m_maxValue: Int
        get() = ksqlite_cipher_params.m_maxValue(pointer)
        set(value) = ksqlite_cipher_params.m_maxValue(pointer, value)

    override fun close() {
        cleanup(this)
        super.close()
    }

    public actual companion object : StructLayout<CipherParams>() {

        override val layout: GroupLayout
            get() = ksqlite_cipher_params.layout()

        override fun reinterpret(pointer: MemorySegment): CipherParams =
            CipherParams(pointer, InternalArray)

        actual override fun cleanup(instance: CipherParams) {
            sqlite3.sqlite3_free(ksqlite_cipher_params.m_name(instance.pointer))
        }
    }
}