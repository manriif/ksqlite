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

import ksqlite.types.cipher.SqliteMcCipher
import ksqlite.types.cipher.SqliteMcCodecType
import ksqlite.types.cipher.SqliteMcConfigParamPrefix

/**
 * Reads and writes SQLite Multiple Ciphers configuration.
 *
 * Every parameter here can be read or written as either transient, only affecting the current
 * connection until it is closed, or permanent, becoming the new default for connections opened
 * afterward. Reads default to transient and writes default to permanent. Pass the
 * [SqliteMcConfigParamPrefix] that matches what is actually wanted to override either default.
 */
public interface CipherConfiguration {

    /**
     * Returns the readable and writable parameters for [cipher].
     */
    public fun <Cipher : SqliteMcCipher> parameters(cipher: Cipher): CipherParameters<Cipher>

    /**
     * Returns the cipher used to encrypt the database.
     *
     * @throws ksqlite.kapi.SQLiteException if reading the value fails.
     */
    public fun getCipher(prefix: SqliteMcConfigParamPrefix = None): SqliteMcCipher

    /**
     * Sets the cipher used to encrypt the database. See [SqliteMcCodecType] for the builtin
     * ciphers.
     *
     * [configure], if supplied, runs against the newly set cipher's own parameters.
     *
     * @throws ksqlite.kapi.SQLiteException if writing the value fails.
     */
    public fun <Cipher : SqliteMcCipher> setCipher(
        cipher: Cipher,
        prefix: SqliteMcConfigParamPrefix.ReadWrite = Default,
        configure: (CipherParameters<Cipher>.() -> Unit)? = null
    )

    /**
     * Returns whether the HMAC is validated on read operations, for cipher schemes that use one.
     *
     * @throws ksqlite.kapi.SQLiteException if reading the value fails.
     */
    public fun isHmacCheckEnabled(prefix: SqliteMcConfigParamPrefix = None): Boolean

    /**
     * Sets whether the HMAC is validated on read operations, for cipher schemes that use one.
     *
     * @throws ksqlite.kapi.SQLiteException if writing the value fails.
     */
    public fun setHmacCheckEnabled(
        enabled: Boolean,
        prefix: SqliteMcConfigParamPrefix.ReadWrite = Default
    )

    /**
     * Returns whether the legacy mode for WAL journal encryption is used.
     *
     * @throws ksqlite.kapi.SQLiteException if reading the value fails.
     */
    public fun isLegacyWalEnabled(prefix: SqliteMcConfigParamPrefix = None): Boolean

    /**
     * Sets whether the legacy mode for WAL journal encryption is used.
     *
     * @throws ksqlite.kapi.SQLiteException if writing the value fails.
     */
    public fun setLegacyWalEnabled(
        enabled: Boolean,
        prefix: SqliteMcConfigParamPrefix.ReadWrite = Default
    )
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Cipher used to encrypt the database for the current connection only.
 *
 * @throws ksqlite.kapi.SQLiteException if reading the value fails.
 */
public val CipherConfiguration.cipher: SqliteMcCipher
    get() = getCipher(None)

/**
 * Whether the HMAC is validated on read operations for the current connection only, for cipher
 * schemes that use one.
 *
 * @throws ksqlite.kapi.SQLiteException if reading the value fails.
 */
public val CipherConfiguration.isHmacCheckEnabled: Boolean
    get() = isHmacCheckEnabled(None)

/**
 * Whether the legacy mode for WAL journal encryption is used for the current connection only.
 *
 * @throws ksqlite.kapi.SQLiteException if reading the value fails.
 */
public val CipherConfiguration.isLegacyWalEnabled: Boolean
    get() = isLegacyWalEnabled(None)

/**
 * Returns the readable and writable parameters for the dynamic cipher named after [name].
 */
public fun CipherConfiguration.parameters(name: String): CipherParameters<SqliteMcCipher.Dynamic> =
    parameters(SqliteMcCipher.Dynamic(name))

/**
 * Sets the dynamic cipher to be used for encrypting the database, using its [name].
 */
public fun CipherConfiguration.setCipher(
    name: String,
    prefix: SqliteMcConfigParamPrefix.ReadWrite = Default,
    configure: (CipherParameters<SqliteMcCipher.Dynamic>.() -> Unit)? = null
) {
    setCipher(
        cipher = SqliteMcCipher.Dynamic(name),
        prefix = prefix,
        configure = configure
    )
}