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

import ksqlite.capi.sqlite3_context
import ksqlite.capi.vtab.sqlite3_vtab_cursor
import ksqlite.types.SqliteResultCode

/**
 * The SQLite core invokes this method in order to find the value for the N-th column of the current
 * row. N is zero-based so the first column is numbered 0. The xColumn method may return its result
 * back to SQLite using one oof the following interfaces:
 * 
 * sqlite3_result_blob()
 * sqlite3_result_blob64()
 * sqlite3_result_double()
 * sqlite3_result_int()
 * sqlite3_result_int64()
 * sqlite3_result_null()
 * sqlite3_result_pointer()
 * sqlite3_result_text()
 * sqlite3_result_text16()
 * sqlite3_result_text16le()
 * sqlite3_result_text16be()
 * sqlite3_result_text64()
 * sqlite3_result_value()
 * sqlite3_result_zeroblob()
 * sqlite3_result_zeroblob64()
 *
 * [The xColumn Method](https://sqlite.org/vtab.html#the_xcolumn_method)
 */
public fun interface SqliteVtabColumnCallback<VtabCursor : sqlite3_vtab_cursor> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xcolumn_method).
     */
    public fun apply(
        cursor: VtabCursor,
        context: sqlite3_context,
        columnIndex: Int
    ): SqliteResultCode.OkOrFailure
}