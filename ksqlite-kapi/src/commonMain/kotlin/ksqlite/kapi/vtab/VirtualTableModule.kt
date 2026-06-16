package ksqlite.kapi.vtab

import co.touchlab.stately.concurrency.Lock
import ksqlite.capi.vtab.sqlite3_module
import ksqlite.kapi.SQLiteException
import ksqlite.kapi.connection.Connection
import kotlin.concurrent.Volatile

/**
 * [Virtual Table Module](https://sqlite.org/vtab.html#implementation).
 *
 * This interface is not directly implementable, one of [Regular], [Eponymous] or [EponymousOnly]
 * must be implemented depending on the virtual table kind.
 *
 * Note that a [VirtualTableModule] cannot be a singleton nor a reusable instance — a new instance
 * must always be passed to [Connection.createModule].
 */
public sealed class VirtualTableModule : AutoCloseable {

    internal var module: sqlite3_module<*>? = null
    internal val moduleLock = Lock()

    ///////////////////////////////////////////////////////////////////////////
    // Kinds
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Regular Virtual Table.
     * Distinct callbacks are passed to SQLite for [create] and [connect].
     */
    public abstract class Regular : VirtualTableModule() {

        /**
         * Creates and connects to a virtual table and returns the [VirtualTable] instance.
         * If an error is detected, an [SQLiteException] is thrown and is returned to SQLite.
         */
        public abstract fun VirtualTableCreateOrConnectScope.create(
            connection: Connection,
            arguments: Array<String>
        ): VirtualTable
    }

    /**
     * [Eponymous Virtual Table](https://sqlite.org/vtab.html#eponymous_virtual_tables).
     * The same callback reference is passed to SQLite for both create and [connect].
     */
    public abstract class Eponymous : VirtualTableModule()

    /**
     * [Eponymous Only Virtual Table](https://sqlite.org/vtab.html#eponymous_only_virtual_tables).
     * A callback is passed to SQLite for [connect] but not for create.
     */
    public abstract class EponymousOnly : VirtualTableModule()

    ///////////////////////////////////////////////////////////////////////////
    // Common
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the optional virtual table functions supported by the module.
     * Only the functions that are returned are invoked by SQLite.
     */
    public fun optionalFunctions(): Set<VirtualTableOptionalFunction> = emptySet()

    /**
     * Connects to an already created virtual table and returns the [VirtualTable] instance.
     * If an error is detected, an [SQLiteException] is thrown and is returned to SQLite.
     */
    public abstract fun VirtualTableCreateOrConnectScope.connect(
        connection: Connection,
        arguments: Array<String>
    ): VirtualTable

    /**
     * Called when the module is finalized by SQLite. Finalization can also happen when the
     * module registration fails.
     */
    override fun close(): Unit = Unit
}