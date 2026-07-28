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