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
import ksqlite.capi.memory.notNull
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.sqlite3_mprintf
import ksqlite.foreign.ksqlite_cipher_descriptor
import ksqlite.foreign.ksqlite_cipher_params
import ksqlite.foreign.sqlite3
import ksqlite.types.cipher.SqliteMcCipherDescriptor
import java.lang.foreign.MemorySegment

public actual class CipherDescriptor internal actual constructor(
    name: String,
    internal val callbacks: CipherCallbacks<*>
) : CloseableStruct(ksqlite_cipher_descriptor.layout(), null, Application),
    SqliteMcCipherDescriptor {

    init {
        ksqlite_cipher_descriptor.m_name(pointer, sqlite3_mprintf(name))
        ksqlite_cipher_descriptor.m_freeCipher(pointer, CipherFreeCipherHandler)
        ksqlite_cipher_descriptor.m_cloneCipher(pointer, CipherCloneCipherHandler)
        ksqlite_cipher_descriptor.m_getLegacy(pointer, CipherGetLegacyHandler)
        ksqlite_cipher_descriptor.m_getPageSize(pointer, CipherGetPageSizeHandler)
        ksqlite_cipher_descriptor.m_getReserved(pointer, CipherGetReservedHandler)
        ksqlite_cipher_descriptor.m_getSalt(pointer, CipherGetSaltHandler)
        ksqlite_cipher_descriptor.m_generateKey(pointer, CipherGenerateKeyHandler)
        ksqlite_cipher_descriptor.m_encryptPage(pointer, CipherEncryptPageHandler)
        ksqlite_cipher_descriptor.m_decryptPage(pointer, CipherDecryptPageHandler)
    }

    actual override val m_name: String
        get() = ksqlite_cipher_descriptor.m_name(pointer).toKStringFromUtf8()

    internal fun setAllocator(function: MemorySegment?) {
        ksqlite_cipher_descriptor.m_allocateCipher(pointer, function.notNull)
    }

    override fun close() {
        sqlite3.sqlite3_free(ksqlite_cipher_params.m_name(pointer))
        super.close()
    }
}