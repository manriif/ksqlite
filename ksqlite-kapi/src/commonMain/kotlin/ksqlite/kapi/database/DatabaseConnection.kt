package ksqlite.kapi.database

import ksqlite.capi.types.sqlite3
import ksqlite.kapi.MAIN_DB_NAME
import ksqlite.kapi.blob.Blob
import ksqlite.kapi.functions.AggregateFunction
import ksqlite.kapi.functions.ScalarFunction
import ksqlite.kapi.functions.WindowFunction
import ksqlite.kapi.vtab.VirtualTableModule
import ksqlite.types.SqliteBlobOpenFlag
import ksqlite.types.SqliteDbStatusOption
import ksqlite.types.SqliteTextEncoding
import ksqlite.types.vtab.SqliteModuleVersion
import kotlin.time.Duration

/**
 * [Database connection](https://sqlite.org/c3ref/sqlite3.html).
 */
public abstract class DatabaseConnection internal constructor() : AutoCloseable {

    internal abstract val db: sqlite3

    /**
     * Returns the number of rows modified, inserted or deleted by the most recently completed
     * INSERT, UPDATE or DELETE statement.
     */
    public abstract val changes: Long

    /**
     * Configuration of the connection.
     */
    public abstract val config: DatabaseConnectionConfiguration

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
        databaseName: String = MAIN_DB_NAME,
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
        encoding: SqliteTextEncoding.Set0,
        collation: Collation
    )

    /**
     * Deletes the collation created using the same parameters.
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while deleting the collation.
     */
    public abstract fun deleteCollation(
        name: String,
        encoding: SqliteTextEncoding.Set0
    )

    /**
     * Creates or updates a scalar function.
     *
     * @throws ksqlite.kapi.SQLiteException if an error happened while creating the function.
     */
    public abstract fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: SqliteTextEncoding,
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
        encoding: SqliteTextEncoding,
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
        encoding: SqliteTextEncoding,
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
        encoding: SqliteTextEncoding,
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
     * Returns the absolute pathname of the database [databaseName] of this connection.
     */
    public abstract fun getFileName(databaseName: String = MAIN_DB_NAME): String?

    /**
     * Returns the schema name for the [index]th database on this connection.
     */
    public abstract fun getName(index: Int): String?

    /**
     * Returns `true` if the database [databaseName] is read-only, or `false` If it is read/write.
     *
     * @throws ksqlite.kapi.SQLiteException if [databaseName] is not the name of a database on this
     * connection.
     */
    public abstract fun isReadOnly(databaseName: String): Boolean

    /**
     * Returns the status for the given options.
     */
    public abstract fun getStatus(
        option: SqliteDbStatusOption,
        reset: Boolean = false
    ): DatabaseConnectionStatus
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Sets a [BusyHandler] that sleeps for a specified amount of time when a table is locked.
 * Any [BusyHandler] previously passed to [setBusyHandler] is replaced.
 *
 * The [duration] is coerced to [Int.MAX_VALUE] milliseconds.
 *
 * @throws ksqlite.kapi.SQLiteException if setting the timeout fails.
 */
public fun DatabaseConnection.setBusyTimeout(duration: Duration): Unit =
    setBusyTimeout(duration.inWholeMilliseconds.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())