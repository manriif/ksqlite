@file:Suppress("ClassName", "SpellCheckingInspection")

package ksqlite.structs

import ksqlite.structLayout

/**
 * Allocates an instance of `sqlite3_vtab` and supplies getters and setters for reading and writing
 * the struct.
 */
public class sqlite3_vtab : JniStruct(layout, StructType.Vtab) {

    public var pModule: Long
        get() = readLong(LAYOUT_INDEX_PMODULE)
        set(value) = writeLong(LAYOUT_INDEX_PMODULE, value)

    public var nRef: Int
        get() = readInt(LAYOUT_INDEX_NREF)
        set(value) = writeInt(LAYOUT_INDEX_NREF, value)

    public var zErrMsg: Long
        get() = readLong(LAYOUT_INDEX_ZERRMSG)
        set(value) = writeLong(LAYOUT_INDEX_ZERRMSG, value)

    public companion object Layout {

        internal val layout by lazy { structLayout(StructType.Vtab) }

        public const val LAYOUT_INDEX_PMODULE: Int = 0
        public const val LAYOUT_INDEX_NREF: Int = 1
        public const val LAYOUT_INDEX_ZERRMSG: Int = 2
    }
}