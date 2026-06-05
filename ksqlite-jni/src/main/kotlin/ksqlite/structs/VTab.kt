@file:Suppress("ClassName", "SpellCheckingInspection")

package ksqlite.structs

import ksqlite.structLayout
import ksqlite.structMalloc

/**
 * Allocates an instance of `sqlite3_vtab` and supplies getters and setters for reading and writing
 * the struct.
 */
public class sqlite3_vtab : JniStruct(layout, { structMalloc(StructType.Vtab, it) }) {

    public var pModule: Long
        get() = readLong(LAYOUT_INDEX_PMODULE)
        set(value) = writeLong(LAYOUT_INDEX_PMODULE, value)

    public var nRef: Int
        get() = readInt(LAYOUT_INDEX_NREF)
        set(value) = writeInt(LAYOUT_INDEX_NREF, value)

    public var zErrMsg: Long
        get() = readLong(LAYOUT_INDEX_ZERRMSG)
        set(value) = writeLong(LAYOUT_INDEX_ZERRMSG, value)

    private companion object Layout {

        val layout by lazy { structLayout(StructType.Vtab) }

        const val LAYOUT_INDEX_PMODULE = 0
        const val LAYOUT_INDEX_NREF = 1
        const val LAYOUT_INDEX_ZERRMSG = 2
    }
}