package ksqlite.kapi.database

import ksqlite.capi.sqlite3
import ksqlite.kapi.MAIN_DB_NAME
import ksqlite.kapi.blob.Blob
import ksqlite.kapi.buffer.Buffer
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
 * Exposes the [Database Connection](https://sqlite.org/c3ref/sqlite3.html) and the associated
 * [Encryption](https://utelle.github.io/SQLite3MultipleCiphers/docs/configuration/config_capi/)
 * APIs.
 */
public abstract class DatabaseConnection internal constructor() : AutoCloseable {

    /**
     * Database connection handle.
     */
    internal abstract val db: sqlite3

    /**
     * Configuration of the connection.
     */
    public abstract val config: DatabaseConnectionConfiguration

    /**
     * Number of rows modified, inserted or deleted by the most recently completed INSERT, UPDATE or
     * DELETE statement.
     */
    public abstract val changes: Long

    /**
     * Most recent error information.
     */
    public abstract val lastError: LastError

    /**
     * File control object that allows direct call to the xFileControl.
     */
    public abstract val fileControl: FileControl

    /**
     * Whether the connection is in autocommit mode.
     */
    public abstract val isAutocommit: Boolean

    /**
     * Whether an interrupt is currently in effect.
     */
    public abstract val isInterrupted: Boolean

    /**
     * Rowid of the most recent successful INSERT into a rowid table or virtual table on this
     * connection.
     */
    public abstract var lastInsertRowid: Long

    /**
     * Total number of rows inserted, modified or deleted by all INSERT, UPDATE or DELETE statements
     * completed since the database connection was opened, including those executed as part of
     * trigger programs.
     */
    public abstract val totalChanges: Long

    /**
     * Write-Ahead Log operating on this connection.
     */
    public abstract val wal: WriteAheadLog

    /**
     * Sets the callback that is invoked prior to each autovacuum of the database file.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the callback fails.
     */
    public abstract fun setAutovacuumPages(handler: AutovacuumPages?)

    /**
     * Opens a [Blob].
     *
     * @throws ksqlite.kapi.SQLiteException if opening the blob fails.
     */
    public abstract fun openBlob(
        tableName: String,
        columnName: String,
        rowid: Long,
        database: String = MAIN_DB_NAME,
        flags: SqliteBlobOpenFlag = SqliteBlobOpenFlag.READONLY
    ): Blob

    /**
     * Sets the callback that might be invoked whenever an attempt is made to access a database
     * table associated with this connection when another thread or process has the table locked.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the handler fails.
     */
    public abstract fun setBusyHandler(handler: BusyHandler?)

    /**
     * Sets a [BusyHandler] that sleeps for a specified amount of time when a table is locked.
     * Any [BusyHandler] previously passed to [setBusyHandler] is replaced.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the timeout fails.
     */
    public abstract fun setBusyTimeout(millis: Int)

    /**
     * Sets the callback that get invoked whenever an undefined collation sequence is required.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the handler fails.
     */
    public abstract fun setCollationNeeded(handler: CollationNeeded?)

    /**
     * Sets the callback that get invoked whenever an undefined collation sequence is required.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the handler fails.
     */
    public abstract fun setCommitHook(handler: CommitHook?)

    /**
     * Creates or replaces a collation.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the handler fails.
     */
    public abstract fun createCollation(
        name: String,
        encoding: SqliteTextEncoding.CreateCollation,
        collation: Collation
    )

    /**
     * Deletes the collation created using the same parameters.
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while deleting the collation.
     */
    public abstract fun deleteCollation(
        name: String,
        encoding: SqliteTextEncoding.CreateCollation
    )

    /**
     * Creates or updates a scalar function.
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while creating the function.
     */
    public abstract fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: SqliteFunctionTextEncoding,
        function: ScalarFunction
    )

    /**
     * Creates or updates an aggregate function.
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while creating the function.
     */
    public abstract fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: SqliteFunctionTextEncoding,
        function: AggregateFunction
    )

    /**
     * Creates or updates a window function.
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while creating the function.
     */
    public abstract fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: SqliteFunctionTextEncoding,
        function: WindowFunction
    )

    /**
     * Deletes the function that was created with the same arguments.
     * If the function is a window function, then [isWindowFunction] should be set to `true`.
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while deleting the function.
     */
    public abstract fun deleteFunction(
        name: String,
        argumentCount: Int,
        encoding: SqliteFunctionTextEncoding,
        isWindowFunction: Boolean = false
    )

    /**
     * Creates or replaces a virtual table module.
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while creating the module.
     */
    public abstract fun createModule(
        name: String,
        version: SqliteModuleVersion = SqliteModuleVersion.VERSION_4,
        module: VirtualTableModule.Regular
    )

    /**
     * Creates or replaces a virtual table module.
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while creating the module.
     */
    public abstract fun createModule(
        name: String,
        version: SqliteModuleVersion = SqliteModuleVersion.VERSION_4,
        module: VirtualTableModule.Eponymous
    )

    /**
     * Creates or replaces a virtual table module.
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while creating the module.
     */
    public abstract fun createModule(
        name: String,
        version: SqliteModuleVersion = SqliteModuleVersion.VERSION_4,
        module: VirtualTableModule.EponymousOnly
    )

    /**
     * Deletes the module for [name].
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while deleting the module.
     */
    public abstract fun deleteModule(name: String)

    /**
     * Deletes all the modules excepts those that get their name listed in [keep].
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while deleting the modules.
     */
    public abstract fun deleteModules(keep: Set<String>)

    /**
     * Writes any dirty pages in the pager-cache that are not currently in use. The operation only
     * take places if there is a write-transaction open at the time this function is called.
     *
     * @throws ksqlite.kapi.SQLiteException if the operation fails.
     */
    public abstract fun flushCache()

    /**
     * Returns the absolute pathname of the database [database] of this connection.
     */
    public abstract fun getFileName(database: String = MAIN_DB_NAME): FileName?

    /**
     * Returns the schema name for the [index]th database on this connection.
     */
    public abstract fun getName(index: Int): String?

    /**
     * Returns `true` if the database [database] is read-only, or `false` If it is read/write.
     *
     * @throws ksqlite.kapi.SQLiteException if [database] is not the name of a database on this
     * connection.
     */
    public abstract fun isReadOnly(database: String): Boolean

    /**
     * Attempts to free as much heap memory as possible from this connection.
     *
     * @throws ksqlite.kapi.SQLiteException if something went wrong.
     */
    public abstract fun releaseMemory()

    /**
     * Returns the status for the given options.
     */
    public abstract fun getStatus(
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
    public abstract fun deserialize(
        serializedDatabase: Buffer,
        database: String? = null,
        databaseSize: Long = serializedDatabase.byteSize,
        bufferSize: Long = databaseSize,
        flags: SqliteDeserializeFlag? = null
    )

    /**
     * Enables or disables the extended result codes.
     *
     * @throws ksqlite.kapi.SQLiteException if the operation fails.
     */
    public abstract fun setExtendedResultCodesEnabled(enabled: Boolean)

    /**
     * Runs zero or more UTF-8 encoded, semicolon-separated SQL statements from [sql]. If [callback]
     * is not `null` then it is invoked for each result row coming out of the evaluated SQL
     * statements.
     *
     * @throws ksqlite.kapi.SQLiteException if the operation fails.
     */
    public abstract fun execute(
        sql: String,
        callback: Exec? = null
    )

    /**
     * Causes any pending database operation to abort and return at its earliest opportunity.
     */
    public abstract fun interrupt()

    /**
     * Sets the database key to use when accessing an encrypted database.
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while setting the key.
     */
    public abstract fun setKey(
        key: ByteArray,
        size: Int = key.size,
        database: String = MAIN_DB_NAME,
    )

    /**
     * Returns the current value of the given [limit] category.
     */
    public abstract fun getLimit(limit: SqliteRuntimeLimit): Int

    /**
     * Sets the value of the given [limit] category.
     */
    public abstract fun setLimit(
        limit: SqliteRuntimeLimit,
        value: Int
    )

    /**
     * Creates and returns a [PreparedStatement].
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while preparing the statement.
     */
    public abstract fun prepare(
        sql: String,
        flags: SqlitePrepareFlag? = null
    ): PreparedStatement

    /**
     * Sets the callback that is invoked prior to each INSERT, UPDATE, and DELETE operation.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the handler fails.
     */
    public abstract fun setPreupdateHook(handler: PreupdateHook?)

    /**
     * Sets the callback that is invoked periodically during long-running calls.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the handler fails.
     */
    public abstract fun setProgressHandler(
        operationCount: Int,
        handler: ProgressHandler?
    )

    /**
     * Changes the database encryption key.
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while setting the key.
     */
    public abstract fun setReKey(
        key: ByteArray,
        size: Int = key.size,
        database: String = MAIN_DB_NAME,
    )

    /**
     * Sets the callback that get invoked whenever a transaction is rolled back.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the handler fails.
     */
    public abstract fun setRollbackHook(handler: RollbackHook?)

    /**
     * Returns a [Buffer] that is a serialization of the given [database].
     */
    public abstract fun serialize(
        flags: SqliteSerializeFlag? = null,
        database: String? = null
    ): SerializeResult

    /**
     * Sets the callback that get invoked whenever an SQL statements is being compiled by [prepare].
     *
     * @throws ksqlite.kapi.SQLiteException if setting the handler fails.
     */
    public abstract fun setAuthorizer(handler: Authorizer?)

    /**
     * Records the current state of the given [database] and returns a [Snapshot].
     *
     * @throws ksqlite.kapi.SQLiteException if a failure occurs while creating the snapshot.
     */
    public abstract fun createSnapshot(database: String? = null): Snapshot

    /**
     * Starts a new read transaction or upgrades an existing one for [database] of this database
     * such that the read transaction refers to historical [snapshot], rather than the most recent
     * change to the database.
     *
     * @throws ksqlite.kapi.SQLiteException if a failure occurred while attempting to open the
     * snapshot.
     */
    public abstract fun openSnapshot(
        snapshot: Snapshot,
        database: String? = null
    )

    /**
     * Attempts to scan the WAL file associated with [database] of this connection and make all
     * valid snapshots available to [openSnapshot].
     *
     * @throws ksqlite.kapi.SQLiteException if the operation fails.
     */
    public abstract fun recoverSnapshots(database: String? = null)

    /**
     * Returns information about the column [column] of table [table] in database [database].
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while collecting information.
     */
    public abstract fun tableColumnMetadata(
        table: String,
        column: String,
        database: String? = null
    ): TableColumnMetadata

    /**
     * Sets the callback that get invoked whenever any of the events identified by [eventCodes]
     * occur.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the handler fails.
     */
    public abstract fun setTrace(
        eventCodes: SqliteTraceEventCode?,
        handler: Trace?
    )

    /**
     * Returns the current transaction state of [database].
     *
     * @throws ksqlite.kapi.SQLiteException if the [database] is not valid.
     */
    public abstract fun getTransactionState(database: String? = null): SqliteTransactionState

    /**
     * Sets the callback that is invoked whenever a row is updated, inserted or deleted in a rowid
     * table.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the handler fails.
     */
    public abstract fun setUpdateHook(handler: UpdateHook?)
}