/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("ClassName", "SpellCheckingInspection")

package ksqlite.structs

/**
 * Allocates or reinterprets a `sqlite3_vfs`.
 */
public abstract class sqlite3_vfs<Pointer: Any>(
    adapter: Adapter<Pointer>,
    pointer: Pointer?
) : Struct<StructType.Sqlite3Vfs, sqlite3_vfs.Member, Pointer>(
    type = Sqlite3Vfs,
    adapter = adapter,
    pointer = pointer
) {

    public var iVersion: Int
        get() = readInt(IVERSION)
        set(value) = writeInt(IVERSION, value)

    public var szOsFile: Int
        get() = readInt(SZOSFILE)
        set(value) = writeInt(SZOSFILE, value)

    public var mxPathname: Int
        get() = readInt(MXPATHNAME)
        set(value) = writeInt(MXPATHNAME, value)

    public var pNext: Pointer
        get() = readPointer(PNEXT)
        set(value) = writePointer(PNEXT, value)

    public var zName: Pointer
        get() = readPointer(ZNAME)
        set(value) = writePointer(ZNAME, value)

    public var pAppData: Pointer
        get() = readPointer(PAPPDATA)
        set(value) = writePointer(PAPPDATA, value)

    public var xOpen: Pointer
        get() = readPointer(XOPEN)
        set(value) = writePointer(XOPEN, value)

    public var xDelete: Pointer
        get() = readPointer(XDELETE)
        set(value) = writePointer(XDELETE, value)

    public var xAccess: Pointer
        get() = readPointer(XACCESS)
        set(value) = writePointer(XACCESS, value)

    public var xFullPathname: Pointer
        get() = readPointer(XFULLPATHNAME)
        set(value) = writePointer(XFULLPATHNAME, value)

    public var xDlOpen: Pointer
        get() = readPointer(XDLOPEN)
        set(value) = writePointer(XDLOPEN, value)

    public var xDlError: Pointer
        get() = readPointer(XDLERROR)
        set(value) = writePointer(XDLERROR, value)

    public var xDlSym: Pointer
        get() = readPointer(XDLSYM)
        set(value) = writePointer(XDLSYM, value)

    public var xDlClose: Pointer
        get() = readPointer(XDLCLOSE)
        set(value) = writePointer(XDLCLOSE, value)

    public var xRandomness: Pointer
        get() = readPointer(XRANDOMNESS)
        set(value) = writePointer(XRANDOMNESS, value)

    public var xSleep: Pointer
        get() = readPointer(XSLEEP)
        set(value) = writePointer(XSLEEP, value)

    public var xCurrentTime: Pointer
        get() = readPointer(XCURRENTTIME)
        set(value) = writePointer(XCURRENTTIME, value)

    public var xGetLastError: Pointer
        get() = readPointer(XGETLASTERROR)
        set(value) = writePointer(XGETLASTERROR, value)

    public var xCurrentTimeInt64: Pointer
        get() = readPointer(XCURRENTTIMEINT64)
        set(value) = writePointer(XCURRENTTIMEINT64, value)

    public var xSetSystemCall: Pointer
        get() = readPointer(XSETSYSTEMCALL)
        set(value) = writePointer(XSETSYSTEMCALL, value)

    public var xGetSystemCall: Pointer
        get() = readPointer(XGETSYSTEMCALL)
        set(value) = writePointer(XGETSYSTEMCALL, value)

    public var xNextSystemCall: Pointer
        get() = readPointer(XNEXTSYSTEMCALL)
        set(value) = writePointer(XNEXTSYSTEMCALL, value)

    /**
     * Members of the `sqlite3_vfs` struct.
     */
    public enum class Member : StructMember<StructType.Sqlite3Vfs> {
        IVERSION,
        SZOSFILE,
        MXPATHNAME,
        PNEXT,
        ZNAME,
        PAPPDATA,
        XOPEN,
        XDELETE,
        XACCESS,
        XFULLPATHNAME,
        XDLOPEN,
        XDLERROR,
        XDLSYM,
        XDLCLOSE,
        XRANDOMNESS,
        XSLEEP,
        XCURRENTTIME,
        XGETLASTERROR,
        XCURRENTTIMEINT64,
        XSETSYSTEMCALL,
        XGETSYSTEMCALL,
        XNEXTSYSTEMCALL,
    }
}