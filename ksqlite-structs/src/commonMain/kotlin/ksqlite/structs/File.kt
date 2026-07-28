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
 * Allocates or reinterprets a `sqlite3_file` with given [size].
 */
public abstract class sqlite3_file<Pointer : Any>(
    adapter: Adapter<Pointer>,
    pointer: Pointer?,
    size: Int
) : Struct<StructType.Sqlite3File, sqlite3_file.Member, Pointer>(
    type = Sqlite3File,
    adapter = adapter,
    pointer = pointer,
    size = size
) {

    public var pMethods: Pointer
        get() = readPointer(PMETHODS)
        set(value) = writePointer(PMETHODS, value)

    /**
     * Members of the `sqlite3_file` struct.
     */
    public enum class Member : StructMember<StructType.Sqlite3File> {
        PMETHODS,
    }
}