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
 * Allocates or reinterprets a `sqlite3_vtab_cursor`.
 */
public abstract class sqlite3_vtab_cursor<Pointer : Any>(
    adapter: Adapter<Pointer>,
    pointer: Pointer?
) : Struct<StructType.Sqlite3VtabCursor, sqlite3_vtab_cursor.Member, Pointer>(
    type = Sqlite3VtabCursor,
    adapter = adapter,
    pointer = pointer
) {

    public var pVtab: Pointer
        get() = readPointer(PVTAB)
        set(value) = writePointer(PVTAB, value)

    /**
     * Members of the `sqlite3_vtab_cursor` struct.
     */
    public enum class Member : StructMember<StructType.Sqlite3VtabCursor> {
        PVTAB,
    }
}