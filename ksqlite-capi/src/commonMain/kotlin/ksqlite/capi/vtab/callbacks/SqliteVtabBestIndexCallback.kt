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

import ksqlite.capi.vtab.sqlite3_index_info
import ksqlite.capi.vtab.sqlite3_vtab
import ksqlite.types.SqliteResultCode

/**
 * SQLite uses the xBestIndex method of a virtual table module to determine the best way to access
 * the virtual table.
 *
 * [The xBestIndex Method](https://sqlite.org/vtab.html#the_xbestindex_method)
 */
public fun interface SqliteVtabBestIndexCallback<Vtab : sqlite3_vtab> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xbestindex_method).
     */
    public fun apply(
        vTab: Vtab,
        info: sqlite3_index_info
    ): SqliteResultCode.OkOrFailure
}