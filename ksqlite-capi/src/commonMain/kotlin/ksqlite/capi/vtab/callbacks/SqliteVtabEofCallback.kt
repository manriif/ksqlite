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

import ksqlite.capi.vtab.sqlite3_vtab_cursor

/**
 * The xEof method must return false (zero) if the specified cursor currently points to a valid row
 * of data, or true (non-zero) otherwise. This method is called by the SQL engine immediately after
 * each xFilter and xNext invocation.
 *
 * [The xEof Method](https://sqlite.org/vtab.html#the_xeof_method)
 */
public fun interface SqliteVtabEofCallback<VtabCursor : sqlite3_vtab_cursor> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xeof_method).
     */
    public fun apply(cursor: VtabCursor): Int
}