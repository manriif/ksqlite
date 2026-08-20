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
package ksqlite.kapi.value

import ksqlite.kapi.buffer.Buffer
import ksqlite.types.SqliteTextEncoding

/**
 * Scope used in places where SQLite is expecting a value to be returned using one of
 * `sqlite3_result_xx` APIs.
 */
public interface ValueReturnScope {

    /**
     * Sets the result as `null`.
     * This function maps to `sqlite3_result_null()`.
     */
    public fun setResult(value: Nothing?)

    /**
     * Sets the result to be a buffer of the given [size] with all bytes set to `zero`.
     * This function maps to `sqlite3_result_zeroblob()`.
     */
    public fun setResult(
        value: Nothing?,
        size: Int
    )

    /**
     * Sets the result to be a buffer of the given [size] with all bytes set to `zero`.
     * This function maps to `sqlite3_result_zeroblob64()`.
     */
    public fun setResult(
        value: Nothing?,
        size: Long
    )

    /**
     * Sets the bytes buffer [value] as the result.
     * This function maps to `sqlite3_result_blob()`.
     */
    public fun setResult(
        value: ByteArray,
        size: Int = value.size
    )

    /**
     * Sets the bytes buffer [value] as the result.
     * This function maps to `sqlite3_result_blob64()`.
     *
     * When SQLite no longer needs the [value], it will invoke [cleanup].
     */
    public fun setResult(
        value: Buffer,
        size: Long = value.byteSize,
        cleanup: ((Buffer) -> Unit)? = null
    )

    /**
     * Sets the 32-bit signed integer [value] as the result.
     * This function maps to `sqlite3_result_int()`.
     */
    public fun setResult(value: Int)

    /**
     * Sets the 64-bit signed integer [value] as the result.
     * This function maps to `sqlite3_result_int64()`.
     */
    public fun setResult(value: Long)

    /**
     * Sets the 64-bit floating point [value] as the result.
     * This function maps to `sqlite3_result_double()`.
     */
    public fun setResult(value: Double)

    /**
     * Sets the text [value] as the UTF-8 encoded result.
     * This function maps to `sqlite3_result_text()`.
     */
    public fun setResult(value: String)

    /**
     * Sets the text buffer [value] as the result.
     * This function maps to `sqlite3_result_text64()`.
     *
     * When SQLite no longer needs the [value], it will invoke [cleanup].
     */
    public fun setResult(
        value: Buffer,
        encoding: SqliteTextEncoding.ResultText,
        size: Long = value.byteSize,
        cleanup: ((Buffer) -> Unit)? = null
    )

    /**
     * Sets [value] as the result.
     * This function maps to `sqlite3_result_value()`.
     *
     * The [value] can be a protected or unprotected SQLite value.
     */
    public fun setResult(value: Value)

    /**
     * Sets [value] as the result.
     * This function maps to `sqlite3_result_pointer()`.
     *
     * If [value] implements [AutoCloseable] then [AutoCloseable.close] is invoked on [value] when
     * SQLite finalize it.
     */
    public fun setResult(
        value: Any,
        type: String? = null
    )
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Sets the bytes buffer [value] as the result or sets `null` as the result if [value] is `null`.
 */
public fun ValueReturnScope.setResult(value: ByteArray?): Unit =
    value?.let(::setResult) ?: setResult(null)

/**
 * Sets the 32-bit signed integer [value] as the result or sets `null` as the result if [value] is
 * `null`.
 */
public fun ValueReturnScope.setResult(value: Int?): Unit =
    value?.let(::setResult) ?: setResult(null)

/**
 * Sets the 64-bit signed integer [value] as the result or sets `null` as the result if [value] is
 * `null`.
 */
public fun ValueReturnScope.setResult(value: Long?): Unit =
    value?.let(::setResult) ?: setResult(null)

/**
 * Sets the 64-bit floating point [value] as the result or sets `null` as the result if [value] is
 * `null`.
 */
public fun ValueReturnScope.setResult(value: Double?): Unit =
    value?.let(::setResult) ?: setResult(null)

/**
 * Sets the text [value] as the UTF-8 encoded result or sets `null` as the result if [value] is
 * `null`.
 */
public fun ValueReturnScope.setResult(value: String?): Unit =
    value?.let(::setResult) ?: setResult(null)

/**
 * Sets [value] result or sets `null` as the result if [value] is
 * `null`.
 */
public fun ValueReturnScope.setResult(value: Value?): Unit =
    value?.let(::setResult) ?: setResult(null)

/**
 * Sets [value] result or sets `null` as the result if [value] is
 * `null`.
 */
public fun ValueReturnScope.setResult(value: Any?): Unit =
    value?.let(::setResult) ?: setResult(null)