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
 * Allocates or reinterprets a `sqlite3_io_methods`.
 */
public abstract class sqlite3_io_methods<Pointer : Any>(
    adapter: Adapter<Pointer>,
    pointer: Pointer?
) : Struct<StructType.Sqlite3IoMethods, sqlite3_io_methods.Member, Pointer>(
    type = Sqlite3IoMethods,
    adapter = adapter,
    pointer = pointer
) {

    public var iVersion: Int
        get() = readInt(IVERSION)
        set(value) = writeInt(IVERSION, value)

    public var xClose: Pointer
        get() = readPointer(XCLOSE)
        set(value) = writePointer(XCLOSE, value)

    public var xRead: Pointer
        get() = readPointer(XREAD)
        set(value) = writePointer(XREAD, value)

    public var xWrite: Pointer
        get() = readPointer(XWRITE)
        set(value) = writePointer(XWRITE, value)

    public var xTruncate: Pointer
        get() = readPointer(XTRUNCATE)
        set(value) = writePointer(XTRUNCATE, value)

    public var xSync: Pointer
        get() = readPointer(XSYNC)
        set(value) = writePointer(XSYNC, value)

    public var xFileSize: Pointer
        get() = readPointer(XFILESIZE)
        set(value) = writePointer(XFILESIZE, value)

    public var xLock: Pointer
        get() = readPointer(XLOCK)
        set(value) = writePointer(XLOCK, value)

    public var xUnlock: Pointer
        get() = readPointer(XUNLOCK)
        set(value) = writePointer(XUNLOCK, value)

    public var xCheckReservedLock: Pointer
        get() = readPointer(XCHECKRESERVEDLOCK)
        set(value) = writePointer(XCHECKRESERVEDLOCK, value)

    public var xFileControl: Pointer
        get() = readPointer(XFILECONTROL)
        set(value) = writePointer(XFILECONTROL, value)

    public var xSectorSize: Pointer
        get() = readPointer(XSECTORSIZE)
        set(value) = writePointer(XSECTORSIZE, value)

    public var xDeviceCharacteristics: Pointer
        get() = readPointer(XDEVICECHARACTERISTICS)
        set(value) = writePointer(XDEVICECHARACTERISTICS, value)

    public var xShmMap: Pointer
        get() = readPointer(XSHMMAP)
        set(value) = writePointer(XSHMMAP, value)

    public var xShmLock: Pointer
        get() = readPointer(XSHMLOCK)
        set(value) = writePointer(XSHMLOCK, value)

    public var xShmBarrier: Pointer
        get() = readPointer(XSHMBARRIER)
        set(value) = writePointer(XSHMBARRIER, value)

    public var xShmUnmap: Pointer
        get() = readPointer(XSHMUNMAP)
        set(value) = writePointer(XSHMUNMAP, value)

    public var xFetch: Pointer
        get() = readPointer(XFETCH)
        set(value) = writePointer(XFETCH, value)

    public var xUnfetch: Pointer
        get() = readPointer(XUNFETCH)
        set(value) = writePointer(XUNFETCH, value)

    /**
     * Members of the `sqlite3_io_methods` struct.
     */
    public enum class Member : StructMember<StructType.Sqlite3IoMethods> {
        IVERSION,
        XCLOSE,
        XREAD,
        XWRITE,
        XTRUNCATE,
        XSYNC,
        XFILESIZE,
        XLOCK,
        XUNLOCK,
        XCHECKRESERVEDLOCK,
        XFILECONTROL,
        XSECTORSIZE,
        XDEVICECHARACTERISTICS,
        XSHMMAP,
        XSHMLOCK,
        XSHMBARRIER,
        XSHMUNMAP,
        XFETCH,
        XUNFETCH,
    }
}