@file:Suppress("PropertyName", "SpellCheckingInspection")

package ksqlite.foreign

import ksqlite.foreign.structs.StructCtor
import ksqlite.foreign.structs.sqlite3_index_info
import ksqlite.foreign.structs.sqlite3_module
import ksqlite.foreign.structs.sqlite3_vtab
import ksqlite.foreign.structs.sqlite3_vtab_cursor
import kotlin.js.JsAny

/**
 * SQLite C-API exposed functions and objects.
 */
public external interface Sqlite3Capi : JsAny {

    public val SQLITE_TRANSIENT: Int

    /**
     * Returns the constructor to [sqlite3_index_info].
     */
    public val sqlite3_index_info: StructCtor<sqlite3_index_info>

    /**
     * Returns the constructor to [sqlite3_module].
     */
    public val sqlite3_module: StructCtor<sqlite3_module>

    /**
     * Returns the constructor to [sqlite3_vtab].
     */
    public val sqlite3_vtab: StructCtor<sqlite3_vtab>

    /**
     * Returns the constructor to [sqlite3_vtab_cursor].
     */
    public val sqlite3_vtab_cursor: StructCtor<sqlite3_vtab_cursor>
}