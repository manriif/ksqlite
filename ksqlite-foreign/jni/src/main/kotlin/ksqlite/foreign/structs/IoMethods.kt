@file:Suppress("ClassName", "SpellCheckingInspection")

package ksqlite.foreign.structs

import ksqlite.foreign.structLayout

/**
 * Wraps an instance of `sqlite3_io_methods` at address [pointer] and supplies getters and setters
 * for reading and writing the struct.
 */
public class sqlite3_io_methods(pointer: Long) : JniStruct(layout, StructType.IoMethods, pointer) {

    public var iVersion: Int
        get() = readInt(STRUCT_MEMBER_INDEX_IVERSION)
        set(value) = writeInt(STRUCT_MEMBER_INDEX_IVERSION, value)

    public var xClose: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XCLOSE)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XCLOSE, value)

    public var xRead: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XREAD)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XREAD, value)

    public var xWrite: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XWRITE)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XWRITE, value)

    public var xTruncate: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XTRUNCATE)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XTRUNCATE, value)

    public var xSync: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XSYNC)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XSYNC, value)

    public var xFileSize: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XFILESIZE)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XFILESIZE, value)

    public var xLock: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XLOCK)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XLOCK, value)

    public var xUnlock: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XUNLOCK)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XUNLOCK, value)

    public var xCheckReservedLock: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XCHECKRESERVEDLOCK)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XCHECKRESERVEDLOCK, value)

    public var xFileControl: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XFILECONTROL)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XFILECONTROL, value)

    public var xSectorSize: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XSECTORSIZE)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XSECTORSIZE, value)

    public var xDeviceCharacteristics: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XDEVICECHARACTERISTICS)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XDEVICECHARACTERISTICS, value)

    public var xShmMap: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XSHMMAP)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XSHMMAP, value)

    public var xShmLock: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XSHMLOCK)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XSHMLOCK, value)

    public var xShmBarrier: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XSHMBARRIER)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XSHMBARRIER, value)

    public var xShmUnmap: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XSHMUNMAP)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XSHMUNMAP, value)

    public var xFetch: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XFETCH)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XFETCH, value)

    public var xUnfetch: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XUNFETCH)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XUNFETCH, value)

    public companion object Layout {

        internal val layout by lazy { structLayout(StructType.IoMethods) }

        public const val STRUCT_MEMBER_INDEX_IVERSION: Int = 0
        public const val STRUCT_MEMBER_INDEX_XCLOSE: Int = 1
        public const val STRUCT_MEMBER_INDEX_XREAD: Int = 2
        public const val STRUCT_MEMBER_INDEX_XWRITE: Int = 3
        public const val STRUCT_MEMBER_INDEX_XTRUNCATE: Int = 4
        public const val STRUCT_MEMBER_INDEX_XSYNC: Int = 5
        public const val STRUCT_MEMBER_INDEX_XFILESIZE: Int = 6
        public const val STRUCT_MEMBER_INDEX_XLOCK: Int = 7
        public const val STRUCT_MEMBER_INDEX_XUNLOCK: Int = 8
        public const val STRUCT_MEMBER_INDEX_XCHECKRESERVEDLOCK: Int = 9
        public const val STRUCT_MEMBER_INDEX_XFILECONTROL: Int = 10
        public const val STRUCT_MEMBER_INDEX_XSECTORSIZE: Int = 11
        public const val STRUCT_MEMBER_INDEX_XDEVICECHARACTERISTICS: Int = 12
        public const val STRUCT_MEMBER_INDEX_XSHMMAP: Int = 13
        public const val STRUCT_MEMBER_INDEX_XSHMLOCK: Int = 14
        public const val STRUCT_MEMBER_INDEX_XSHMBARRIER: Int = 15
        public const val STRUCT_MEMBER_INDEX_XSHMUNMAP: Int = 16
        public const val STRUCT_MEMBER_INDEX_XFETCH: Int = 17
        public const val STRUCT_MEMBER_INDEX_XUNFETCH: Int = 18
    }
}