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
@file:Suppress("SpellCheckingInspection")

package ksqlite.types

/**
 * The following are allowed values for the 6th argument (the F argument) to the
 * sqlite3_deserialize(D,S,P,N,M,F) interface.
 *
 * [Flags for sqlite3_deserialize()](https://sqlite.org/c3ref/c_deserialize_freeonclose.html).
 */
public sealed class SqliteDeserializeFlag(public open val value: Int) {

    /**
     * Flag that is a constant.
     */
    public sealed class Constant(value: Int) : SqliteDeserializeFlag(value)

    /**
     * The SQLITE_DESERIALIZE_FREEONCLOSE means that the database serialization in the P argument is
     * held in memory obtained from sqlite3_malloc64() and that SQLite should take ownership of this
     * memory and automatically free it when it has finished using it. Without this flag, the caller
     * is responsible for freeing any dynamically allocated memory.
     */
    public data object FREEONCLOSE : Constant(1)

    /**
     * The SQLITE_DESERIALIZE_RESIZEABLE flag means that SQLite is allowed to grow the size of the
     * database using calls to sqlite3_realloc64(). This flag should only be used if
     * SQLITE_DESERIALIZE_FREEONCLOSE is also used. Without this flag, the deserialized database
     * cannot increase in size beyond the number of bytes specified by the M parameter.
     */
    public data object RESIZEABLE : Constant(2)

    /**
     * The SQLITE_DESERIALIZE_READONLY flag means that the deserialized database should be treated
     * as read-only.
     */
    public data object READONLY : Constant(4)

    ///////////////////////////////////////////////////////////////////////////
    // Masking
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Holder for the flags to be passed to the deserialize API function.
     */
    @ConsistentCopyVisibility
    public data class Mask internal constructor(override val value: Int) :
        SqliteDeserializeFlag(value) {

        override fun contains(flag: SqliteDeserializeFlag): Boolean =
            (value and flag.value) == flag.value
    }

    /**
     * Returns an [SqliteDeserializeFlag] which is ORed with [flag].
     */
    public infix fun or(flag: SqliteDeserializeFlag): SqliteDeserializeFlag =
        Mask(value or flag.value)

    /**
     * Returns an [SqliteDeserializeFlag] which is ANDed with [flag].
     */
    public infix fun and(flag: SqliteDeserializeFlag): SqliteDeserializeFlag =
        Mask(value and flag.value)

    /**
     * Returns an [SqliteDeserializeFlag] which has [flag] removed.
     */
    public infix fun without(flag: SqliteDeserializeFlag): SqliteDeserializeFlag =
        Mask(value and flag.value.inv())

    /**
     * Returns `true` if [flag] is equals to `this`.
     * It this is a mask, returns `true` if it contains [flag].
     */
    public open operator fun contains(flag: SqliteDeserializeFlag): Boolean =
        flag == this || flag.value == value
}