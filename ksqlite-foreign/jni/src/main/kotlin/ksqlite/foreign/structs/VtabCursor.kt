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

/**
 * Allocates an instance of `sqlite3_vtab_cursor` and supplies getters and setters for reading and
 * writing the struct.
 */
public class sqlite3_vtab_cursor : JniStruct(layout) {

    public var pVtab: Int
        get() = readInt(STRUCT_MEMBER_INDEX_PVTAB)
        set(value) = writeInt(STRUCT_MEMBER_INDEX_PVTAB, value)

    public companion object Layout {

        internal val layout by lazy { structLayout(StructType.VtabCursor) }

        public const val STRUCT_MEMBER_INDEX_PVTAB: Int = 0
    }
}