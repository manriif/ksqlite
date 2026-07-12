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
 * Exposes the API to deal with [PreparedStatement]'s parameters.
 *
 * [Binding Values To Prepared Statements](https://sqlite.org/c3ref/bind_blob.html).
 * [Binding Parameters and Reusing Prepared Statements](https://sqlite.org/cintro.html#binding_parameters_and_reusing_prepared_statements)
 */
public interface PreparedStatementParameters {

    /**
     * Number of parameter that can be potentially bound to.
     */
    public val count: Int

    /**
     * Returns the index of an SQL parameter given its name.
     */
    public fun getIndex(name: String): Int

    /**
     * Returns the name of the SQL parameter at [index].
     */
    public fun getName(index: Int): String?

    /**
     * Sets `null` as the value for the parameter at [index].
     * This function maps to `sqlite3_bind_null()`.
     *
     * @throws ksqlite.kapi.SQLiteException if the bind operation fails.
     */
    public fun bind(
        index: Int,
        value: Nothing?
    )

    /**
     * Sets the value for the parameter at [index] to be a buffer of the given [size] with all bytes
     * set to `zero`.
     * This function maps to `sqlite3_bind_zeroblob()`.
     *
     * @throws ksqlite.kapi.SQLiteException if the bind operation fails.
     */
    public fun bind(
        index: Int,
        value: Nothing?,
        size: Int
    )

    /**
     * Sets the value for the parameter at [index] to be a buffer of the given [size] with all bytes
     * set to `zero`.
     * This function maps to `sqlite3_bind_zeroblob64()`.
     *
     * @throws ksqlite.kapi.SQLiteException if the bind operation fails.
     */
    public fun bind(
        index: Int,
        value: Nothing?,
        size: ULong
    )

    /**
     * Sets the bytes buffer [value] as the value for the parameter at [index].
     * This function maps to `sqlite3_bind_blob()`.
     *
     * @throws ksqlite.kapi.SQLiteException if the bind operation fails.
     */
    public fun bind(
        index: Int,
        value: ByteArray,
        size: Int = value.size
    )

    /**
     * Sets the bytes buffer [value] as the value for the parameter at [index].
     * This function maps to `sqlite3_bind_blob64()`.
     *
     * When SQLite no longer needs the [value], it will invoke [cleanup].
     *
     * @throws ksqlite.kapi.SQLiteException if the bind operation fails.
     */
    public fun bind(
        index: Int,
        value: Buffer,
        size: Long = value.byteSize,
        cleanup: ((Buffer) -> Unit)? = null
    )

    /**
     * Sets the 32-bit signed integer [value] as the value for the parameter at [index].
     * This function maps to `sqlite3_bind_int()`.
     *
     * @throws ksqlite.kapi.SQLiteException if the bind operation fails.
     */
    public fun bind(
        index: Int,
        value: Int
    )

    /**
     * Sets the 64-bit signed integer [value] as the value for the parameter at [index].
     * This function maps to `sqlite3_bind_int64()`.
     *
     * @throws ksqlite.kapi.SQLiteException if the bind operation fails.
     */
    public fun bind(
        index: Int,
        value: Long
    )

    /**
     * Sets the 64-bit floating point [value] as the value for the parameter at [index].
     * This function maps to `sqlite3_bind_double()`.
     *
     * @throws ksqlite.kapi.SQLiteException if the bind operation fails.
     */
    public fun bind(
        index: Int,
        value: Double
    )

    /**
     * Sets the text [value] as the UTF-8 encoded value for the parameter at [index].
     * This function maps to `sqlite3_bind_text()`.
     *
     * @throws ksqlite.kapi.SQLiteException if the bind operation fails.
     */
    public fun bind(
        index: Int,
        value: String
    )

    /**
     * Sets the text buffer [value] as the value for the parameter at [index].
     * This function maps to `sqlite3_bind_text64()`.
     *
     * When SQLite no longer needs the [value], it will invoke [cleanup].
     *
     * @throws ksqlite.kapi.SQLiteException if the bind operation fails.
     */
    public fun bind(
        index: Int,
        value: Buffer,
        encoding: SqliteTextEncoding.BindText,
        size: Long = value.byteSize,
        cleanup: ((Buffer) -> Unit)? = null
    )

    /**
     * Sets [value] as the value for the parameter at [index].
     * This function maps to `sqlite3_bind_value()`.
     *
     * The [value] can be a protected or unprotected SQLite value.
     *
     * @throws ksqlite.kapi.SQLiteException if the bind operation fails.
     */
    public fun bind(
        index: Int,
        value: Value
    )

    /**
     * Sets [value] as the value for the parameter at [index].
     * This function maps to `sqlite3_bind_pointer()`.
     *
     * If [value] implements [AutoCloseable] then [AutoCloseable.close] is invoked on [value] when
     * SQLite finalize it.
     *
     * @throws ksqlite.kapi.SQLiteException if the bind operation fails.
     */
    public fun bind(
        index: Int,
        value: Any,
        type: String? = null
    )

    /**
     * Resets all host parameters to `null`.
     *
     * @throws ksqlite.kapi.SQLiteException if the operation fails.
     */
    public fun clear()
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Sets the bytes buffer [value] as the value for the parameter at [index] or sets `null` as the
 * value for the parameter at [index] if [value] is `null`.
 *
 * @throws ksqlite.kapi.SQLiteException if the bind operation fails.
 */
public fun PreparedStatementParameters.bind(
    index: Int,
    value: ByteArray?
): Unit = value?.let { bind(index, it) } ?: bind(index, null)

/**
 * Sets the 32-bit signed integer [value] as the value for the parameter at [index] or sets `null`
 * as the value for the parameter at [index] if [value] is `null`.
 *
 * @throws ksqlite.kapi.SQLiteException if the bind operation fails.
 */
public fun PreparedStatementParameters.bind(
    index: Int,
    value: Int?
): Unit = value?.let { bind(index, it) } ?: bind(index, null)

/**
 * Sets the 64-bit signed integer [value] as the value for the parameter at [index] or sets `null`
 * as the value for the parameter at [index] if [value] is `null`.
 *
 * @throws ksqlite.kapi.SQLiteException if the bind operation fails.
 */
public fun PreparedStatementParameters.bind(
    index: Int,
    value: Long?
): Unit = value?.let { bind(index, it) } ?: bind(index, null)

/**
 * Sets the 64-bit floating point [value] as the value for the parameter at [index] or sets `null`
 * as the value for the parameter at [index] if [value] is `null`.
 *
 * @throws ksqlite.kapi.SQLiteException if the bind operation fails.
 */
public fun PreparedStatementParameters.bind(
    index: Int,
    value: Double?
): Unit = value?.let { bind(index, it) } ?: bind(index, null)

/**
 * Sets the text [value] as the UTF-8 encoded value for the parameter at [index] or sets `null` as
 * the value for the parameter at [index] if [value] is `null`.
 *
 * @throws ksqlite.kapi.SQLiteException if the bind operation fails.
 */
public fun PreparedStatementParameters.bind(
    index: Int,
    value: String?
): Unit = value?.let { bind(index, it) } ?: bind(index, null)

/**
 * Sets [value] as the value for the parameter at [index] or sets `null`
 * as the value for the parameter at [index] if [value] is `null`.
 *
 * @throws ksqlite.kapi.SQLiteException if the bind operation fails.
 */
public fun PreparedStatementParameters.bind(
    index: Int,
    value: Any?,
    type: String? = null
): Unit = value?.let { bind(index, it, type) } ?: bind(index, null)

/**
 * Sets [value] as the value for the parameter at [index] or sets `null`
 * as the value for the parameter at [index] if [value] is `null`.
 *
 * @throws ksqlite.kapi.SQLiteException if the bind operation fails.
 */
public fun PreparedStatementParameters.bind(
    index: Int,
    value: Value?
): Unit = value?.let { bind(index, it) } ?: bind(index, null)