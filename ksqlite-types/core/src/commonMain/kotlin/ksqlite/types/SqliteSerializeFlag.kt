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
package ksqlite.types

/**
 * The following are allowed values for the 4th argument (the F argument) to the
 * serializeInternal(D,S,P,F) interface.
 *
 * [Flags for serializeInternal()](https://sqlite.org/c3ref/serialize.html).
 */
public sealed class SqliteSerializeFlag(public open val value: Int) {

    /**
     * Flag that is a constant.
     */
    public sealed class Constant(value: Int) : SqliteSerializeFlag(value)

    /**
     * If the F argument contains the SQLITE_SERIALIZE_NOCOPY bit, then no memory allocations are
     * made, and the serializeInternal() function will return a pointer to the contiguous memory
     * representation of the database that SQLite is currently using for that database, or NULL if
     * no such contiguous memory representation of the database exist.
     */
    public data object NOCOPY : Constant(0x001)

    ///////////////////////////////////////////////////////////////////////////
    // Masking
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Holder for the flags to be passed to the serialize API function.
     */
    @ConsistentCopyVisibility
    public data class Mask internal constructor(override val value: Int) :
        SqliteSerializeFlag(value) {

        override fun contains(flag: SqliteSerializeFlag): Boolean =
            (value and flag.value) == flag.value
    }

    /**
     * Returns an [SqliteSerializeFlag] which is ORed with [flag].
     */
    public infix fun or(flag: SqliteSerializeFlag): SqliteSerializeFlag =
        Mask(value or flag.value)

    /**
     * Returns an [SqliteSerializeFlag] which is ANDed with [flag].
     */
    public infix fun and(flag: SqliteSerializeFlag): SqliteSerializeFlag =
        Mask(value and flag.value)

    /**
     * Returns an [SqliteSerializeFlag] which has [flag] removed.
     */
    public infix fun without(flag: SqliteSerializeFlag): SqliteSerializeFlag =
        Mask(value and flag.value.inv())

    /**
     * Returns `true` if [flag] is equals to `this`.
     * It this is a mask, returns `true` if it contains [flag].
     */
    public open operator fun contains(flag: SqliteSerializeFlag): Boolean =
        flag == this || flag.value == value
}