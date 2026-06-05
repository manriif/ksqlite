@file:Suppress("ClassName", "SpellCheckingInspection")

package ksqlite.structs

import ksqlite.structLayout
import ksqlite.structMalloc

/**
 * Allocates an instance of `sqlite3_vtab_cursor` and supplies getters and setters for reading and
 * writing the struct.
 */
public class sqlite3_vtab_cursor : JniStruct(layout, { structMalloc(StructType.VtabCursor, it) }) {

    public var pVtab: Int
        get() = readInt(LAYOUT_INDEX_PVTAB)
        set(value) = writeInt(LAYOUT_INDEX_PVTAB, value)

    private companion object Layout {

        val layout by lazy { structLayout(StructType.VtabCursor) }

        const val LAYOUT_INDEX_PVTAB = 0
    }
}