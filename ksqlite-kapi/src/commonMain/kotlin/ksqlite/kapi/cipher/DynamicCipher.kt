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

import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.connection.DatabaseConnection

/**
 * A user-implemented [dynamic cipher scheme](https://utelle.github.io/SQLite3MultipleCiphers/docs/ciphers/cipher_dynamic/#-dynamic-cipher-schemes),
 * registered through [CipherManager.register].
 */
public interface DynamicCipher : AutoCloseable {

    /**
     * Whether this cipher is in legacy mode.
     */
    public val isLegacy: Boolean

    /**
     * Actual size of a database page in legacy mode. Returning `0` is enough outside of legacy
     * mode, the page size is then determined automatically.
     */
    public val pageSize: Int

    /**
     * Number of reserved bytes per database page, used to store a HMAC or other data this cipher
     * scheme needs to verify page consistency.
     */
    public val reserved: Int

    /**
     * Salt for this cipher scheme, or `null` if [Factory.saltSize] is `0`.
     */
    public val salt: Buffer?

    /**
     * Derives an encryption key from [userPassword].
     *
     * This function itself must not throw. If key derivation fails, record the failure and throw
     * it from the next call to [encryptPage] or [decryptPage] instead.
     */
    public fun generateKey(
        userPassword: ByteArray,
        rekey: Int,
        cipherSalt: Buffer?
    )

    /**
     * Encrypts a single database page.
     *
     * @throws ksqlite.kapi.SQLiteException if encryption fails.
     */
    public fun encryptPage(
        page: Int,
        data: Buffer,
        reserved: Int
    )

    /**
     * Decrypts a single database page.
     *
     * @throws ksqlite.kapi.SQLiteException if decryption fails.
     */
    public fun decryptPage(
        page: Int,
        data: Buffer,
        reserved: Int,
        hmacCheck: Boolean
    )

    /**
     * Releases the resources this cipher holds.
     */
    override fun close()

    ///////////////////////////////////////////////////////////////////////////
    // Factory
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Creates and configures instances of [Cipher] on demand.
     */
    public interface Factory<Cipher : DynamicCipher> {

        /**
         * Size, in bytes, of this cipher's salt, or `0` if it does not need one.
         */
        public val saltSize: Long

        /**
         * Registers every parameter this cipher accepts. A sentinel entry is appended
         * automatically after the ones registered here.
         */
        public fun DynamicCipherParameterRegistry.registerParameters()

        /**
         * Creates a new [Cipher] instance. Called the first time a connection actually derives a
         * key with this cipher, not merely when it is selected through
         * [ksqlite.kapi.cipher.CipherConfiguration.setCipher].
         *
         * @throws ksqlite.kapi.SQLiteException if creating the cipher fails.
         */
        public fun DynamicCipherCreateScope.create(connection: DatabaseConnection): Cipher

        /**
         * Copies the state of [source] into [target], called when SQLite needs a second,
         * independently-keyed cipher instance derived from the same key, typically when opening
         * another connection to the same database.
         */
        public fun clone(source: Cipher, target: Cipher)
    }
}