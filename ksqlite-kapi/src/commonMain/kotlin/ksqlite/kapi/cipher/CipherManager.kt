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

/**
 * Registers and looks up SQLite Multiple Ciphers [dynamic ciphers](https://utelle.github.io/SQLite3MultipleCiphers/docs/configuration/config_capi/#function-sqlite3mc_register_cipher),
 * on top of the ciphers SQLite Multiple Ciphers already provides.
 *
 * Unless documented otherwise, every member throws [IllegalStateException] once the owning
 * [ksqlite.kapi.SQLite] instance is closed.
 */
public interface CipherManager {

    /**
     * Configuration shared by every connection.
     */
    public val config: CipherConfiguration

    /**
     * Creates and destroys virtual file systems wrapped for encryption.
     */
    public val virtualFileSystems: CipherVirtualFileSystemManager

    /**
     * Number of currently registered ciphers, builtin and dynamic.
     */
    public val count: Int

    /**
     * Returns the 1-based index of [cipher] among the registered ciphers. See [SqliteMcCodecType]
     * for the builtin ciphers.
     *
     * @throws ksqlite.kapi.SQLiteException if [cipher] is not registered.
     */
    public fun getIndex(cipher: SqliteMcCipher): Int

    /**
     * Returns the name of the cipher at the 1-based [index].
     *
     * @throws ksqlite.kapi.SQLiteException if there is no cipher at [index].
     */
    public fun getName(index: Int): String

    /**
     * Registers a dynamic cipher under [name], created through [factory] whenever a connection
     * actually derives a key with it.
     *
     * [makeDefault] sets it as the default cipher for connections that do not select one
     * explicitly.
     *
     * @throws ksqlite.kapi.SQLiteException if the name is invalid or registration fails.
     */
    public fun <Cipher : DynamicCipher> register(
        name: String,
        factory: DynamicCipher.Factory<Cipher>,
        makeDefault: Boolean = true
    )
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the 1-based index of the dynamic cipher named [name] among the registered ciphers.
 *
 * @throws ksqlite.kapi.SQLiteException if it is not registered.
 */
public fun CipherManager.getIndex(name: String): Int =
    getIndex(SqliteMcCipher.Dynamic(name))