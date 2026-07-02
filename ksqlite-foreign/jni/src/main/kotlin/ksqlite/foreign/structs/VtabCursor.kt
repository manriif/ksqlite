@file:Suppress("ClassName", "SpellCheckingInspection")

package ksqlite.foreign.structs

import ksqlite.foreign.structLayout

/**
 * Allocates an instance of `sqlite3_vtab_cursor` and supplies getters and setters for reading and
 * writing the struct.
 */
public class sqlite3_vtab_cursor : JniStruct(layout) {

    public var pVtab: Int
        get() = readInt(STRUCT_MEMBER_INDEX_PVTAB)
        set(value) = writeInt(STRUCT_MEMBER_INDEX_PVTAB, value)

    public companion object Layout {

        internal val layout by lazy { structLayout(StructType.VtabCursor) }

        public const val STRUCT_MEMBER_INDEX_PVTAB: Int = 0
    }
}