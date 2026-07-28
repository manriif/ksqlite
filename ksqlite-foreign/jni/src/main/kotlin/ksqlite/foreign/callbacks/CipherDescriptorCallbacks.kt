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
package ksqlite.foreign.callbacks

import ksqlite.foreign.JniPointer

/**
 * Regroups all the callbacks of the `ksqlite_cipher_descriptor` interface.
 * All the functions are invoked from JNI.
 */
public interface CipherDescriptorCallbacks<Cipher : Any> {

    public fun allocateCipher(
        db: JniPointer,
        index: Int
    ): Cipher?

    public fun freeCipher(cipher: Cipher)

    public fun cloneCipher(cipherTo: Cipher, cipherFrom: Cipher)

    public fun getLegacy(cipher: Cipher): Int

    public fun getPageSize(cipher: Cipher): Int

    public fun getReserved(cipher: Cipher): Int

    public fun getSalt(cipher: Cipher): JniPointer

    public fun generateKey(
        cipher: Cipher,
        userPassword: ByteArray,
        rekey: Int,
        cipherSalt: Long
    )

    public fun encryptPage(
        cipher: Cipher,
        page: Int,
        data: JniPointer,
        dataLength: Int,
        reserved: Int
    ): Int

    public fun decryptPage(
        cipher: Cipher,
        page: Int,
        data: JniPointer,
        dataLength: Int,
        reserved: Int,
        hmacCheck: Int
    ): Int
}