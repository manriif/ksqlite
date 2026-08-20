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
package ksqlite.kapi.cipher

import ksqlite.capi.sqlite3_randomness
import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.buffer.readBytes
import ksqlite.kapi.connection.DatabaseConnection

internal const val XOR_DYNAMIC_CIPHER_SALT_LENGTH = 16L

/**
 * Tracks which [DynamicCipher.Factory]/[DynamicCipher] hooks have actually been invoked by
 * SQLite Multiple Ciphers for a given registered cipher.
 */
internal class XorDynamicCipherTracking {
    var created = false
    var closed = false
    var cloned = false
    var generateKeyCalled = false
    var encryptPageCalled = false
    var decryptPageCalled = false
    var parameterValueAtCreate: Int? = null
}

/**
 * Trivial single-byte XOR "cipher": it exists only to prove that every hook of [DynamicCipher] is
 * actually wired up and invoked, not to provide any real confidentiality.
 */
internal class XorDynamicCipher(
    private val tracking: XorDynamicCipherTracking,
    override val salt: Buffer
) : DynamicCipher {

    private var keyByte: Byte = 0

    override val isLegacy: Boolean = false
    override val pageSize: Int = 0
    override val reserved: Int = 0

    override fun generateKey(userPassword: ByteArray, rekey: Int, cipherSalt: Buffer?) {
        tracking.generateKeyCalled = true

        // Reopening an existing database (or rekeying it): adopt the salt SQLite Multiple Ciphers
        // read back from the file header. Creating a new one: generate a fresh random salt so it
        // gets persisted to the header via the getSalt hook -- a fresh, unseeded salt buffer per
        // instance (see Factory.create) would otherwise never match on a later reopen.
        if (cipherSalt != null) {
            salt.write(cipherSalt.readBytes(), XOR_DYNAMIC_CIPHER_SALT_LENGTH.toInt())
        } else {
            sqlite3_randomness(XOR_DYNAMIC_CIPHER_SALT_LENGTH.toInt(), salt.buffer)
        }

        var derived = 0

        for (byte in userPassword) {
            derived = derived xor byte.toInt()
        }

        keyByte = derived.toByte()
    }

    private fun xor(data: Buffer) {
        val bytes = data.readBytes()

        for (index in bytes.indices) {
            bytes[index] = (bytes[index].toInt() xor keyByte.toInt()).toByte()
        }

        data.write(bytes, bytes.size)
    }

    override fun encryptPage(page: Int, data: Buffer, reserved: Int) {
        tracking.encryptPageCalled = true
        xor(data)
    }

    override fun decryptPage(page: Int, data: Buffer, reserved: Int, hmacCheck: Boolean) {
        tracking.decryptPageCalled = true
        xor(data)
    }

    override fun close() {
        tracking.closed = true
        salt.close()
    }

    class Factory(private val tracking: XorDynamicCipherTracking) :
        DynamicCipher.Factory<XorDynamicCipher> {

        override val saltSize: Long = XOR_DYNAMIC_CIPHER_SALT_LENGTH

        override fun DynamicCipherParameterRegistry.registerParameters() {
            register {
                m_name = "test_param"
                m_value = 42
                m_default = 42
                m_minValue = 0
                m_maxValue = 100
            }
        }

        override fun DynamicCipherCreateScope.create(connection: DatabaseConnection): XorDynamicCipher {
            tracking.created = true
            tracking.parameterValueAtCreate = getParameter("test_param")
            return XorDynamicCipher(tracking, Buffer.allocate(XOR_DYNAMIC_CIPHER_SALT_LENGTH))
        }

        override fun clone(source: XorDynamicCipher, target: XorDynamicCipher) {
            tracking.cloned = true
            target.keyByte = source.keyByte
            target.salt.write(source.salt.readBytes(), XOR_DYNAMIC_CIPHER_SALT_LENGTH.toInt())
        }
    }
}
