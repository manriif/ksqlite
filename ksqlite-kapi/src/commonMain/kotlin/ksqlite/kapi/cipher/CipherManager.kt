package ksqlite.kapi.cipher

import ksqlite.types.cipher.SqliteMcCipher
import ksqlite.types.cipher.SqliteMcCodecType

/**
 * Exposes SQLite Multiple Cipher global APIs covering dynamic cipher registration and virtual file
 * system related operations.
 *
 * [Register cipher][https://utelle.github.io/SQLite3MultipleCiphers/docs/configuration/config_capi/#function-sqlite3mc_register_cipher]
 */
public interface CipherManager {

    /**
     * Configuration operating on all the connections.
     */
    public val config: CipherConfiguration

    /**
     * Manager for the cipher wrapped virtual file systems.
     */
    public val virtualFileSystems: CipherVirtualFileSystemManager

    /**
     * Number of currently registered cipher schemes.
     */
    public val count: Int

    /**
     * Returns the relative 1-based index of the given [cipher].
     * See [SqliteMcCodecType] for the builtin ciphers.
     *
     * @throws CipherException if the cipher is not registered.
     */
    public fun getIndex(cipher: SqliteMcCipher): Int

    /**
     * Returns the name if the cipher at the relative 1-based [index].
     *
     * @throws CipherException if there is no cipher for the given index.
     */
    public fun getName(index: Int): String

    /**
     * Registers a dynamic cipher, supplied by [factory], under the given [name].
     *
     * If [makeDefault] is `true`, which is the default behavior, then the cipher is set as the
     * default one.
     *
     * @throws ksqlite.kapi.SQLiteException if the cipher registration fails.
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
 * Returns the relative 1-based index of the dynamic cipher named after [name].
 *
 * @throws CipherException if the cipher is not registered.
 */
public fun CipherManager.getIndex(name: String): Int =
    getIndex(SqliteMcCipher.Dynamic(name))