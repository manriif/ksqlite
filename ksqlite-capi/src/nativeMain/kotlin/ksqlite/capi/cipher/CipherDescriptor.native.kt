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

import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.capi.memory.ClosableStruct
import ksqlite.capi.s3
import ksqlite.foreign.ksqlite_cipher_descriptor
import ksqlite.foreign.sqlite3_free
import ksqlite.foreign.sqlite3_mprintf
import ksqlite.types.cipher.SqliteMcCipherDescriptor

public actual class CipherDescriptor private constructor(
    internal val callbacks: CipherCallbacks<*>,
    override val pointer: CPointer<ksqlite_cipher_descriptor>
) : ClosableStruct(pointer, Application),
    SqliteMcCipherDescriptor {

    internal actual constructor(
        name: String,
        callbacks: CipherCallbacks<*>
    ) : this(callbacks, allocate {
        m_name = sqlite3_mprintf(name)
        m_freeCipher = CipherFreeCipherHandler
        m_cloneCipher = CipherCloneCipherHandler
        m_getLegacy = CipherGetLegacyHandler
        m_getPageSize = CipherGetPageSizeHandler
        m_getReserved = CipherGetReservedHandler
        m_getSalt = CipherGetSaltHandler
        m_generateKey = CipherGenerateKeyHandler
        m_encryptPage = CipherEncryptPageHandler
        m_decryptPage = CipherDecryptPageHandler
    })

    actual override val m_name: String
        get() = pointer.pointed.m_name!!.toKStringFromUtf8()

    internal fun setAllocator(function: CPointer<CFunction<(CPointer<s3>?) -> COpaquePointer?>>?) {
        pointer.pointed.m_allocateCipher = function
    }

    override fun close() {
        sqlite3_free(pointer.pointed.m_name)
        super.close()
    }
}