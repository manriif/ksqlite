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

import ksqlite.capi.vtab.sqlite3_vtab
import ksqlite.capi.vtab.sqlite3_vtab_cursor
import ksqlite.types.SqliteResultCode

/**
 * The xOpen method creates a new cursor used for accessing (read and/or writing) a virtual table.
 * A successful invocation of this method will allocate the memory for the sqlite3_vtab_cursor
 * (or a subclass), initialize the new object, and make *ppCursor point to the new object.
 *
 * [The xOpen Method](https://sqlite.org/vtab.html#the_xopen_method)
 */
public fun interface SqliteVtabOpenCallback<Vtab : sqlite3_vtab, VtabCursor : sqlite3_vtab_cursor> {

    /**
     * Result for [apply].
     */
    public sealed interface Result<out VtabCursor : sqlite3_vtab_cursor>

    /**
     * Scope for [apply].
     */
    public sealed interface Scope<VtabCursor : sqlite3_vtab_cursor> {

        /**
         * Writes [cursor] to `ppCursor` and returns [SqliteResultCode.OK] to SQLite.
         */
        public fun success(cursor: VtabCursor): Result<VtabCursor>

        /**
         * Returns [result] to SQLite.
         */
        public fun failure(result: SqliteResultCode.Failure): Result<VtabCursor>
    }

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xopen_method)
     *
     * A [Result] instance can be obtained by invoking one of [Scope.success] or [Scope.failure].
     */
    public fun Scope<VtabCursor>.apply(vTab: Vtab): Result<VtabCursor>
}