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
package ksqlite.kapi.result

import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.value.Value
import ksqlite.types.SqliteTextEncoding

/**
 * Scope used in places where SQLite expects a result value to be returned. Every method returns
 * [Result] as proof that a result was set.
 */
public interface ResultScope {

    /**
     * Sets the result as `null`.
     */
    public fun resultNull(): Result

    /**
     * Sets the result to be a buffer of [size] bytes, all set to zero.
     */
    public fun resultZeroBlob(size: Int): Result

    /**
     * Overload of [resultZeroBlob] for sizes larger than [Int.MAX_VALUE].
     */
    public fun resultZeroBlob(size: Long): Result

    /**
     * Sets the bytes buffer [value] as the result.
     */
    public fun resultByteArray(
        value: ByteArray,
        size: Int = value.size
    ): Result

    /**
     * Sets the bytes buffer [value] as the result.
     *
     * When SQLite no longer needs [value], it invokes [cleanup].
     */
    public fun resultBuffer(
        value: Buffer,
        size: Long = value.byteSize,
        cleanup: ((Buffer) -> Unit)? = null
    ): Result

    /**
     * Sets the 32-bit signed integer [value] as the result.
     */
    public fun resultInt(value: Int): Result

    /**
     * Sets the 64-bit signed integer [value] as the result.
     */
    public fun resultLong(value: Long): Result

    /**
     * Sets the 64-bit floating point [value] as the result.
     */
    public fun resultDouble(value: Double): Result

    /**
     * Sets the text [value] as the UTF-8 encoded result.
     */
    public fun resultString(value: String): Result

    /**
     * Sets the text buffer [value], interpreted using [encoding], as the result.
     *
     * When SQLite no longer needs [value], it invokes [cleanup].
     */
    public fun resultText(
        value: Buffer,
        encoding: SqliteTextEncoding.ResultText,
        size: Long = value.byteSize,
        cleanup: ((Buffer) -> Unit)? = null
    ): Result

    /**
     * Sets [value] as the result. [value] can be a protected or unprotected SQLite value.
     */
    public fun resultValue(value: Value): Result

    /**
     * Sets [value] as the result.
     *
     * If [value] implements [AutoCloseable] then [AutoCloseable.close] is invoked on it when
     * SQLite finalizes it.
     */
    public fun resultPointer(
        value: Any,
        type: String? = null
    ): Result
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Convenience overload of [resultByteArray] that returns `null` when [value] is `null`.
 */
public fun ResultScope.resultByteArray(value: ByteArray?): Result =
    value?.let(::resultByteArray) ?: resultNull()

/**
 * Convenience overload of [resultBuffer] that returns `null` when [value] is `null`.
 */
public fun ResultScope.resultBuffer(
    value: Buffer?,
    size: Long = value?.byteSize ?: 0L,
    cleanup: ((Buffer) -> Unit)? = null
): Result = value?.let { resultBuffer(it, size, cleanup) } ?: resultNull()

/**
 * Convenience overload of [resultInt] that returns `null` when [value] is `null`.
 */
public fun ResultScope.resultInt(value: Int?): Result =
    value?.let(::resultInt) ?: resultNull()

/**
 * Sets the result to [value], using SQLite's own convention for booleans, `1` for `true` and `0`
 * for `false`, since SQLite has no dedicated boolean storage class.
 */
public fun ResultScope.resultBoolean(value: Boolean): Result =
    resultInt(if (value) 1 else 0)

/**
 * Convenience overload of [resultBoolean] that returns `null` when [value] is `null`.
 */
public fun ResultScope.resultBoolean(value: Boolean?): Result =
    value?.let(::resultBoolean) ?: resultNull()

/**
 * Convenience overload of [resultLong] that returns `null` when [value] is `null`.
 */
public fun ResultScope.resultLong(value: Long?): Result =
    value?.let(::resultLong) ?: resultNull()

/**
 * Convenience overload of [resultDouble] that returns `null` when [value] is `null`.
 */
public fun ResultScope.resultDouble(value: Double?): Result =
    value?.let(::resultDouble) ?: resultNull()

/**
 * Convenience overload of [resultString] that returns `null` when [value] is `null`.
 */
public fun ResultScope.resultString(value: String?): Result =
    value?.let(::resultString) ?: resultNull()

/**
 * Convenience overload of [resultText] that returns `null` when [value] is `null`.
 */
public fun ResultScope.resultText(
    value: Buffer?,
    encoding: SqliteTextEncoding.ResultText,
    size: Long = value?.byteSize ?: 0L,
    cleanup: ((Buffer) -> Unit)? = null
): Result = value?.let { resultText(it, encoding, size, cleanup) } ?: resultNull()

/**
 * Convenience overload of [resultValue] that returns `null` when [value] is `null`.
 */
public fun ResultScope.resultValue(value: Value?): Result =
    value?.let(::resultValue) ?: resultNull()

/**
 * Convenience overload of [resultPointer] that returns `null` when [value] is `null`.
 */
public fun ResultScope.resultPointer(
    value: Any?,
    type: String?
): Result = value?.let { resultPointer(it, type) } ?: resultNull()