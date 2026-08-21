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
@file:Suppress("ClassName", "SpellCheckingInspection")

package ksqlite.capi.cipher

import ksqlite.capi.sqlite3mc_cipher_index
import ksqlite.capi.sqlite3mc_cipher_name
import ksqlite.types.cipher.SqliteMcCipher
import ksqlite.types.cipher.SqliteMcConfigParam
import ksqlite.types.internal.convertMcCipher

/**
 * Parameters supported by
 * [sqlite3mc_config](https://utelle.github.io/SQLite3MultipleCiphers/docs/configuration/config_capi/#function-sqlite3mc_config).
 */
public sealed interface SqliteMcConfig<Value : Any> : SqliteMcConfigParam<Value> {

    /**
     * The cipher to be used for encrypting the database.
     */
    public data object CIPHER : SqliteMcConfig<SqliteMcCipher> {

        override val name: String
            get() = "cipher"

        override fun toInt(value: SqliteMcCipher?): Int =
            sqlite3mc_cipher_index(value ?: return -1)

        override fun toValue(value: Int): SqliteMcCipher? =
            value.takeIf { it > 0 }?.let(::sqlite3mc_cipher_name)?.let(::convertMcCipher)
    }

    /**
     * Boolean flag whether the HMAC should be validated on read operations for encryption schemes
     * using HMACs
     */
    public data object HMAC_CHECK :
        SqliteMcConfig<Int>,
        SqliteMcConfigParam.OfInt("hmac_check")

    /**
     * Boolean flag whether the legacy mode for the WAL journal encryption should be used.
     */
    public data object MC_LEGACY_WAL :
        SqliteMcConfig<Int>,
        SqliteMcConfigParam.OfInt("mc_legacy_wal")
}