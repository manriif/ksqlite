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

import ksqlite.capi.memory.CloseableStruct
import ksqlite.capi.memory.NullPtr
import ksqlite.capi.memory.PointerOwner
import ksqlite.capi.memory.StructLayout
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.sqlite3_mprintf
import ksqlite.foreign.JniPointer
import ksqlite.foreign.sqlite3_free
import ksqlite.foreign.structs.ksqlite_cipher_params
import ksqlite.structs.RawStructType
import ksqlite.structs.StructType
import ksqlite.types.cipher.SqliteMcCipherParams

public actual class CipherParams private constructor(
    private val params: ksqlite_cipher_params,
    owner: PointerOwner
) : CloseableStruct(params, owner),
    SqliteMcCipherParams {

    public actual constructor() : this(ksqlite_cipher_params(null), Application)

    init {
        params.m_name = NullPtr
    }

    actual override var m_name: String
        get() = params.m_name.toKStringFromUtf8()
        set(value) = sqlite3_mprintf(params::m_name, value)

    actual override var m_value: Int
        get() = params.m_value
        set(value) {
            params.m_value = value
        }

    actual override var m_default: Int
        get() = params.m_default
        set(value) {
            params.m_default = value
        }

    actual override var m_minValue: Int
        get() = params.m_minValue
        set(value) {
            params.m_minValue = value
        }

    actual override var m_maxValue: Int
        get() = params.m_maxValue
        set(value) {
            params.m_maxValue = value
        }

    override fun close() {
        cleanup(this)
        super.close()
    }

    public actual companion object : StructLayout<CipherParams>() {

        override val type: RawStructType
            get() = StructType.KsqliteCipherParams

        override fun reinterpret(pointer: JniPointer): CipherParams =
            CipherParams(ksqlite_cipher_params(pointer), InternalArray)

        actual override fun cleanup(instance: CipherParams) {
            sqlite3_free(instance.params.m_name)
        }
    }
}