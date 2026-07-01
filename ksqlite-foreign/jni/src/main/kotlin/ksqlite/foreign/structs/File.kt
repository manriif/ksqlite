@file:Suppress("ClassName", "SpellCheckingInspection")

package ksqlite.foreign.structs

import ksqlite.foreign.structLayout

/**
 * Allocates an instance of `sqlite3_file` and supplies getters and setters for reading and writing
 * the struct.
 */
public class sqlite3_file : JniStruct(layout, StructType.File) {

    public var pMethods: Long
        get() = readLong(STRUCT_MEMBER_INDEX_PMETHODS)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_PMETHODS, value)

    public companion object Layout {

        internal val layout by lazy { structLayout(StructType.File) }

        public const val STRUCT_MEMBER_INDEX_PMETHODS: Int = 0
    }
}