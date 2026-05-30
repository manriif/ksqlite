@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.memory.StructPointer

/**
 * The sqlite3_index_info structure and its substructures is used as part of the virtual table
 * interface to pass information into and receive the reply from the xBestIndex method of a virtual
 * table module.
 *
 * [sqlite3_index_info](https://sqlite.org/c3ref/index_info.html)
 */
public expect class sqlite3_index_info : StructPointer, Sqlite3IndexInfo

/**
 * This structure, sometimes called a "virtual table module", defines the implementation of a
 * virtual table. This structure consists mostly of methods for the module.
 *
 * [sqlite3_module](https://sqlite.org/c3ref/module.html)
 */
public expect class sqlite3_module : StructPointer

/**
 * Every virtual table module implementation uses a subclass of this object to describe a particular
 * instance of the virtual table. Each subclass will be tailored to the specific needs of the module
 * implementation. The purpose of this superclass is to define certain fields that are common to all
 * module implementations.
 *
 * [sqlite3_vtab](https://sqlite.org/c3ref/vtab.html)
 */
public expect class sqlite3_vtab : StructPointer

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
 */
public expect class sqlite3_vtab_cursor : StructPointer