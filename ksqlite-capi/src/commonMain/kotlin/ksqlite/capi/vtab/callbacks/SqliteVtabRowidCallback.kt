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
@file:Suppress("SpellCheckingInspection")

package ksqlite.capi.vtab.callbacks

import ksqlite.capi.vtab.sqlite3_vtab_cursor
import ksqlite.types.SqliteResultCode

/**
 * A successful invocation of this method will cause *pRowid to be filled with the rowid of row that
 * the virtual table cursor pCur is currently pointing at. This method returns SQLITE_OK on success.
 * It returns an appropriate error code on failure.
 *
 * [The xRowid Method](https://sqlite.org/vtab.html#the_xrowid_method)
 */
public fun interface SqliteVtabRowidCallback<VtabCursor : sqlite3_vtab_cursor> {

    /**
     * Result for [apply].
     */
    public sealed interface Result

    /**
     * Scope for [apply].
     */
    public sealed interface Scope {

        /**
         * Writes [rowid] to `pRowid` and returns [SqliteResultCode.OK] to SQLite.
         */
        public fun success(rowid: Long): Result

        /**
         * Returns [result] to SQLite.
         */
        public fun failure(result: SqliteResultCode.Failure): Result
    }

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xrowid_method)
     *
     * A [Result] instance can be obtained by invoking one of [Scope.success] or [Scope.failure].
     */
    public fun Scope.apply(cursor: VtabCursor): Result
}