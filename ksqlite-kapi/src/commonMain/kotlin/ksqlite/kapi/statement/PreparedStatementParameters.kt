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
package ksqlite.kapi.statement

import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.value.Value
import ksqlite.types.SqliteTextEncoding

/**
 * Exposes the API to bind values to a [PreparedStatement]'s parameters.
 *
 * [Binding Values To Prepared Statements](https://sqlite.org/c3ref/bind_blob.html)
 */
public interface PreparedStatementParameters {

    /**
     * Number of parameters that can be bound to.
     */
    public val count: Int

    /**
     * Returns the index of the parameter named [name], or `0` if there is no matching
     * parameter.
     */
    public fun getIndex(name: String): Int

    /**
     * Returns the name of the parameter at [index], or `null` if it is unnamed or [index] is
     * out of range.
     */
    public fun getName(index: Int): String?

    /**
     * Sets `null` as the value for the parameter at [index].
     *
     * @throws ksqlite.kapi.SQLiteException if binding the value fails.
     */
    public fun bindNull(index: Int)

    /**
     * Sets the value for the parameter at [index] to be a buffer of [size] bytes, all set to
     * zero.
     *
     * @throws ksqlite.kapi.SQLiteException if binding the value fails.
     */
    public fun bindZeroBlob(
        index: Int,
        size: Int
    )

    /**
     * Overload of [bindZeroBlob] for sizes larger than [Int.MAX_VALUE].
     *
     * @throws ksqlite.kapi.SQLiteException if binding the value fails.
     */
    public fun bindZeroBlob(
        index: Int,
        size: Long
    )

    /**
     * Sets the bytes buffer [value] as the value for the parameter at [index].
     *
     * @throws ksqlite.kapi.SQLiteException if binding the value fails.
     */
    public fun bindByteArray(
        index: Int,
        value: ByteArray,
        size: Int = value.size
    )

    /**
     * Sets the bytes buffer [value] as the value for the parameter at [index].
     *
     * When SQLite no longer needs [value], it invokes [cleanup].
     *
     * @throws ksqlite.kapi.SQLiteException if binding the value fails.
     */
    public fun bindBuffer(
        index: Int,
        value: Buffer,
        size: Long = value.byteSize,
        cleanup: ((Buffer) -> Unit)? = null
    )

    /**
     * Sets the 32-bit signed integer [value] as the value for the parameter at [index].
     *
     * @throws ksqlite.kapi.SQLiteException if binding the value fails.
     */
    public fun bindInt(
        index: Int,
        value: Int
    )

    /**
     * Sets the 64-bit signed integer [value] as the value for the parameter at [index].
     *
     * @throws ksqlite.kapi.SQLiteException if binding the value fails.
     */
    public fun bindLong(
        index: Int,
        value: Long
    )

    /**
     * Sets the 64-bit floating point [value] as the value for the parameter at [index].
     *
     * @throws ksqlite.kapi.SQLiteException if binding the value fails.
     */
    public fun bindDouble(
        index: Int,
        value: Double
    )

    /**
     * Sets the text [value] as the UTF-8 encoded value for the parameter at [index].
     *
     * @throws ksqlite.kapi.SQLiteException if binding the value fails.
     */
    public fun bindString(
        index: Int,
        value: String
    )

    /**
     * Sets the text buffer [value], interpreted using [encoding], as the value for the
     * parameter at [index].
     *
     * When SQLite no longer needs [value], it invokes [cleanup].
     *
     * @throws ksqlite.kapi.SQLiteException if binding the value fails.
     */
    public fun bindText(
        index: Int,
        value: Buffer,
        encoding: SqliteTextEncoding.BindText,
        size: Long = value.byteSize,
        cleanup: ((Buffer) -> Unit)? = null
    )

    /**
     * Sets [value] as the value for the parameter at [index]. [value] can be a protected or
     * unprotected SQLite value.
     *
     * @throws ksqlite.kapi.SQLiteException if binding the value fails.
     */
    public fun bindValue(
        index: Int,
        value: Value
    )

    /**
     * Sets [value] as the value for the parameter at [index].
     *
     * If [value] implements [AutoCloseable] then [AutoCloseable.close] is invoked on it when
     * SQLite finalizes it.
     *
     * @throws ksqlite.kapi.SQLiteException if binding the value fails.
     */
    public fun bindPointer(
        index: Int,
        value: Any,
        type: String? = null
    )

    /**
     * Resets all parameters to `null`.
     *
     * @throws ksqlite.kapi.SQLiteException if the operation fails.
     */
    public fun clear()
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Convenience overload of [bindByteArray] that binds `null` when [value] is `null`.
 *
 * @throws ksqlite.kapi.SQLiteException if binding the value fails.
 */
public fun PreparedStatementParameters.bindByteArray(
    index: Int,
    value: ByteArray?,
    size: Int = value?.size ?: 0
): Unit = value?.let { bindByteArray(index, it, size) } ?: bindNull(index)

/**
 * Convenience overload of [bindBuffer] that binds `null` when [value] is `null`.
 *
 * @throws ksqlite.kapi.SQLiteException if binding the value fails.
 */
public fun PreparedStatementParameters.bindBuffer(
    index: Int,
    value: Buffer?,
    cleanup: ((Buffer) -> Unit)? = null
): Unit = value?.let { bindBuffer(index, it, cleanup) } ?: bindNull(index)

/**
 * Convenience overload of [bindInt] that binds `null` when [value] is `null`.
 *
 * @throws ksqlite.kapi.SQLiteException if binding the value fails.
 */
public fun PreparedStatementParameters.bindInt(
    index: Int,
    value: Int?
): Unit = value?.let { bindInt(index, it) } ?: bindNull(index)

/**
 * Sets [value] as the value for the parameter at [index], using SQLite's own convention for
 * booleans, `1` for `true` and `0` for `false`, since SQLite has no dedicated boolean storage
 * class.
 *
 * @throws ksqlite.kapi.SQLiteException if binding the value fails.
 */
public fun PreparedStatementParameters.bindBoolean(
    index: Int,
    value: Boolean
): Unit = bindInt(index, if (value) 1 else 0)

/**
 * Convenience overload of [bindBoolean] that binds `null` when [value] is `null`.
 *
 * @throws ksqlite.kapi.SQLiteException if binding the value fails.
 */
public fun PreparedStatementParameters.bindBoolean(
    index: Int,
    value: Boolean?
): Unit = value?.let { bindBoolean(index, it) } ?: bindNull(index)

/**
 * Convenience overload of [bindLong] that binds `null` when [value] is `null`.
 *
 * @throws ksqlite.kapi.SQLiteException if binding the value fails.
 */
public fun PreparedStatementParameters.bindLong(
    index: Int,
    value: Long?
): Unit = value?.let { bindLong(index, it) } ?: bindNull(index)

/**
 * Convenience overload of [bindDouble] that binds `null` when [value] is `null`.
 *
 * @throws ksqlite.kapi.SQLiteException if binding the value fails.
 */
public fun PreparedStatementParameters.bindDouble(
    index: Int,
    value: Double?
): Unit = value?.let { bindDouble(index, it) } ?: bindNull(index)

/**
 * Convenience overload of [bindString] that binds `null` when [value] is `null`.
 *
 * @throws ksqlite.kapi.SQLiteException if binding the value fails.
 */
public fun PreparedStatementParameters.bindString(
    index: Int,
    value: String?
): Unit = value?.let { bindString(index, it) } ?: bindNull(index)

/**
 * Convenience overload of [bindText] that binds `null` when [value] is `null`.
 *
 * @throws ksqlite.kapi.SQLiteException if binding the value fails.
 */
public fun PreparedStatementParameters.bindText(
    index: Int,
    value: Buffer?,
    encoding: SqliteTextEncoding.BindText,
    size: Long = value?.byteSize ?: 0L,
    cleanup: ((Buffer) -> Unit)? = null
): Unit = value?.let { bindText(index, it, encoding, size, cleanup) } ?: bindNull(index)

/**
 * Convenience overload of [bindValue] that binds `null` when [value] is `null`.
 *
 * @throws ksqlite.kapi.SQLiteException if binding the value fails.
 */
public fun PreparedStatementParameters.bindValue(
    index: Int,
    value: Value?
): Unit = value?.let { bindValue(index, it) } ?: bindNull(index)

/**
 * Convenience overload of [bindPointer] that binds `null` when [value] is `null`.
 *
 * @throws ksqlite.kapi.SQLiteException if binding the value fails.
 */
public fun PreparedStatementParameters.bindPointer(
    index: Int,
    value: Any?,
    type: String? = null
): Unit = value?.let { bindPointer(index, it, type) } ?: bindNull(index)
