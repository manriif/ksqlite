package ksqlite.capi.vtab.callbacks

import ksqlite.capi.callbacks.Sqlite3DestroyCallback
import ksqlite.capi.callbacks.Sqlite3FunctionFuncCallback
import ksqlite.capi.types.vtab.Sqlite3VTabConstraintOperatorCode
import ksqlite.capi.vtab.sqlite3_vtab

/**
 * This method releases a connection to a virtual table, just like the xDisconnect method, and it
 * also findFunctions the underlying table implementation. This method undoes the work of xCreate.
 *
 * [The xFindFunction Method](https://sqlite.org/vtab.html#the_xfindfunction_method)
 */
public fun interface Sqlite3VTabFindFunctionCallback<VTab : sqlite3_vtab> {

    /**
     * Result for [handle].
     */
    public sealed interface Result

    /**
     * Scope for [handle].
     */
    public sealed interface Scope {

        /**
         * Writes [function] to `pxFunc`, overloading the original one, and returns 1 to SQLite.
         */
        public fun overload(function: Sqlite3FunctionFuncCallback<Nothing?>): Result

        /**
         * Writes [function] to `pxFunc`, overloading the original one, and returns 1 to SQLite.
         */
        public fun <AppData> overload(
            appData: AppData,
            function: Sqlite3FunctionFuncCallback<in AppData>,
            destroy: Sqlite3DestroyCallback<in AppData>?
        ): Result

        /**
         * Writes [function] to `pxFunc`, overloading the original one, and returns [constraintOp]
         * to SQLite.
         */
        public fun overload(
            constraintOp: Sqlite3VTabConstraintOperatorCode.Custom,
            function: Sqlite3FunctionFuncCallback<Nothing?>
        ): Result

        /**
         * Writes [function] to `pxFunc`, overloading the original one, and returns [constraintOp]
         * to SQLite.
         */
        public fun <AppData> overload(
            constraintOp: Sqlite3VTabConstraintOperatorCode.Custom,
            appData: AppData,
            function: Sqlite3FunctionFuncCallback<in AppData>,
            destroy: Sqlite3DestroyCallback<in AppData>?
        ): Result

        /**
         * Returns `0` to SQLite to not overload the function.
         */
        public fun doNotOverload(): Result
    }

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xfindfunction_method).
     */
    public fun Scope.handle(
        vTab: VTab,
        argumentCount: Int,
        functionName: String
    ): Result
}