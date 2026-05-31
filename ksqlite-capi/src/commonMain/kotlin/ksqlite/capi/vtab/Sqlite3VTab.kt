@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.StructPointer

/**
 * Every virtual table module implementation uses a subclass of this object to describe a particular
 * instance of the virtual table. Each subclass will be tailored to the specific needs of the module
 * implementation. The purpose of this superclass is to define certain fields that are common to all
 * module implementations.
 *
 * [sqlite3_vtab](https://sqlite.org/c3ref/vtab.html)
 */
public expect abstract class sqlite3_vtab() : StructPointer, MemoryScope {

    /**
     * Number of open cursor.
     */
    public val nRef: Int

    /**
     * Error message that can be set from virtual table methods.
     * Setting a value to this field automatically free any previously existing value.
     */
    public var errMsg: String?
}