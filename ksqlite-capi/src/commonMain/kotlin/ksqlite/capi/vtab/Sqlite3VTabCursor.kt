@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.memory.Struct
import ksqlite.capi.types.vtab.Sqlite3VTabCursor

/**
 * Every virtual table module implementation uses a subclass of the following structure to describe
 * cursors that point into the virtual table and are used to loop through the virtual table. Cursors
 * are created using the xOpen method of the module and are destroyed by the xClose method. Cursors
 * are used by the xFilter, xNext, xEof, xColumn, and xRowid methods of the module. Each module
 * implementation will define the content of a cursor structure to suit its own needs.
 *
 * This superclass exists in order to define fields of the cursor that are common to all
 * implementations.
 *
 * [sqlite3_vtab_cursor](https://sqlite.org/c3ref/vtab_cursor.html)
 *
 * -------------------------------------------------------------------------------------------------
 *
 * # Ksqlite
 *
 * Subclasser may pass the typed [sqlite3_vtab] as a constructor parameter if necessary.
 */
public expect open class sqlite3_vtab_cursor() : Struct, Sqlite3VTabCursor