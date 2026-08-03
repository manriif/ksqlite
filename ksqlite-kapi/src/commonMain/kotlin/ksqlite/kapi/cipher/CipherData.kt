package ksqlite.kapi.cipher

import ksqlite.kapi.buffer.Buffer

/**
 * Exposes the [cipher data](https://utelle.github.io/SQLite3MultipleCiphers/docs/configuration/config_capi/#function-sqlite3mc_codec_data).
 *
 * Note that, unless specified, it is the caller responsibility to close the returned [Buffer]s.
 */
public interface CipherData {

    /**
     * Returns the random cipher salt used for key derivation and stored in the database header (as
     * a hexadecimal encoded string, 32 bytes).
     */
    public fun cipherSalt(database: String? = null): Buffer?

    /**
     * Returns the random cipher salt used for key derivation and stored in the database header (as
     * a raw binary string, 16 bytes).
     */
    public fun cipherSaltRaw(database: String? = null): Buffer?
}