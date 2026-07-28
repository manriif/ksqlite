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
 * Allocates or reinterprets a`sqlite3_vtab`.
 */
public abstract class sqlite3_vtab<Pointer : Any>(
    adapter: Adapter<Pointer>,
    pointer: Pointer?
) : Struct<StructType.Sqlite3Vtab, sqlite3_vtab.Member, Pointer>(
    type = Sqlite3Vtab,
    adapter = adapter,
    pointer = pointer
) {

    public var pModule: Pointer
        get() = readPointer(PMODULE)
        set(value) = writePointer(PMODULE, value)

    public var nRef: Int
        get() = readInt(NREF)
        set(value) = writeInt(NREF, value)

    public var zErrMsg: Pointer
        get() = readPointer(ZERRMSG)
        set(value) = writePointer(ZERRMSG, value)

    /**
     * Members of the `sqlite3_vtab` struct.
     */
    public enum class Member : StructMember<StructType.Sqlite3Vtab> {
        PMODULE,
        NREF,
        ZERRMSG,
    }
}