package ksqlite.kapi.connection

import ksqlite.capi.types.Sqlite3BlobOpenFlag
import ksqlite.capi.types.Sqlite3TextEncoding
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.vtab.Sqlite3ModuleVersion
import ksqlite.kapi.blob.Blob
import ksqlite.kapi.callbacks.AutovacuumPages
import ksqlite.kapi.callbacks.BusyHandler
import ksqlite.kapi.callbacks.CollationComparator
import ksqlite.kapi.functions.AggregateFunction
import ksqlite.kapi.functions.ScalarFunction
import ksqlite.kapi.functions.WindowFunction
import ksqlite.kapi.vtab.VirtualTableModule
import kotlin.time.Duration

public typealias OpenFlag = Sqlite3BlobOpenFlag

/**
 * [Database connection](https://sqlite.org/c3ref/sqlite3.html).
 */
public abstract class Connection internal constructor(): AutoCloseable {

    internal abstract val db: sqlite3

    /**
     * [sqlite3_autovacuum_pages()](https://sqlite.org/c3ref/autovacuum_pages.html)
     */
    public abstract fun setAutovacuumPages(callback: AutovacuumPages?)

    /**
     *
     */
    public abstract fun openBlob(openFlag: OpenFlag): Blob

    /**
     * Sets the busy [handler].
     * Note that any existing [ksqlite.kapi.callbacks.BusyHandler] is replaced.
     */
    public abstract fun setBusyHandler(handler: BusyHandler?)

    /**
     * Sets the busy timeout.
     * Note that any existing [BusyHandler] is replaced.
     */
    public abstract fun setBusyTimeout(duration: Duration)

    public abstract fun setCollationNeeded()

    public abstract fun createCollation(comparator: CollationComparator)

    public abstract fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: Sqlite3TextEncoding,
        function: ScalarFunction
    )

    public abstract fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: Sqlite3TextEncoding,
        function: AggregateFunction
    )

    public abstract fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: Sqlite3TextEncoding,
        function: WindowFunction
    )

    public abstract fun createModule(
        name: String,
        version: Sqlite3ModuleVersion = Sqlite3ModuleVersion.VERSION_4,
        module: VirtualTableModule.Regular
    )

    public abstract fun createModule(
        name: String,
        version: Sqlite3ModuleVersion = Sqlite3ModuleVersion.VERSION_4,
        module: VirtualTableModule.Eponymous
    )

    public abstract fun createModule(
        name: String,
        version: Sqlite3ModuleVersion = Sqlite3ModuleVersion.VERSION_4,
        module: VirtualTableModule.EponymousOnly
    )

    public abstract fun dropModules(keepModules: Set<String>)
}