package ksqlite.kapi.vtab

import ksqlite.kapi.Connection
import ksqlite.kapi.SQLiteException

/**
 * Depending on the virtual table kind, the implementation must extends one of [Eponymous],
 * [EponymousOnly] or [Regular].
 *
 *
 * [Virtual Table](https://sqlite.org/vtab.html)
 */
public sealed interface VirtualTableModule {

    ///////////////////////////////////////////////////////////////////////////
    // Kind
    ///////////////////////////////////////////////////////////////////////////

    /**
     * [Eponymous Virtual Table](https://sqlite.org/vtab.html#eponymous_virtual_tables), sharing the
     * same implementation for xConnect and xCreate.
     */
    public fun interface Eponymous : VirtualTableModule

    /**
     * [Eponymous Only Virtual Table](https://sqlite.org/vtab.html#eponymous_only_virtual_tables),
     * not providing an xCreate implementation.
     */
    public fun interface EponymousOnly : VirtualTableModule

    /**
     * Regular Virtual Table implementing the xCreate function.
     */
    public interface Regular : VirtualTableModule {

        public fun VirtualTableCreateOrConnectScope.create(
            connection: Connection,
            arguments: Array<String>
        ): VirtualTable
    }

    ///////////////////////////////////////////////////////////////////////////
    //  Mandatory functions
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Connects to a virtual table and returns the table.
     *
     * If an error is detected, it is allowed to throws an [SQLiteException] that is then returned
     * to SQLite.
     */
    public fun VirtualTableCreateOrConnectScope.connect(
        connection: Connection,
        arguments: Array<String>
    ): VirtualTable
}