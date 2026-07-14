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
@file:Suppress("PropertyName", "SpellCheckingInspection")

package ksqlite.foreign

import ksqlite.foreign.structs.StructCtor
import ksqlite.foreign.structs.sqlite3_file
import ksqlite.foreign.structs.sqlite3_index_info
import ksqlite.foreign.structs.sqlite3_io_methods
import ksqlite.foreign.structs.sqlite3_module
import ksqlite.foreign.structs.sqlite3_vfs
import ksqlite.foreign.structs.sqlite3_vtab
import ksqlite.foreign.structs.sqlite3_vtab_cursor
import kotlin.js.JsAny

/**
 * SQLite C-API exposed functions and objects.
 */
public external interface Sqlite3Capi : JsAny {

    public val SQLITE_TRANSIENT: Int

    /**
     * Returns the constructor to [sqlite3_file].
     */
    public val sqlite3_file: StructCtor<sqlite3_file>

    /**
     * Returns the constructor to [sqlite3_index_info].
     */
    public val sqlite3_index_info: StructCtor<sqlite3_index_info>

    /**
     * Returns the constructor to [sqlite3_io_methods].
     */
    public val sqlite3_io_methods: StructCtor<sqlite3_io_methods>

    /**
     * Returns the constructor to [sqlite3_module].
     */
    public val sqlite3_module: StructCtor<sqlite3_module>

    /**
     * Returns the constructor to [sqlite3_vfs].
     */
    public val sqlite3_vfs: StructCtor<sqlite3_vfs>

    /**
     * Returns the constructor to [sqlite3_vtab].
     */
    public val sqlite3_vtab: StructCtor<sqlite3_vtab>

    /**
     * Returns the constructor to [sqlite3_vtab_cursor].
     */
    public val sqlite3_vtab_cursor: StructCtor<sqlite3_vtab_cursor>
}