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

import ksqlite.capi.cipher.callbacks.CipherDescriptorAllocateCipherCallback
import ksqlite.capi.cipher.callbacks.CipherDescriptorCloneCipherCallback
import ksqlite.capi.cipher.callbacks.CipherDescriptorDecryptPageCallback
import ksqlite.capi.cipher.callbacks.CipherDescriptorEncryptPageCallback
import ksqlite.capi.cipher.callbacks.CipherDescriptorFreeCipherCallback
import ksqlite.capi.cipher.callbacks.CipherDescriptorGenerateKeyCallback
import ksqlite.capi.cipher.callbacks.CipherDescriptorGetLegacyCallback
import ksqlite.capi.cipher.callbacks.CipherDescriptorGetPageSizeCallback
import ksqlite.capi.cipher.callbacks.CipherDescriptorGetReservedCallback
import ksqlite.capi.cipher.callbacks.CipherDescriptorGetSaltCallback
import ksqlite.capi.memory.ClosableStruct
import ksqlite.types.cipher.SqliteMcCipherDescriptor

/**
 * A cipher descriptor specifies the name of the cipher scheme, and a number of API function
 * pointers.
 *
 * [CipherDescriptor](https://utelle.github.io/SQLite3MultipleCiphers/docs/ciphers/cipher_dynamic/#cipher-descriptor)
 */
public expect class CipherDescriptor internal constructor(
    name: String,
    callbacks: CipherCallbacks<*>
) : ClosableStruct,
    SqliteMcCipherDescriptor {

    override val m_name: String
}

///////////////////////////////////////////////////////////////////////////
// Factory
///////////////////////////////////////////////////////////////////////////

/**
 * Returns an instance of [CipherDescriptor].
 *
 * The caller take the ownership of the returned [CipherDescriptor] and is responsible to release it
 * by invoking [CipherDescriptor.close].
 *
 * @param name The first character must be alphabetic = alpha, all other characters may be
 * alphanumeric or underscore. The name may consist of a maximum of 63 characters.
 *
 * @param saltSize The size of the salt in bytes.
 */
public fun <Cipher : Any> CipherDescriptor(
    name: String,
    saltSize: Long,
    allocate: CipherDescriptorAllocateCipherCallback<Cipher>,
    free: CipherDescriptorFreeCipherCallback<Cipher>,
    clone: CipherDescriptorCloneCipherCallback<Cipher>,
    getLegacy: CipherDescriptorGetLegacyCallback<Cipher>,
    getPageSize: CipherDescriptorGetPageSizeCallback<Cipher>,
    getReserved: CipherDescriptorGetReservedCallback<Cipher>,
    getSalt: CipherDescriptorGetSaltCallback<Cipher>,
    generateKey: CipherDescriptorGenerateKeyCallback<Cipher>,
    encryptPage: CipherDescriptorEncryptPageCallback<Cipher>,
    decryptPage: CipherDescriptorDecryptPageCallback<Cipher>
): CipherDescriptor = CipherDescriptor(
    name = name,
    callbacks = CipherCallbacks(
        saltSize = saltSize,
        allocate = allocate,
        free = free,
        clone = clone,
        getLegacy = getLegacy,
        getPageSize = getPageSize,
        getReserved = getReserved,
        getSalt = getSalt,
        generateKey = generateKey,
        encryptPage = encryptPage,
        decryptPage = decryptPage,
    )
)