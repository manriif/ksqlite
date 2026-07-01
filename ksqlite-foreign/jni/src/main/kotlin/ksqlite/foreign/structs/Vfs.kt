@file:Suppress("ClassName", "SpellCheckingInspection")

package ksqlite.foreign.structs

import ksqlite.foreign.structLayout

/**
 * Wraps an instance of `sqlite3_vfs` at address [pointer] and supplies getters and setters
 * for reading and writing the struct.
 */
public class sqlite3_vfs(pointer: Long) : JniStruct(layout, StructType.Vfs, pointer) {

    public var iVersion: Int
        get() = readInt(STRUCT_MEMBER_INDEX_IVERSION)
        set(value) = writeInt(STRUCT_MEMBER_INDEX_IVERSION, value)

    public var szOsFile: Int
        get() = readInt(STRUCT_MEMBER_INDEX_SZOSFILE)
        set(value) = writeInt(STRUCT_MEMBER_INDEX_SZOSFILE, value)

    public var mxPathname: Int
        get() = readInt(STRUCT_MEMBER_INDEX_MXPATHNAME)
        set(value) = writeInt(STRUCT_MEMBER_INDEX_MXPATHNAME, value)

    public var pNext: Long
        get() = readLong(STRUCT_MEMBER_INDEX_PNEXT)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_PNEXT, value)

    public var zName: Long
        get() = readLong(STRUCT_MEMBER_INDEX_ZNAME)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_ZNAME, value)

    public var pAppData: Long
        get() = readLong(STRUCT_MEMBER_INDEX_PAPPDATA)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_PAPPDATA, value)

    public var xOpen: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XOPEN)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XOPEN, value)

    public var xDelete: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XDELETE)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XDELETE, value)

    public var xAccess: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XACCESS)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XACCESS, value)

    public var xFullPathname: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XFULLPATHNAME)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XFULLPATHNAME, value)

    public var xDlOpen: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XDLOPEN)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XDLOPEN, value)

    public var xDlError: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XDLERROR)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XDLERROR, value)

    public var xDlSym: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XDLSYM)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XDLSYM, value)

    public var xDlClose: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XDLCLOSE)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XDLCLOSE, value)

    public var xRandomness: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XRANDOMNESS)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XRANDOMNESS, value)

    public var xSleep: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XSLEEP)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XSLEEP, value)

    public var xCurrentTime: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XCURRENTTIME)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XCURRENTTIME, value)

    public var xGetLastError: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XGETLASTERROR)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XGETLASTERROR, value)

    public var xCurrentTimeInt64: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XCURRENTTIMEINT64)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XCURRENTTIMEINT64, value)

    public var xSetSystemCall: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XSETSYSTEMCALL)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XSETSYSTEMCALL, value)

    public var xGetSystemCall: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XGETSYSTEMCALL)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XGETSYSTEMCALL, value)

    public var xNextSystemCall: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XNEXTSYSTEMCALL)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XNEXTSYSTEMCALL, value)

    public companion object Layout {

        internal val layout by lazy { structLayout(StructType.Vfs) }

        public const val STRUCT_MEMBER_INDEX_IVERSION: Int = 0
        public const val STRUCT_MEMBER_INDEX_SZOSFILE: Int = 1
        public const val STRUCT_MEMBER_INDEX_MXPATHNAME: Int = 2
        public const val STRUCT_MEMBER_INDEX_PNEXT: Int = 3
        public const val STRUCT_MEMBER_INDEX_ZNAME: Int = 4
        public const val STRUCT_MEMBER_INDEX_PAPPDATA: Int = 5
        public const val STRUCT_MEMBER_INDEX_XOPEN: Int = 6
        public const val STRUCT_MEMBER_INDEX_XDELETE: Int = 7
        public const val STRUCT_MEMBER_INDEX_XACCESS: Int = 8
        public const val STRUCT_MEMBER_INDEX_XFULLPATHNAME: Int = 9
        public const val STRUCT_MEMBER_INDEX_XDLOPEN: Int = 10
        public const val STRUCT_MEMBER_INDEX_XDLERROR: Int = 11
        public const val STRUCT_MEMBER_INDEX_XDLSYM: Int = 12
        public const val STRUCT_MEMBER_INDEX_XDLCLOSE: Int = 13
        public const val STRUCT_MEMBER_INDEX_XRANDOMNESS: Int = 14
        public const val STRUCT_MEMBER_INDEX_XSLEEP: Int = 15
        public const val STRUCT_MEMBER_INDEX_XCURRENTTIME: Int = 16
        public const val STRUCT_MEMBER_INDEX_XGETLASTERROR: Int = 17
        public const val STRUCT_MEMBER_INDEX_XCURRENTTIMEINT64: Int = 18
        public const val STRUCT_MEMBER_INDEX_XSETSYSTEMCALL: Int = 19
        public const val STRUCT_MEMBER_INDEX_XGETSYSTEMCALL: Int = 20
        public const val STRUCT_MEMBER_INDEX_XNEXTSYSTEMCALL: Int = 21
    }
}