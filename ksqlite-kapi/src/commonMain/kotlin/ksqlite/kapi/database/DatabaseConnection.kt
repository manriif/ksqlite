package ksqlite.kapi.database

import ksqlite.capi.types.sqlite3
import ksqlite.kapi.MAIN_DB_NAME
import ksqlite.kapi.blob.Blob
import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.functions.AggregateFunction
import ksqlite.kapi.functions.ScalarFunction
import ksqlite.kapi.functions.WindowFunction
import ksqlite.kapi.vtab.VirtualTableModule
import ksqlite.types.SqliteBlobOpenFlag
import ksqlite.types.SqliteDbStatusOption
import ksqlite.types.SqliteDeserializeFlag
import ksqlite.types.SqliteFunctionTextEncoding
import ksqlite.types.SqliteTextEncoding
import ksqlite.types.vtab.SqliteModuleVersion

/**
 * [Database connection](https://sqlite.org/c3ref/sqlite3.html).
 */
public abstract class DatabaseConnection internal constructor() : AutoCloseable {

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
    public abstract val lastError: DatabaseConnectionLastError

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
    public abstract fun getFileName(database: String = MAIN_DB_NAME): String?

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
     * Returns the status for the given options.
     */
    public abstract fun getStatus(
        option: SqliteDbStatusOption,
        reset: Boolean = false
    ): DatabaseConnectionOptionStatus

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
}