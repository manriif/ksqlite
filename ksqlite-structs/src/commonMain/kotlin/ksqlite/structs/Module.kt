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
 * Allocates or reinterprets a `sqlite3_module`.
 */
public abstract class sqlite3_module<Pointer : Any>(
    adapter: Adapter<Pointer>,
    pointer: Pointer?
) : Struct<StructType.Sqlite3Module, sqlite3_module.Member, Pointer>(
    type = Sqlite3Module,
    adapter = adapter,
    pointer = pointer
) {

    public var iVersion: Int
        get() = readInt(IVERSION)
        set(value) = writeInt(IVERSION, value)

    public var xCreate: Pointer
        get() = readPointer(XCREATE)
        set(value) = writePointer(XCREATE, value)

    public var xConnect: Pointer
        get() = readPointer(XCONNECT)
        set(value) = writePointer(XCONNECT, value)

    public var xBestIndex: Pointer
        get() = readPointer(XBESTINDEX)
        set(value) = writePointer(XBESTINDEX, value)

    public var xDisconnect: Pointer
        get() = readPointer(XDISCONNECT)
        set(value) = writePointer(XDISCONNECT, value)

    public var xDestroy: Pointer
        get() = readPointer(XDESTROY)
        set(value) = writePointer(XDESTROY, value)

    public var xOpen: Pointer
        get() = readPointer(XOPEN)
        set(value) = writePointer(XOPEN, value)

    public var xClose: Pointer
        get() = readPointer(XCLOSE)
        set(value) = writePointer(XCLOSE, value)

    public var xFilter: Pointer
        get() = readPointer(XFILTER)
        set(value) = writePointer(XFILTER, value)

    public var xNext: Pointer
        get() = readPointer(XNEXT)
        set(value) = writePointer(XNEXT, value)

    public var xEof: Pointer
        get() = readPointer(XEOF)
        set(value) = writePointer(XEOF, value)

    public var xColumn: Pointer
        get() = readPointer(XCOLUMN)
        set(value) = writePointer(XCOLUMN, value)

    public var xRowid: Pointer
        get() = readPointer(XROWID)
        set(value) = writePointer(XROWID, value)

    public var xUpdate: Pointer
        get() = readPointer(XUPDATE)
        set(value) = writePointer(XUPDATE, value)

    public var xBegin: Pointer
        get() = readPointer(XBEGIN)
        set(value) = writePointer(XBEGIN, value)

    public var xSync: Pointer
        get() = readPointer(XSYNC)
        set(value) = writePointer(XSYNC, value)

    public var xCommit: Pointer
        get() = readPointer(XCOMMIT)
        set(value) = writePointer(XCOMMIT, value)

    public var xRollback: Pointer
        get() = readPointer(XROLLBACK)
        set(value) = writePointer(XROLLBACK, value)

    public var xFindFunction: Pointer
        get() = readPointer(XFINDFUNCTION)
        set(value) = writePointer(XFINDFUNCTION, value)

    public var xRename: Pointer
        get() = readPointer(XRENAME)
        set(value) = writePointer(XRENAME, value)

    public var xSavepoint: Pointer
        get() = readPointer(XSAVEPOINT)
        set(value) = writePointer(XSAVEPOINT, value)

    public var xRelease: Pointer
        get() = readPointer(XRELEASE)
        set(value) = writePointer(XRELEASE, value)

    public var xRollbackTo: Pointer
        get() = readPointer(XROLLBACKTO)
        set(value) = writePointer(XROLLBACKTO, value)

    public var xShadowName: Pointer
        get() = readPointer(XSHADOWNAME)
        set(value) = writePointer(XSHADOWNAME, value)

    public var xIntegrity: Pointer
        get() = readPointer(XINTEGRITY)
        set(value) = writePointer(XINTEGRITY, value)

    /**
     * Members of the `sqlite3_module` struct.
     */
    public enum class Member : StructMember<StructType.Sqlite3Module> {
        IVERSION,
        XCREATE,
        XCONNECT,
        XBESTINDEX,
        XDISCONNECT,
        XDESTROY,
        XOPEN,
        XCLOSE,
        XFILTER,
        XNEXT,
        XEOF,
        XCOLUMN,
        XROWID,
        XUPDATE,
        XBEGIN,
        XSYNC,
        XCOMMIT,
        XROLLBACK,
        XFINDFUNCTION,
        XRENAME,
        XSAVEPOINT,
        XRELEASE,
        XROLLBACKTO,
        XSHADOWNAME,
        XINTEGRITY,
    }
}