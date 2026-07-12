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

import ksqlite.capi.sqlite3_value
import ksqlite.capi.vtab.sqlite3_vtab_cursor
import ksqlite.types.SqliteResultCode

/**
 * This method begins a search of a virtual table. The first argument is a cursor opened by xOpen.
 * The next two arguments define a particular search index previously chosen by  xBestIndex. The
 * specific meanings of idxNum and idxStr are unimportant as long as xFilter and xBestIndex agree on
 * what that meaning is.
 *
 * [The xFilter Method](https://sqlite.org/vtab.html#the_xfilter_method)
 */
public fun interface SqliteVtabFilterCallback<VtabCursor : sqlite3_vtab_cursor> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xfilter_method).
     */
    public fun apply(
        cursor: VtabCursor,
        idxNum: Int,
        idxStr: String?,
        arguments: Array<sqlite3_value>
    ): SqliteResultCode.OkOrFailure
}