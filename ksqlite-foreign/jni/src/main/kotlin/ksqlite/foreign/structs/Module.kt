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

package ksqlite.foreign.structs

import ksqlite.foreign.callbacks.VtabModuleCallbacks
import ksqlite.foreign.moduleDeinit
import ksqlite.foreign.moduleInit
import ksqlite.foreign.structLayout

/**
 * Allocates an instance of `sqlite3_module` and supplies getters and setters for reading and
 * writing the struct.
 */
public class sqlite3_module(
    callbacks: VtabModuleCallbacks,
    callbackMask: Int,
    eponymous: Boolean
) : JniStruct(layout) {

    init {
        moduleInit(pointer, callbackMask, eponymous, callbacks)
    }

    public var iVersion: Int
        get() = readInt(STRUCT_MEMBER_INDEX_IVERSION)
        set(value) = writeInt(STRUCT_MEMBER_INDEX_IVERSION, value)

    public var xCreate: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XCREATE)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XCREATE, value)

    public var xConnect: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XCONNECT)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XCONNECT, value)

    public var xBestIndex: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XBESTINDEX)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XBESTINDEX, value)

    public var xDisconnect: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XDISCONNECT)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XDISCONNECT, value)

    public var xDestroy: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XDESTROY)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XDESTROY, value)

    public var xOpen: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XOPEN)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XOPEN, value)

    public var xClose: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XCLOSE)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XCLOSE, value)

    public var xFilter: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XFILTER)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XFILTER, value)

    public var xNext: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XNEXT)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XNEXT, value)

    public var xEof: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XEOF)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XEOF, value)

    public var xColumn: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XCOLUMN)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XCOLUMN, value)

    public var xRowid: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XROWID)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XROWID, value)

    public var xUpdate: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XUPDATE)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XUPDATE, value)

    public var xBegin: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XBEGIN)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XBEGIN, value)

    public var xSync: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XSYNC)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XSYNC, value)

    public var xCommit: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XCOMMIT)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XCOMMIT, value)

    public var xRollback: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XROLLBACK)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XROLLBACK, value)

    public var xFindFunction: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XFINDFUNCTION)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XFINDFUNCTION, value)

    public var xRename: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XRENAME)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XRENAME, value)

    public var xSavepoint: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XSAVEPOINT)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XSAVEPOINT, value)

    public var xRelease: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XRELEASE)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XRELEASE, value)

    public var xRollbackTo: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XROLLBACKTO)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XROLLBACKTO, value)

    public var xShadowName: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XSHADOWNAME)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XSHADOWNAME, value)

    public var xIntegrity: Long
        get() = readLong(STRUCT_MEMBER_INDEX_XINTEGRITY)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_XINTEGRITY, value)

    override fun free() {
        moduleDeinit(pointer)
        super.free()
    }

    public companion object Layout {

        internal val layout by lazy { structLayout(StructType.Module) }

        public const val STRUCT_MEMBER_INDEX_IVERSION: Int = 0
        public const val STRUCT_MEMBER_INDEX_XCREATE: Int = 1
        public const val STRUCT_MEMBER_INDEX_XCONNECT: Int = 2
        public const val STRUCT_MEMBER_INDEX_XBESTINDEX: Int = 3
        public const val STRUCT_MEMBER_INDEX_XDISCONNECT: Int = 4
        public const val STRUCT_MEMBER_INDEX_XDESTROY: Int = 5
        public const val STRUCT_MEMBER_INDEX_XOPEN: Int = 6
        public const val STRUCT_MEMBER_INDEX_XCLOSE: Int = 7
        public const val STRUCT_MEMBER_INDEX_XFILTER: Int = 8
        public const val STRUCT_MEMBER_INDEX_XNEXT: Int = 9
        public const val STRUCT_MEMBER_INDEX_XEOF: Int = 10
        public const val STRUCT_MEMBER_INDEX_XCOLUMN: Int = 11
        public const val STRUCT_MEMBER_INDEX_XROWID: Int = 12
        public const val STRUCT_MEMBER_INDEX_XUPDATE: Int = 13
        public const val STRUCT_MEMBER_INDEX_XBEGIN: Int = 14
        public const val STRUCT_MEMBER_INDEX_XSYNC: Int = 15
        public const val STRUCT_MEMBER_INDEX_XCOMMIT: Int = 16
        public const val STRUCT_MEMBER_INDEX_XROLLBACK: Int = 17
        public const val STRUCT_MEMBER_INDEX_XFINDFUNCTION: Int = 18
        public const val STRUCT_MEMBER_INDEX_XRENAME: Int = 19
        public const val STRUCT_MEMBER_INDEX_XSAVEPOINT: Int = 20
        public const val STRUCT_MEMBER_INDEX_XRELEASE: Int = 21
        public const val STRUCT_MEMBER_INDEX_XROLLBACKTO: Int = 22
        public const val STRUCT_MEMBER_INDEX_XSHADOWNAME: Int = 23
        public const val STRUCT_MEMBER_INDEX_XINTEGRITY: Int = 24
    }
}