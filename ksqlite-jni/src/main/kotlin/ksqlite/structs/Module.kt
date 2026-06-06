@file:Suppress("ClassName", "SpellCheckingInspection")

package ksqlite.structs

import ksqlite.structLayout

/**
 * Allocates an instance of `sqlite3_module` and supplies getters and setters for reading and
 * writing the struct.
 */
public class sqlite3_module : JniStruct(layout, StructType.Module) {

    public var iVersion: Int
        get() = readInt(LAYOUT_INDEX_IVERSION)
        set(value) = writeInt(LAYOUT_INDEX_IVERSION, value)

    public var xCreate: Long
        get() = readLong(LAYOUT_INDEX_XCREATE)
        set(value) = writeLong(LAYOUT_INDEX_XCREATE, value)

    public var xConnect: Long
        get() = readLong(LAYOUT_INDEX_XCONNECT)
        set(value) = writeLong(LAYOUT_INDEX_XCONNECT, value)

    public var xBestIndex: Long
        get() = readLong(LAYOUT_INDEX_XBESTINDEX)
        set(value) = writeLong(LAYOUT_INDEX_XBESTINDEX, value)

    public var xDisconnect: Long
        get() = readLong(LAYOUT_INDEX_XDISCONNECT)
        set(value) = writeLong(LAYOUT_INDEX_XDISCONNECT, value)

    public var xDestroy: Long
        get() = readLong(LAYOUT_INDEX_XDESTROY)
        set(value) = writeLong(LAYOUT_INDEX_XDESTROY, value)

    public var xOpen: Long
        get() = readLong(LAYOUT_INDEX_XOPEN)
        set(value) = writeLong(LAYOUT_INDEX_XOPEN, value)

    public var xClose: Long
        get() = readLong(LAYOUT_INDEX_XCLOSE)
        set(value) = writeLong(LAYOUT_INDEX_XCLOSE, value)

    public var xFilter: Long
        get() = readLong(LAYOUT_INDEX_XFILTER)
        set(value) = writeLong(LAYOUT_INDEX_XFILTER, value)

    public var xNext: Long
        get() = readLong(LAYOUT_INDEX_XNEXT)
        set(value) = writeLong(LAYOUT_INDEX_XNEXT, value)

    public var xEof: Long
        get() = readLong(LAYOUT_INDEX_XEOF)
        set(value) = writeLong(LAYOUT_INDEX_XEOF, value)

    public var xColumn: Long
        get() = readLong(LAYOUT_INDEX_XCOLUMN)
        set(value) = writeLong(LAYOUT_INDEX_XCOLUMN, value)

    public var xRowid: Long
        get() = readLong(LAYOUT_INDEX_XROWID)
        set(value) = writeLong(LAYOUT_INDEX_XROWID, value)

    public var xUpdate: Long
        get() = readLong(LAYOUT_INDEX_XUPDATE)
        set(value) = writeLong(LAYOUT_INDEX_XUPDATE, value)

    public var xBegin: Long
        get() = readLong(LAYOUT_INDEX_XBEGIN)
        set(value) = writeLong(LAYOUT_INDEX_XBEGIN, value)

    public var xSync: Long
        get() = readLong(LAYOUT_INDEX_XSYNC)
        set(value) = writeLong(LAYOUT_INDEX_XSYNC, value)

    public var xCommit: Long
        get() = readLong(LAYOUT_INDEX_XCOMMIT)
        set(value) = writeLong(LAYOUT_INDEX_XCOMMIT, value)

    public var xRollback: Long
        get() = readLong(LAYOUT_INDEX_XROLLBACK)
        set(value) = writeLong(LAYOUT_INDEX_XROLLBACK, value)

    public var xFindFunction: Long
        get() = readLong(LAYOUT_INDEX_XFINDFUNCTION)
        set(value) = writeLong(LAYOUT_INDEX_XFINDFUNCTION, value)

    public var xRename: Long
        get() = readLong(LAYOUT_INDEX_XRENAME)
        set(value) = writeLong(LAYOUT_INDEX_XRENAME, value)

    public var xSavepoint: Long
        get() = readLong(LAYOUT_INDEX_XSAVEPOINT)
        set(value) = writeLong(LAYOUT_INDEX_XSAVEPOINT, value)

    public var xRelease: Long
        get() = readLong(LAYOUT_INDEX_XRELEASE)
        set(value) = writeLong(LAYOUT_INDEX_XRELEASE, value)

    public var xRollbackTo: Long
        get() = readLong(LAYOUT_INDEX_XROLLBACKTO)
        set(value) = writeLong(LAYOUT_INDEX_XROLLBACKTO, value)

    public var xShadowName: Long
        get() = readLong(LAYOUT_INDEX_XSHADOWNAME)
        set(value) = writeLong(LAYOUT_INDEX_XSHADOWNAME, value)

    public var xIntegrity: Long
        get() = readLong(LAYOUT_INDEX_XINTEGRITY)
        set(value) = writeLong(LAYOUT_INDEX_XINTEGRITY, value)

    public companion object Layout {

        internal val layout by lazy { structLayout(StructType.Module) }

        public const val LAYOUT_INDEX_IVERSION: Int = 0
        public const val LAYOUT_INDEX_XCREATE: Int = 1
        public const val LAYOUT_INDEX_XCONNECT: Int = 2
        public const val LAYOUT_INDEX_XBESTINDEX: Int = 3
        public const val LAYOUT_INDEX_XDISCONNECT: Int = 4
        public const val LAYOUT_INDEX_XDESTROY: Int = 5
        public const val LAYOUT_INDEX_XOPEN: Int = 6
        public const val LAYOUT_INDEX_XCLOSE: Int = 7
        public const val LAYOUT_INDEX_XFILTER: Int = 8
        public const val LAYOUT_INDEX_XNEXT: Int = 9
        public const val LAYOUT_INDEX_XEOF: Int = 10
        public const val LAYOUT_INDEX_XCOLUMN: Int = 11
        public const val LAYOUT_INDEX_XROWID: Int = 12
        public const val LAYOUT_INDEX_XUPDATE: Int = 13
        public const val LAYOUT_INDEX_XBEGIN: Int = 14
        public const val LAYOUT_INDEX_XSYNC: Int = 15
        public const val LAYOUT_INDEX_XCOMMIT: Int = 16
        public const val LAYOUT_INDEX_XROLLBACK: Int = 17
        public const val LAYOUT_INDEX_XFINDFUNCTION: Int = 18
        public const val LAYOUT_INDEX_XRENAME: Int = 19
        public const val LAYOUT_INDEX_XSAVEPOINT: Int = 20
        public const val LAYOUT_INDEX_XRELEASE: Int = 21
        public const val LAYOUT_INDEX_XROLLBACKTO: Int = 22
        public const val LAYOUT_INDEX_XSHADOWNAME: Int = 23
        public const val LAYOUT_INDEX_XINTEGRITY: Int = 24
    }
}