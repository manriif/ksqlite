/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ksqlite.capi.vtab.callbacks

import ksqlite.capi.callbacks.SqliteDestroyCallback
import ksqlite.capi.callbacks.SqliteFunctionFuncCallback
import ksqlite.capi.vtab.sqlite3_vtab
import ksqlite.types.vtab.SqliteVtabConstraintOperatorCode

/**
 * This method releases a connection to a virtual table, just like the xDisconnect method, and it
 * also findFunctions the underlying table implementation. This method undoes the work of xCreate.
 *
 * [The xFindFunction Method](https://sqlite.org/vtab.html#the_xfindfunction_method)
 */
public fun interface SqliteVtabFindFunctionCallback<Vtab : sqlite3_vtab> {

    /**
     * Result for [apply].
     */
    public sealed interface Result

    /**
     * Scope for [apply].
     */
    public sealed interface Scope {

        /**
         * Writes [function] to `pxFunc`, overloading the original one, and returns 1 to SQLite.
         */
        public fun overload(function: SqliteFunctionFuncCallback<Nothing?>): Result

        /**
         * Writes [function] to `pxFunc`, overloading the original one, and returns 1 to SQLite.
         */
        public fun <AppData> overload(
            appData: AppData,
            function: SqliteFunctionFuncCallback<in AppData>,
            destroy: SqliteDestroyCallback<in AppData>?
        ): Result

        /**
         * Writes [function] to `pxFunc`, overloading the original one, and returns [constraintOp]
         * to SQLite.
         */
        public fun overload(
            constraintOp: SqliteVtabConstraintOperatorCode.Custom,
            function: SqliteFunctionFuncCallback<Nothing?>
        ): Result

        /**
         * Writes [function] to `pxFunc`, overloading the original one, and returns [constraintOp]
         * to SQLite.
         */
        public fun <AppData> overload(
            constraintOp: SqliteVtabConstraintOperatorCode.Custom,
            appData: AppData,
            function: SqliteFunctionFuncCallback<in AppData>,
            destroy: SqliteDestroyCallback<in AppData>?
        ): Result

        /**
         * Returns `0` to SQLite to not overload the function.
         */
        public fun doNotOverload(): Result
    }

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xfindfunction_method).
     */
    public fun Scope.apply(
        vTab: Vtab,
        argumentCount: Int,
        functionName: String
    ): Result
}