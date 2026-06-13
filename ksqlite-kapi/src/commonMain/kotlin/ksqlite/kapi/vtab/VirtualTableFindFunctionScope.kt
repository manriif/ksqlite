package ksqlite.kapi.vtab

import ksqlite.capi.types.vtab.Sqlite3VTabConstraintOperatorCode

/**
 * Scope to use with [VirtualTable.bestIndex].
 */
public interface VirtualTableFindFunctionScope {

    /**
     * Sets the custom constraint operator code to return to SQLite.
     *
     * By default, if [VirtualTable.bestIndex] returns a non-null function, `one` is returned to
     * SQLite to indicates that the function is overloaded, and `zero` is returned otherwise.
     *
     * If a custom [code] is set, it is returned, with the scalar function, instead of the default
     * `one`. However, if no scalar function is returned but a custom [code] is set, then an
     * exception is thrown.
     *
     * Note that this replaces any [code] from a previous call to [customConstraintOperator].
     */
    public fun customConstraintOperator(code: Sqlite3VTabConstraintOperatorCode.Custom)
}