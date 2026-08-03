package ksqlite.kapi.cipher

import ksqlite.types.cipher.SqliteMcCipher
import ksqlite.types.cipher.SqliteMcCodecType
import ksqlite.types.cipher.SqliteMcConfigParamPrefix

/**
 * Exposes SQLite Multiple Cipher configuration APIs.
 *
 * Note regarding default values:
 * 
 * - Transient configuration parameters values are returned by default.
 * - New parameter values are set permanently by default.
 * 
 * These behaviors can be adjusted by supplying the [SqliteMcConfigParamPrefix] that best match 
 * the request. 
 */
public interface CipherConfiguration {

    /**
     * Returns the readable and writable parameters for the given [cipher].
     */
    public fun <Cipher : SqliteMcCipher> parameters(cipher: Cipher): CipherParameters<Cipher>

    /**
     * Returns the cipher to be used for encrypting the database.
     *
     * @throws CipherException if an error occurred while reading the value.
     */
    public fun getCipher(prefix: SqliteMcConfigParamPrefix = None): SqliteMcCipher

    /**
     * Sets the cipher to be used for encrypting the database.
     * See [SqliteMcCodecType] for the builtin ciphers.
     *
     * The newly set cipher can be configured within [configure].
     *
     * @throws CipherException if an error occurred while writing the value.
     */
    public fun <Cipher : SqliteMcCipher> setCipher(
        cipher: Cipher,
        prefix: SqliteMcConfigParamPrefix.ReadWrite = Default,
        configure: (CipherParameters<Cipher>.() -> Unit)? = null
    )

    /**
     * Returns whether the HMAC should be validated on read operations for encryption schemes using 
     * HMACs.
     *
     * @throws CipherException if an error occurred while reading the value.
     */
    public fun isHmacCheckEnabled(prefix: SqliteMcConfigParamPrefix = None): Boolean

    /**
     * Sets whether the HMAC should be validated on read operations for encryption schemes using
     * HMACs.
     *
     * @throws CipherException if an error occurred while writing the value.
     */
    public fun setHmacCheckEnabled(
        enabled: Boolean,
        prefix: SqliteMcConfigParamPrefix.ReadWrite = Default
    )

    /**
     * Returns whether the legacy mode for the WAL journal encryption should be used.
     *
     * @throws CipherException if an error occurred while reading the value.
     */
    public fun isLegacyWalEnabled(prefix: SqliteMcConfigParamPrefix = None): Boolean

    /**
     * Sets whether the legacy mode for the WAL journal encryption should be used.
     * The actual value is returned.
     *
     * @throws CipherException if an error occurred while writing the value.
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
 * Transient cipher to be used for encrypting the database.
 *
 * @throws CipherException if an error occurred while reading the value.
 */
public val CipherConfiguration.cipher: SqliteMcCipher
    get() = getCipher(None)

/**
 * Transient value for whether the HMAC should be validated on read operations for encryption
 * schemes using HMACs.
 *
 * @throws CipherException if an error occurred while reading the value.
 */
public val CipherConfiguration.isHmacCheckEnabled: Boolean
    get() = isHmacCheckEnabled(None)

/**
 * Transient value for whether the legacy mode for the WAL journal encryption should be used.
 *
 * @throws CipherException if an error occurred while reading the value.
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