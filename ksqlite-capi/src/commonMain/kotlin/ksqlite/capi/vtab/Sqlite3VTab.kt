@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.Struct
import ksqlite.capi.types.vtab.Sqlite3VTab

/**
 * Every virtual table module implementation uses a subclass of this object to describe a particular
 * instance of the virtual table. Each subclass will be tailored to the specific needs of the module
 * implementation. The purpose of this superclass is to define certain fields that are common to all
 * module implementations.
 *
 * [sqlite3_vtab](https://sqlite.org/c3ref/vtab.html)
 */
public expect open class sqlite3_vtab() : Struct, MemoryScope, Sqlite3VTab {
    override val nRef: Int
    override var errMsg: String?
}