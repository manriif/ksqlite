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

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.NativePtr
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.pointed
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.capi.memory.ClosableStruct
import ksqlite.capi.memory.PointerOwner
import ksqlite.capi.memory.StructLayout
import ksqlite.foreign.ksqlite_cipher_params
import ksqlite.foreign.sqlite3_free
import ksqlite.foreign.sqlite3_mprintf
import ksqlite.types.cipher.SqliteMcCipherParams

public actual class CipherParams private constructor(
    override val pointer: CPointer<ksqlite_cipher_params>,
    owner: PointerOwner
) : ClosableStruct(pointer, owner),
    SqliteMcCipherParams {

    public actual constructor() : this(allocate(), Application)

    init {
        pointer.pointed.m_name = null
    }

    actual override var m_name: String
        get() = pointer.pointed.m_name!!.toKStringFromUtf8()
        set(value) {
            sqlite3_free(pointer.pointed.m_name)
            pointer.pointed.m_name = sqlite3_mprintf(value)
        }

    actual override var m_value: Int
        get() = pointer.pointed.m_value
        set(value) {
            pointer.pointed.m_value = value
        }

    actual override var m_default: Int
        get() = pointer.pointed.m_default
        set(value) {
            pointer.pointed.m_default = value
        }

    actual override var m_minValue: Int
        get() = pointer.pointed.m_minValue
        set(value) {
            pointer.pointed.m_minValue = value
        }

    actual override var m_maxValue: Int
        get() = pointer.pointed.m_maxValue
        set(value) {
            pointer.pointed.m_maxValue = value
        }

    override fun close() {
        cleanup(this)
        super.close()
    }

    public actual companion object : StructLayout<CipherParams>() {

        override val elementSize: Long
            get() = sizeOf<ksqlite_cipher_params>()

        override fun reinterpret(rawPtr: NativePtr): CipherParams =
            CipherParams(interpretCPointer(rawPtr)!!, InternalArray)

        actual override fun cleanup(instance: CipherParams) {
            sqlite3_free(instance.pointer.pointed.m_name)
        }
    }
}