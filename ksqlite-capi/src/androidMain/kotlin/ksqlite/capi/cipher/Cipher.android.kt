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

import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.notNull
import ksqlite.capi.sqlite3
import ksqlite.foreign.JniPointer
import ksqlite.foreign.callbacks.CipherDescriptorCallbacks

internal val CipherAllocateCipherHandlers = createCipherAllocators(0, 1, 2, 3)

internal object CipherDescriptorHandler : CipherDescriptorCallbacks<CipherWrapper<*>> {

    override fun allocateCipher(
        db: JniPointer,
        index: Int
    ): CipherWrapper<*>? = cipherAllocate(sqlite3(db), index)

    override fun freeCipher(cipher: CipherWrapper<*>) = cipher.free()

    override fun cloneCipher(
        cipherTo: CipherWrapper<*>,
        cipherFrom: CipherWrapper<*>
    ) = cipherTo.clone(cipherFrom)

    override fun getLegacy(cipher: CipherWrapper<*>): Int = cipher.getLegacy()

    override fun getPageSize(cipher: CipherWrapper<*>): Int = cipher.getPageSize()

    override fun getReserved(cipher: CipherWrapper<*>): Int = cipher.getReserved()

    override fun getSalt(cipher: CipherWrapper<*>): JniPointer = cipher.getSalt()?.pointer.notNull

    override fun generateKey(
        cipher: CipherWrapper<*>,
        userPassword: ByteArray,
        rekey: Int,
        cipherSalt: Long
    ) = cipher.run {
        generateKey(
            userPassword = userPassword,
            rekey = rekey,
            cipherSalt = Buffer.from(cipherSalt, saltSize)
        )
    }

    override fun encryptPage(
        cipher: CipherWrapper<*>,
        page: Int,
        data: JniPointer,
        dataLength: Int,
        reserved: Int
    ): Int = cipher.encryptPage(
        page = page,
        data = Buffer.from(data, dataLength.toLong())!!,
        reserved = reserved
    )

    override fun decryptPage(
        cipher: CipherWrapper<*>,
        page: Int,
        data: JniPointer,
        dataLength: Int,
        reserved: Int,
        hmacCheck: Int
    ): Int = cipher.decryptPage(
        page = page,
        data = Buffer.from(data, dataLength.toLong())!!,
        reserved = reserved,
        hmacCheck = hmacCheck
    )
}