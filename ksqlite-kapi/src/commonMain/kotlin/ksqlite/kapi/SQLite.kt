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
package ksqlite.kapi

import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.cipher.CipherManager
import ksqlite.kapi.config.AnyTimeConfiguration
import ksqlite.kapi.config.ConfigurationScope
import ksqlite.kapi.connection.AutoExtension
import ksqlite.kapi.connection.DatabaseConnection
import ksqlite.kapi.value.Status
import ksqlite.kapi.vfs.VirtualFileSystemManager
import ksqlite.types.SqliteOpenFlag
import ksqlite.types.SqliteStatusOption

/**
 * [SQLite](https://sqlite.org/docs.html) entry point.
 *
 * Only one [SQLite] instance can exist at a time, see [initialize]. Unless documented otherwise,
 * every member of this interface throws [IllegalStateException] once this instance is closed.
 */
public interface SQLite : AutoCloseable {

    /**
     * Configuration options that can be read or changed at any point during this instance's
     * lifetime, as opposed to the ones only settable through [initialize]'s `configure` block.
     */
    public val config: AnyTimeConfiguration

    /**
     * Registers and configures ciphers used to encrypt database connections.
     */
    public val ciphers: CipherManager

    /**
     * Looks up and registers virtual file systems.
     */
    public val virtualFileSystems: VirtualFileSystemManager

    /**
     * Hard limit on the amount of heap memory SQLite may allocate.
     *
     * Reading this returns the current limit, `-1` meaning no limit is set. Writing it changes the
     * limit and returns immediately, it does not free memory already allocated.
     *
     * @throws SQLiteException if setting the value fails.
     */
    public var hardHeapLimit: Long

    /**
     * Number of bytes of memory currently allocated by SQLite.
     */
    public val memoryUsed: Long

    /**
     * Highest value [memoryUsed] has reached since the high-water mark was last reset, either
     * through [getMemoryStatus] or [getStatus].
     */
    public val memoryHighwater: Long

    /**
     * Soft limit on the amount of heap memory SQLite may allocate.
     *
     * Unlike [hardHeapLimit], SQLite may temporarily exceed this limit rather than fail an
     * operation.
     *
     * @throws SQLiteException if setting the value fails.
     */
    public var softHeapLimit: Long

    /**
     * Registers [autoExtension] so that it runs against every database connection opened from now
     * on, including ones already open. Has no effect if [autoExtension] is already registered.
     */
    public fun addAutoExtension(autoExtension: AutoExtension)

    /**
     * Unregisters [autoExtension]. Has no effect if it was not registered.
     */
    public fun removeAutoExtension(autoExtension: AutoExtension)

    /**
     * Unregisters every [AutoExtension] previously registered.
     */
    public fun clearAutoExtensions()

    /**
     * Returns [memoryUsed] and [memoryHighwater], resetting the high-water mark afterward if
     * [reset] is `true`.
     */
    public fun getMemoryStatus(reset: Boolean): Status

    /**
     * Opens the SQLite database at [fileName] and returns the resulting [DatabaseConnection].
     *
     * [fileName] can be a path, a `file:` URI if [SqliteOpenFlag.URI] is set in [flags], or
     * `:memory:` for a private, temporary in-memory database. [vfs] selects the virtual file
     * system to open it with, or the default one if `null`.
     *
     * @throws SQLiteException if opening the database fails or a registered [AutoExtension] fails.
     */
    public fun open(
        fileName: String,
        flags: SqliteOpenFlag.Db = SqliteOpenFlag.READWRITE or SqliteOpenFlag.CREATE,
        vfs: String? = null
    ): DatabaseConnection

    /**
     * Fills [output] with [size] bytes of randomness. [output] must hold at least [size] bytes,
     * writing beyond its capacity is undefined behavior.
     */
    public fun generateRandomBytes(
        output: Buffer,
        size: Int
    )

    /**
     * Attempts to free up to [size] bytes of heap memory by discarding non-essential memory SQLite
     * is holding onto, and returns the number of bytes actually freed, which may be less than
     * [size].
     */
    public fun releaseMemory(size: Int): Int

    /**
     * Returns the current value and high-water mark for [option], resetting the high-water mark
     * afterward if [reset] is `true`.
     */
    public fun getStatus(
        option: SqliteStatusOption,
        reset: Boolean = false
    ): Status

    /**
     * Closes this instance, releasing every global resource SQLite holds. Calling this again on an
     * already closed instance has no effect.
     *
     * Any statement, transaction or [DatabaseConnection] still open at that point should be closed
     * first, closing this instance does not do it for the caller.
     *
     * @throws SQLiteException if an error happens while shutting down SQLite.
     */
    override fun close()

    ///////////////////////////////////////////////////////////////////////////
    // Companion
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Provides access to SQLite APIs that do not require SQLite initialization.
     */
    public companion object : SQLiteStatic by SQLiteStaticImpl {

        /**
         * Initializes SQLite and returns an [SQLite] instance used to initiate connections.
         * SQLite global options can be configured by supplying a value to [configure].
         *
         * When the returned instance is no longer needed, then [SQLite.close] must be called and this
         * factory method can be called again.
         *
         * Only a single instance of [SQLite] exists at a time.
         *
         * @throws IllegalStateException if a previously returned instance of [SQLite] was not closed.
         * @throws SQLiteException if an operation fails while creating and configuring [SQLite].
         */
        public fun initialize(configure: (ConfigurationScope.() -> Unit)? = null): SQLite =
            sqliteInitialize(configure)
    }
}