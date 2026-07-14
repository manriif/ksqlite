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

import ksqlite.capi.sqlite3
import ksqlite.capi.vtab.sqlite3_vtab
import ksqlite.types.SqliteResultCode

/**
 * Serves both for [SqliteVtabCreateCallback] and [SqliteVtabConnectCallback].
 */
public fun interface SqliteVtabCreateOrConnectCallback<AppData, Vtab : sqlite3_vtab> {

    /**
     * Result for [apply].
     */
    public sealed interface Result<out Vtab : sqlite3_vtab>

    /**
     * Scope for [apply].
     */
    public sealed interface Scope<Vtab : sqlite3_vtab> {

        /**
         * Writes [vTab] to `ppVtab` and returns [SqliteResultCode.OK] to SQLite.
         */
        public fun success(vTab: Vtab): Result<Vtab>

        /**
         * Writes [error] to `pzErr` and returns [SqliteResultCode.ERROR] to SQLite.
         */
        public fun failure(error: String): Result<Vtab>
    }

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xcreate_method)
     * and/or [here](https://sqlite.org/vtab.html#the_xconnect_method).
     *
     * A [Result] instance can be obtained by invoking one of [Scope.success] or [Scope.failure].
     */
    public fun Scope<Vtab>.apply(
        db: sqlite3,
        appData: AppData,
        argv: Array<String>
    ): Result<Vtab>
}

///////////////////////////////////////////////////////////////////////////
// Aliases
///////////////////////////////////////////////////////////////////////////

/**
 * The xCreate method is called to create a new instance of a virtual table in response to a CREATE
 * VIRTUAL TABLE statement. If the xCreate method is the same pointer as the xConnect method, then
 * the virtual table is an eponymous virtual table. If the xCreate method is omitted (if it is a
 * NULL pointer) then the virtual table is an eponymous-only virtual table.
 *
 * [The xCreate Method](https://sqlite.org/vtab.html#the_xcreate_method)
 */
public typealias SqliteVtabCreateCallback<AppData, Vtab> =
        SqliteVtabCreateOrConnectCallback<AppData, Vtab>

/**
 * The xConnect method is very similar to xCreate. It has the same parameters and constructs a new
 * sqlite3_vtab structure just like xCreate. And it must also call sqlite3_declare_vtab() like
 * xCreate. It should also make all of the same sqlite3_vtab_config() calls as xCreate.
 *
 * The difference is that xConnect is called to establish a new connection to an existing virtual
 * table whereas xCreate is called to create a new virtual table from scratch.
 *
 * [The xConnect Method](https://sqlite.org/vtab.html#the_xconnect_method)
 */
public typealias SqliteVtabConnectCallback<AppData, Vtab> =
        SqliteVtabCreateOrConnectCallback<AppData, Vtab>