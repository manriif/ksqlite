package ksqlite.kapi

import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.buffer.readBytes
import ksqlite.kapi.config.AnyTimeConfiguration
import ksqlite.kapi.config.ConfigurationScope
import ksqlite.kapi.database.AutoExtension
import ksqlite.kapi.database.DatabaseConnection
import ksqlite.kapi.value.StatusValue
import ksqlite.types.SqliteOpenFlag
import ksqlite.types.SqliteStatusOption

/**
 * [SQLite](https://sqlite.org/docs.html) entry point.
 */
public interface SQLite : AutoCloseable {

    /**
     * Configuration exposing options that can be accessed at anytime.
     */
    public val config: AnyTimeConfiguration

    /**
     * Hard limit on the amount of heap memory that may be allocated by SQLite.
     *
     * @throws SQLiteException if setting the value failed.
     */
    public var hardHeapLimit: Long

    /**
     * Number of bytes of memory currently outstanding.
     */
    public val memoryUsed: Long

    /**
     * Maximum value of [memoryUsed] since the high-water mark was last reset.
     */
    public val memoryHighwater: Long

    /**
     * Soft limit on the amount of heap memory that may be allocated by SQLite.
     *
     * @throws SQLiteException if setting the value failed.
     */
    public var softHeapLimit: Long

    /**
     * Registers the [autoExtension] callback.
     * This method has no effect if the [autoExtension] is already registered.
     */
    public fun addAutoExtension(autoExtension: AutoExtension)

    /**
     * Unregisters the [autoExtension] callback.
     * This method has no effect if the [autoExtension] is not registered.
     */
    public fun removeAutoExtension(autoExtension: AutoExtension)

    /**
     * Unregisters all the [AutoExtension] previously registered.
     */
    public fun clearAutoExtensions()

    /**
     * Returns the number of bytes of memory currently outstanding and the highwater mark.
     */
    public fun getMemoryStatus(reset: Boolean): StatusValue

    /**
     * Opens an SQLite database file as specified by [fileName] and returns a [DatabaseConnection].
     *
     * @throws SQLiteException if an error happens while opening the database or if a registered
     * [AutoExtension] fails.
     */
    public fun open(
        fileName: String,
        flags: SqliteOpenFlag.Db = SqliteOpenFlag.READONLY,
        vfs: String? = null
    ): DatabaseConnection

    /**
     * Stores [size] bytes of randomness into [output].
     */
    public fun generateRandomBytes(
        output: Buffer,
        size: Int
    )

    /**
     * Attempts to free [size] bytes of heap memory by deallocating non-essential memory allocations
     * held by the database library.
     */
    public fun releaseMemory(size: Int): Int

    /**
     * Returns the status for the given options.
     */
    public fun getStatus(
        option: SqliteStatusOption,
        reset: Boolean = false
    ): StatusValue

    /**
     * Invokes `sqlite3_shutdown()` and resets global SQLite state.
     *
     * It is recommended to terminate any active statement, transaction and opened database
     * connection first before closing `this` [SQLite] instance.
     *
     * @throws SQLiteException if error happens while shutting down SQLite.
     */
    override fun close()

    ///////////////////////////////////////////////////////////////////////////
    // Companion
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Provides access to SQLite APIs that do not require SQLite initialization.
     */
    public companion object :
        SQLiteLoader by sqliteLoader(),
        SQLiteStatic by SQLiteStaticImpl
}

///////////////////////////////////////////////////////////////////////////
// Factory
///////////////////////////////////////////////////////////////////////////

/**
 * Initializes SQLite and returns an [SQLite] instance used to initiate connections.
 * SQLite options can be configured by supplying a value to [configure].
 *
 * When the returned instance will no longer be needed, [SQLite.close] must be called and this
 * method can be called again.
 *
 * Only a single instance of [SQLite] exists at a time and an [IllegalStateException] is thrown if
 * a previously returned instance of [SQLite] was not closed.
 *
 * @throws SQLiteException if an operation fails while creating and configuring [SQLite].
 */
public fun SQLite(configure: (ConfigurationScope.() -> Unit)? = null): SQLite =
    sqliteInitialize(configure)

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a [ByteArray] of given [size] filled with random bytes.
 */
public fun SQLite.generateRandomBytes(size: Int): ByteArray {
    val output = Buffer.allocate(size)
    generateRandomBytes(output, size)
    return output.readBytes()
}