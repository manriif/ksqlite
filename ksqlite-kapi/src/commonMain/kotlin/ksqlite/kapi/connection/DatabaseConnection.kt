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
package ksqlite.kapi.connection

import ksqlite.kapi.blob.Blob
import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.cipher.CipherConfiguration
import ksqlite.kapi.cipher.CipherData
import ksqlite.kapi.function.AggregateFunction
import ksqlite.kapi.function.ScalarFunction
import ksqlite.kapi.function.WindowFunction
import ksqlite.kapi.snapshot.Snapshot
import ksqlite.kapi.statement.PreparedStatement
import ksqlite.kapi.value.Status
import ksqlite.kapi.vfs.FileName
import ksqlite.kapi.vtab.VirtualTableModule
import ksqlite.types.SqliteBlobOpenFlag
import ksqlite.types.SqliteDbStatusOption
import ksqlite.types.SqliteDeserializeFlag
import ksqlite.types.SqliteFunctionTextEncoding
import ksqlite.types.SqlitePrepareFlag
import ksqlite.types.SqliteRuntimeLimit
import ksqlite.types.SqliteSerializeFlag
import ksqlite.types.SqliteTextEncoding
import ksqlite.types.SqliteTraceEventCode
import ksqlite.types.SqliteTransactionState
import ksqlite.types.vtab.SqliteModuleVersion

/**
 * A [database connection](https://sqlite.org/c3ref/sqlite3.html), opened through
 * [ksqlite.kapi.SQLite.open].
 *
 * Unless documented otherwise, every member throws [IllegalStateException] once this connection
 * is closed.
 */
public sealed interface DatabaseConnection : AutoCloseable {

    /**
     * Configuration of the connection.
     */
    public val config: DatabaseConnectionConfiguration

    /**
     * Cipher configuration operating on this connection.
     */
    public val cipherConfig: CipherConfiguration

    /**
     * Cipher data for this connection.
     */
    public val cipherData: CipherData

    /**
     * Number of rows modified, inserted or deleted by the most recently completed INSERT, UPDATE or
     * DELETE statement.
     */
    public val changes: Long

    /**
     * Most recent error information.
     */
    public val lastError: LastError

    /**
     * Low-level file control operations on the underlying VFS.
     */
    public val fileControl: FileControl

    /**
     * Whether the connection is in autocommit mode.
     */
    public val isAutocommit: Boolean

    /**
     * Whether an interrupt is currently in effect.
     */
    public val isInterrupted: Boolean

    /**
     * Rowid of the most recent successful INSERT into a rowid table or virtual table on this
     * connection.
     */
    public var lastInsertRowid: Long

    /**
     * Total number of rows inserted, modified or deleted by all INSERT, UPDATE or DELETE statements
     * completed since the database connection was opened, including those executed as part of
     * trigger programs.
     */
    public val totalChanges: Long

    /**
     * Write-Ahead Log operating on this connection.
     */
    public val wal: WriteAheadLog

    /**
     * Sets the callback that is invoked prior to each autovacuum of the database file.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the callback fails.
     */
    public fun setAutovacuumPages(handler: AutovacuumPages?)

    /**
     * Opens the [Blob] for incremental I/O on column [columnName] of the row [rowid] in table
     * [tableName], in [database]. [flags] controls whether the blob is opened for writing.
     *
     * @throws ksqlite.kapi.SQLiteException if opening the blob fails.
     */
    public fun openBlob(
        tableName: String,
        columnName: String,
        rowid: Long,
        database: String = SQLITE_MAIN_DB_NAME,
        flags: SqliteBlobOpenFlag = SqliteBlobOpenFlag.READWRITE
    ): Blob

    /**
     * Sets the callback that might be invoked whenever an attempt is made to access a database
     * table associated with this connection when another thread or process has the table locked.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the handler fails.
     */
    public fun setBusyHandler(handler: BusyHandler?)

    /**
     * Sets a [BusyHandler] that sleeps for a specified amount of time when a table is locked.
     * Any [BusyHandler] previously passed to [setBusyHandler] is replaced.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the timeout fails.
     */
    public fun setBusyTimeout(millis: Int)

    /**
     * Sets the callback invoked whenever an undefined collation sequence is required.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the handler fails.
     */
    public fun setCollationNeeded(handler: CollationNeeded?)

    /**
     * Sets the callback invoked right before a transaction commits.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the handler fails.
     */
    public fun setCommitHook(handler: CommitHook?)

    /**
     * Creates the collation [name], or replaces it if one was already created with that name and
     * [encoding], backed by [collation].
     *
     * @throws ksqlite.kapi.SQLiteException if setting the handler fails.
     */
    @IgnorableReturnValue
    public fun createCollation(
        name: String,
        encoding: SqliteTextEncoding.CreateCollation,
        collation: Collation
    ): Registration

    /**
     * Creates the scalar function `name(argumentCount)`, or replaces it if one was already
     * created with the same name, argument count and encoding, and returns a handle that removes
     * it again once closed.
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while creating the function.
     */
    @IgnorableReturnValue
    public fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: SqliteFunctionTextEncoding,
        function: ScalarFunction
    ): Registration

    /**
     * Creates the aggregate function `name(argumentCount)`, or replaces it if one was already
     * created with the same name, argument count and encoding, and returns a handle that removes
     * it again once closed.
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while creating the function.
     */
    @IgnorableReturnValue
    public fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: SqliteFunctionTextEncoding,
        function: AggregateFunction
    ): Registration

    /**
     * Creates the window function `name(argumentCount)`, or replaces it if one was already
     * created with the same name, argument count and encoding, and returns a handle that removes
     * it again once closed.
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while creating the function.
     */
    @IgnorableReturnValue
    public fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: SqliteFunctionTextEncoding,
        function: WindowFunction
    ): Registration

    /**
     * Creates the virtual table module [name], or replaces it if one was already created with
     * that name, and returns a handle that removes it again once closed. [module] must implement
     * its `create` and `connect` functions differently, since SQLite only creates the backing
     * table once but connects to it every time it is referenced.
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while creating the module.
     */
    @IgnorableReturnValue
    public fun createModule(
        name: String,
        version: SqliteModuleVersion = SqliteModuleVersion.VERSION_4,
        module: VirtualTableModule.Regular
    ): Registration

    /**
     * Creates the virtual table module [name], or replaces it if one was already created with
     * that name, and returns a handle that removes it again once closed. The table [module]
     * connects to can either be referenced directly by [name] in SQL, with no
     * `CREATE VIRTUAL TABLE` needed, or explicitly created like a [VirtualTableModule.Regular]
     * one.
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while creating the module.
     */
    @IgnorableReturnValue
    public fun createModule(
        name: String,
        version: SqliteModuleVersion = SqliteModuleVersion.VERSION_4,
        module: VirtualTableModule.Eponymous
    ): Registration

    /**
     * Creates the virtual table module [name], or replaces it if one was already created with
     * that name, and returns a handle that removes it again once closed. The table [module]
     * connects to can only be referenced directly by [name] in SQL, `CREATE VIRTUAL TABLE` is not
     * supported for it.
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while creating the module.
     */
    @IgnorableReturnValue
    public fun createModule(
        name: String,
        version: SqliteModuleVersion = SqliteModuleVersion.VERSION_4,
        module: VirtualTableModule.EponymousOnly
    ): Registration

    /**
     * Writes any dirty pages in the pager cache that are not currently in use. The operation only
     * takes place if there is a write transaction open at the time this function is called.
     *
     * @throws ksqlite.kapi.SQLiteException if the operation fails.
     */
    public fun flushCache()

    /**
     * Returns the absolute pathname of [database], or `null` for a temporary or in-memory
     * database.
     */
    public fun getFileName(database: String = SQLITE_MAIN_DB_NAME): FileName?

    /**
     * Returns the schema name of the database at the 0-based [index], `main` being `0`, or `null`
     * if there is no database at that index.
     */
    public fun getName(index: Int): String?

    /**
     * Returns `true` if the database [database] is read-only, or `false` If it is read/write.
     *
     * @throws ksqlite.kapi.SQLiteException if [database] is not the name of a database on this
     * connection.
     */
    public fun isReadOnly(database: String): Boolean

    /**
     * Attempts to free as much heap memory as possible from this connection.
     *
     * @throws ksqlite.kapi.SQLiteException if something went wrong.
     */
    public fun releaseMemory()

    /**
     * Returns the current value and high-water mark for [option], resetting the high-water mark
     * afterward if [reset] is `true`.
     */
    public fun getStatus(
        option: SqliteDbStatusOption,
        reset: Boolean = false
    ): Status

    /**
     * Disconnects from [database] and then reopens [database] as an in-memory database based on the
     * serialization contained in [serializedDatabase]. The [serializedDatabase] is [databaseSize]
     * bytes in size. [bufferSize] is the size of the buffer [serializedDatabase], which might be
     * larger than [databaseSize]. If [bufferSize] is larger than [databaseSize], and the
     * [SqliteDeserializeFlag.READONLY] bit is not set in [flags], then SQLite is permitted to add
     * content to the in-memory database as long as the total size does not exceed [bufferSize]
     * bytes.
     *
     * @throws ksqlite.kapi.SQLiteException if the operation fails.
     */
    public fun deserialize(
        serializedDatabase: Buffer,
        database: String = SQLITE_MAIN_DB_NAME,
        databaseSize: Long = serializedDatabase.byteSize,
        bufferSize: Long = databaseSize,
        flags: SqliteDeserializeFlag? = null
    )

    /**
     * Enables or disables extended result codes, which report failures in more detail than the
     * primary [ksqlite.types.SqliteResultCode] alone.
     *
     * @throws ksqlite.kapi.SQLiteException if the operation fails.
     */
    public fun setExtendedResultCodesEnabled(enabled: Boolean)

    /**
     * Runs zero or more UTF-8 encoded, semicolon-separated SQL statements from [sql]. If [callback]
     * is not `null` then it is invoked for each result row coming out of the evaluated SQL
     * statements.
     *
     * @throws ksqlite.kapi.SQLiteException if the operation fails.
     */
    public fun execute(
        sql: String,
        callback: Exec? = null
    )

    /**
     * Causes any pending database operation to abort and return at its earliest opportunity.
     */
    public fun interrupt()

    /**
     * Sets the key used to access an encrypted database.
     *
     * This connection must have been opened through a cipher virtual file system for encryption
     * to actually take effect, see [ksqlite.kapi.cipher.CipherVirtualFileSystemManager]. Setting
     * a key on a connection opened through a plain virtual file system fails with a generic
     * error rather than a clear one.
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while setting the key.
     */
    public fun setKey(
        key: ByteArray,
        size: Int = key.size,
        database: String = SQLITE_MAIN_DB_NAME,
    )

    /**
     * Returns the current value of the given [limit] category.
     */
    public fun getLimit(limit: SqliteRuntimeLimit): Int

    /**
     * Sets the value of the given [limit] category.
     */
    public fun setLimit(
        limit: SqliteRuntimeLimit,
        value: Int
    )

    /**
     * Creates and returns a [PreparedStatement].
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while preparing the statement.
     */
    public fun prepare(
        sql: String,
        flags: SqlitePrepareFlag? = null
    ): PreparedStatement

    /**
     * Sets the callback that is invoked prior to each INSERT, UPDATE, and DELETE operation.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the handler fails.
     */
    public fun setPreupdateHook(handler: PreupdateHook?)

    /**
     * Sets [handler] to be invoked every [operationCount] internal VM instructions during
     * long-running calls, or clears it if [operationCount] is not positive.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the handler fails.
     */
    public fun setProgressHandler(
        operationCount: Int,
        handler: ProgressHandler?
    )

    /**
     * Changes the database encryption key.
     *
     * For a dynamic cipher, the cipher must be selected again through
     * [ksqlite.kapi.cipher.CipherConfiguration.setCipher] right before calling this, even if it
     * was already selected earlier on the same connection. Otherwise this silently does nothing
     * instead of failing.
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while setting the key.
     */
    public fun setReKey(
        key: ByteArray,
        size: Int = key.size,
        database: String = SQLITE_MAIN_DB_NAME,
    )

    /**
     * Sets the callback invoked whenever a transaction is rolled back.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the handler fails.
     */
    public fun setRollbackHook(handler: RollbackHook?)

    /**
     * Serializes [database] into a [SerializeResult]. Whether the returned buffer is owned by
     * SQLite or by the caller depends on [flags], see [SerializeResult.Immutable] and
     * [SerializeResult.Mutable].
     */
    public fun serialize(
        flags: SqliteSerializeFlag? = null,
        database: String = SQLITE_MAIN_DB_NAME
    ): SerializeResult

    /**
     * Sets the callback invoked while an SQL statement is being compiled by [prepare], once per
     * action it would perform, to allow or deny each one.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the handler fails.
     */
    public fun setAuthorizer(handler: Authorizer?)

    /**
     * Records the current state of the given [database] and returns a [Snapshot].
     *
     * @throws ksqlite.kapi.SQLiteException if a failure occurs while creating the snapshot.
     */
    public fun createSnapshot(database: String = SQLITE_MAIN_DB_NAME): Snapshot

    /**
     * Starts a new read transaction or upgrades an existing one for [database] of this database
     * such that the read transaction refers to historical [snapshot], rather than the most recent
     * change to the database.
     *
     * @throws ksqlite.kapi.SQLiteException if a failure occurred while attempting to open the
     * snapshot.
     */
    public fun openSnapshot(
        snapshot: Snapshot,
        database: String = SQLITE_MAIN_DB_NAME
    )

    /**
     * Attempts to scan the WAL file associated with [database] of this connection and make all
     * valid snapshots available to [openSnapshot].
     *
     * @throws ksqlite.kapi.SQLiteException if the operation fails.
     */
    public fun recoverSnapshots(database: String = SQLITE_MAIN_DB_NAME)

    /**
     * Returns information about the column [column] of table [table] in database [database].
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while collecting information.
     */
    public fun tableColumnMetadata(
        table: String,
        column: String,
        database: String? = null
    ): TableColumnMetadata

    /**
     * Sets the callback invoked whenever any of the events identified by [eventCodes] occur, or
     * clears it if [eventCodes] is `null`.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the handler fails.
     */
    public fun setTrace(
        eventCodes: SqliteTraceEventCode?,
        handler: Trace?
    )

    /**
     * Returns the current transaction state of [database].
     *
     * @throws ksqlite.kapi.SQLiteException if the [database] is not valid.
     */
    public fun getTransactionState(database: String? = null): SqliteTransactionState

    /**
     * Sets the callback that is invoked whenever a row is updated, inserted or deleted in a rowid
     * table.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the handler fails.
     */
    public fun setUpdateHook(handler: UpdateHook?)
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Convenience overload of [createCollation] using UTF-8 encoding, the only one currently
 * supported.
 *
 * @throws ksqlite.kapi.SQLiteException if setting the handler fails.
 */
@IgnorableReturnValue
public fun DatabaseConnection.createCollation(
    name: String,
    collation: Collation
): Registration = createCollation(name, SqliteTextEncoding.UTF8, collation)

/**
 * Convenience overload of [createFunction] using UTF-8 encoding, the only one currently supported.
 *
 * @throws ksqlite.kapi.SQLiteException if an error happened while creating the function.
 */
@IgnorableReturnValue
public fun DatabaseConnection.createFunction(
    name: String,
    argumentCount: Int,
    function: ScalarFunction
): Registration = createFunction(name, argumentCount, SqliteTextEncoding.UTF8, function)

/**
 * Convenience overload of [createFunction] using UTF-8 encoding, the only one currently supported.
 *
 * @throws ksqlite.kapi.SQLiteException if an error happened while creating the function.
 */
@IgnorableReturnValue
public fun DatabaseConnection.createFunction(
    name: String,
    argumentCount: Int,
    function: AggregateFunction
): Registration = createFunction(name, argumentCount, SqliteTextEncoding.UTF8, function)

/**
 * Convenience overload of [createFunction] using UTF-8 encoding, the only one currently supported.
 *
 * @throws ksqlite.kapi.SQLiteException if an error happened while creating the function.
 */
@IgnorableReturnValue
public fun DatabaseConnection.createFunction(
    name: String,
    argumentCount: Int,
    function: WindowFunction
): Registration = createFunction(name, argumentCount, SqliteTextEncoding.UTF8, function)