@file:Suppress("ClassName", "SpellCheckingInspection")

package ksqlite.structs

import ksqlite.structLayout
import ksqlite.structMalloc

/**
 * Allocates an instance of `sqlite3_module` and supplies getters and setters for reading and
 * writing the struct.
 */
public class sqlite3_module : JniStruct(
    structMalloc(StructType.Module), layout) {

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

    public var xIntegrity: Long
		get() = readLong(LAYOUT_INDEX_XINTEGRITY)
		set(value) = writeLong(LAYOUT_INDEX_XINTEGRITY, value)

    private companion object Layout {

        val layout by lazy { structLayout(StructType.Module) }

        const val LAYOUT_INDEX_IVERSION = 0
        const val LAYOUT_INDEX_XCREATE = 1
        const val LAYOUT_INDEX_XCONNECT = 2
        const val LAYOUT_INDEX_XBESTINDEX = 3
        const val LAYOUT_INDEX_XDISCONNECT = 4
        const val LAYOUT_INDEX_XDESTROY = 5
        const val LAYOUT_INDEX_XOPEN = 6
        const val LAYOUT_INDEX_XCLOSE = 7
        const val LAYOUT_INDEX_XFILTER = 8
        const val LAYOUT_INDEX_XNEXT = 9
        const val LAYOUT_INDEX_XEOF = 10
        const val LAYOUT_INDEX_XCOLUMN = 11
        const val LAYOUT_INDEX_XROWID = 12
        const val LAYOUT_INDEX_XUPDATE = 13
        const val LAYOUT_INDEX_XBEGIN = 14
        const val LAYOUT_INDEX_XSYNC = 15
        const val LAYOUT_INDEX_XCOMMIT = 16
        const val LAYOUT_INDEX_XROLLBACK = 17
        const val LAYOUT_INDEX_XFINDFUNCTION = 18
        const val LAYOUT_INDEX_XRENAME = 19
        const val LAYOUT_INDEX_XSAVEPOINT = 20
        const val LAYOUT_INDEX_XRELEASE = 21
        const val LAYOUT_INDEX_XROLLBACKTO = 22
        const val LAYOUT_INDEX_XINTEGRITY = 23
    }
}