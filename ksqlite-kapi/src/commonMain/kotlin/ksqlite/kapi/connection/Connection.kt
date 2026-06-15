package ksqlite.kapi.connection

import ksqlite.capi.types.sqlite3
import ksqlite.kapi.MAIN_DB_NAME
import ksqlite.kapi.blob.Blob
import ksqlite.kapi.functions.AggregateFunction
import ksqlite.kapi.functions.ScalarFunction
import ksqlite.kapi.functions.WindowFunction
import ksqlite.kapi.vtab.VirtualTableModule
import ksqlite.types.SqliteBlobOpenFlag
import ksqlite.types.SqliteTextEncoding
import ksqlite.types.vtab.SqliteModuleVersion
import kotlin.time.Duration

/**
 * [Database connection](https://sqlite.org/c3ref/sqlite3.html).
 */
public abstract class Connection internal constructor(): AutoCloseable {

    internal abstract val db: sqlite3

    /**
     * Sets the callback that is invoked prior to each autovacuum of the database file.
     *
     * Any previously set callback is replaced.
     */
    public abstract fun setAutovacuumPages(callback: AutovacuumPages?)

    /**
     * Opens a [Blob].
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
     * Any previously set callback is replaced.
     */
    public abstract fun setBusyHandler(handler: BusyHandler?)

    /**
     * Sets a[BusyHandler] that sleeps for a specified amount of time when a table is locked.
     * Any [BusyHandler] previously passed to [setBusyHandler] is replaced.
     */
    public abstract fun setBusyTimeout(duration: Duration)

    public abstract fun setCollationNeeded()

    public abstract fun createCollation(comparator: CollationComparator)

    public abstract fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: SqliteTextEncoding,
        function: ScalarFunction
    )

    public abstract fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: SqliteTextEncoding,
        function: AggregateFunction
    )

    public abstract fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: SqliteTextEncoding,
        function: WindowFunction
    )

    public abstract fun createModule(
        name: String,
        version: SqliteModuleVersion = SqliteModuleVersion.VERSION_4,
        module: VirtualTableModule.Regular
    )

    public abstract fun createModule(
        name: String,
        version: SqliteModuleVersion = SqliteModuleVersion.VERSION_4,
        module: VirtualTableModule.Eponymous
    )

    public abstract fun createModule(
        name: String,
        version: SqliteModuleVersion = SqliteModuleVersion.VERSION_4,
        module: VirtualTableModule.EponymousOnly
    )

    public abstract fun dropModules(keepModules: Set<String>)
}