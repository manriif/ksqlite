package ksqlite.kapi.vtab

import ksqlite.capi.vtab.Sqlite3VTabConfigOption

/**
 * Scope to use with [VirtualTableModule.connect] and [VirtualTableModule.Regular.create].
 */
public interface VirtualTableCreateOrConnectScope {

    /**
     * Configures the virtual table [options].
     */
    public fun configure(options: List<Sqlite3VTabConfigOption>)

    /**
     * Declares the schema of the virtual table.
     */
    public fun declare(sql: String)

    /**
     * Declares that the virtual table overloads the function identified by [name] and
     * [argumentCount].
     *
     * The virtual table must implement [VirtualTable.FindFunctionSupport].
     */
    public fun overloadFunction(
        name: String,
        argumentCount: Int
    )
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Configures the virtual table [options].
 */
public fun VirtualTableCreateOrConnectScope.configure(options: Iterable<Sqlite3VTabConfigOption>) {
    configure(options.toList())
}

/**
 * Configures the virtual table [options].
 */
public fun VirtualTableCreateOrConnectScope.configure(vararg options: Sqlite3VTabConfigOption) {
    configure(options.toList())
}