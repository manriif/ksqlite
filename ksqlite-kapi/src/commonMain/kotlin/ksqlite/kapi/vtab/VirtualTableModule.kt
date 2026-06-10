package ksqlite.kapi.vtab

import ksqlite.kapi.SQLiteConnection

/**
 * Depending on the virtual table kind, the implementation must extends one of [Eponymous],
 * [EponymousOnly] or [Regular].
 *
 *
 * [Virtual Table](https://sqlite.org/vtab.html)
 */
public sealed interface VirtualTableModule<Cursor : VirtualTableCursor, Table : VirtualTable<Cursor>> {

    ///////////////////////////////////////////////////////////////////////////
    // Kind
    ///////////////////////////////////////////////////////////////////////////

    /**
     * [Eponymous Virtual Table](https://sqlite.org/vtab.html#eponymous_virtual_tables), sharing the
     * same implementation for xConnect and xCreate.
     */
    public interface Eponymous<Cursor : VirtualTableCursor, Table : VirtualTable<Cursor>> :
        VirtualTableModule<Cursor, Table>

    /**
     * [Eponymous Only Virtual Table](https://sqlite.org/vtab.html#eponymous_only_virtual_tables),
     * not providing an xCreate implementation.
     */
    public interface EponymousOnly<Cursor : VirtualTableCursor, Table : VirtualTable<Cursor>> :
        VirtualTableModule<Cursor, Table>

    /**
     * Regular Virtual Table implementing the xCreate function.
     */
    public interface Regular<Cursor : VirtualTableCursor, Table : VirtualTable<Cursor>> :
        VirtualTableModule<Cursor, Table> {

        public fun VirtualTableCreateOrConnectScope<Table>.create(

        ): Table
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
    public fun connect(
        connection: SQLiteConnection,
        arguments: Array<String>
    ): Table
}