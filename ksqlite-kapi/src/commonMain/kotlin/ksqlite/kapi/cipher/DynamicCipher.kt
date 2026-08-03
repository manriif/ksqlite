package ksqlite.kapi.cipher

import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.database.DatabaseConnection

/**
 * Represents a [dynamic cipher scheme](https://utelle.github.io/SQLite3MultipleCiphers/docs/ciphers/cipher_dynamic/#-dynamic-cipher-schemes)
 */
public interface DynamicCipher : AutoCloseable {

    /**
     * Whether this cipher is in legacy mode.
     */
    public val isLegacy: Boolean

    /**
     * Actual size of a database page in legacy mode. For non-legacy mode returning a value of 0 is
     * sufficient, because the page size will be determined automatically.
     */
    public val pageSize: Int

    /**
     * Number of reserved bytes per database page. The reserved bytes are used to store a HMAC (or
     * other data) used by the cipher scheme to verify database page consistency.
     */
    public val reserved: Int

    /**
     * Bytes used as the salt of the cipher scheme.
     */
    public val salt: Buffer?

    /**
     * Derives an encryption key from the given user passphrase.
     *
     * If an error happens here, it must be thrown on call to [encryptPage] or [decryptPage].
     */
    public fun generateKey(
        userPassword: ByteArray,
        rekey: Int,
        cipherSalt: Buffer?
    )

    /**
     * Encrypts a single database page.
     *
     * @throws ksqlite.kapi.SQLiteException if an error occurred.
     */
    public fun encryptPage(
        page: Int,
        data: Buffer,
        reserved: Int
    )

    /**
     * Decrypts a single database page.
     *
     * @throws ksqlite.kapi.SQLiteException if an error occurred.
     */
    public fun decryptPage(
        page: Int,
        data: Buffer,
        reserved: Int,
        hmacCheck: Boolean
    )

    /**
     * Releases this [DynamicCipher] resources.
     */
    override fun close()

    ///////////////////////////////////////////////////////////////////////////
    // Factory
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Provides an API to create and configure dynamic [DynamicCipher].
     */
    public interface Factory<Cipher : DynamicCipher> {

        /**
         * Returns the size, in bytes, of the cipher salt, or zero if no salt is required for the
         * cipher.
         */
        public val saltSize: Long

        /**
         * Registers all the cipher parameters required by this cipher.
         * A sentinel entry is automatically added after all the declared parameters.
         */
        public fun DynamicCipherParameterRegistry.registerParameters()

        /**
         * Creates a new [DynamicCipher] instance.
         *
         * @throws CipherException if an error happened while creating the [Cipher].
         */
        public fun DynamicCipherCreateScope.create(connection: DatabaseConnection): Cipher

        /**
         * Clones [source] cipher into [target] cipher.
         */
        public fun clone(source: Cipher, target: Cipher)
    }
}