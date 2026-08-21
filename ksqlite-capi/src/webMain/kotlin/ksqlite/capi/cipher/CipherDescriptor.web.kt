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

import ksqlite.capi.exports
import ksqlite.capi.memory.CloseableStruct
import ksqlite.capi.memory.notNull
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.sqlite3_mprintf
import ksqlite.foreign.structs.ksqlite_cipher_descriptor
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.types.cipher.SqliteMcCipherDescriptor

public actual class CipherDescriptor private constructor(
    internal val callbacks: CipherCallbacks<*>,
    private val descriptor: ksqlite_cipher_descriptor
) : CloseableStruct(descriptor, Application),
    SqliteMcCipherDescriptor {

    internal actual constructor(
        name: String,
        callbacks: CipherCallbacks<*>
    ) : this(callbacks, ksqlite_cipher_descriptor().apply {
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
        get() = descriptor.m_name.toKStringFromUtf8()

    internal fun setAllocator(function: WasmPointer?) {
        descriptor.m_allocateCipher = function.notNull
    }

    override fun close() {
        exports.sqlite3_free(descriptor.m_name)
        super.close()
    }
}