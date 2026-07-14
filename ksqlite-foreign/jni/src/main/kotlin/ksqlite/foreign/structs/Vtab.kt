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

import ksqlite.foreign.structLayout
import ksqlite.foreign.vTabDeinit
import ksqlite.foreign.vTabInit

/**
 * Allocates an instance of `sqlite3_vtab` and supplies getters and setters for reading and writing
 * the struct.
 */
public class sqlite3_vtab : JniStruct(layout) {

    init {
        vTabInit(pointer)
    }

    public var pModule: Long
        get() = readLong(STRUCT_MEMBER_INDEX_PMODULE)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_PMODULE, value)

    public var nRef: Int
        get() = readInt(STRUCT_MEMBER_INDEX_NREF)
        set(value) = writeInt(STRUCT_MEMBER_INDEX_NREF, value)

    public var zErrMsg: Long
        get() = readLong(STRUCT_MEMBER_INDEX_ZERRMSG)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_ZERRMSG, value)

    override fun free() {
        vTabDeinit(pointer)
        super.free()
    }

    public companion object Layout {

        internal val layout by lazy { structLayout(StructType.Vtab) }

        public const val STRUCT_MEMBER_INDEX_PMODULE: Int = 0
        public const val STRUCT_MEMBER_INDEX_NREF: Int = 1
        public const val STRUCT_MEMBER_INDEX_ZERRMSG: Int = 2
    }
}