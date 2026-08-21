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

import ksqlite.capi.sqlite3_value
import ksqlite.capi.sqlite3_value_blob
import ksqlite.capi.sqlite3_value_buffer
import ksqlite.capi.sqlite3_value_bytes
import ksqlite.capi.sqlite3_value_double
import ksqlite.capi.sqlite3_value_dup
import ksqlite.capi.sqlite3_value_encoding
import ksqlite.capi.sqlite3_value_free
import ksqlite.capi.sqlite3_value_frombind
import ksqlite.capi.sqlite3_value_int
import ksqlite.capi.sqlite3_value_int64
import ksqlite.capi.sqlite3_value_numeric_type
import ksqlite.capi.sqlite3_value_pointer
import ksqlite.capi.sqlite3_value_subtype
import ksqlite.capi.sqlite3_value_text
import ksqlite.capi.sqlite3_value_type
import ksqlite.kapi.buffer.ReadableBuffer
import ksqlite.internal.runtime.closeable.CloseableScope
import ksqlite.internal.runtime.closeable.DelegatingCloseableScope
import ksqlite.kapi.helpers.sqliteOutOfMemoryCheck
import ksqlite.kapi.throwSQLiteException
import ksqlite.types.SqliteDataType
import ksqlite.types.SqliteResultCode
import ksqlite.types.SqliteTextEncoding

/**
 * Base type for a value obtained from SQLite, either a [ProtectedValue] or an [UnprotectedValue].
 */
public sealed class Value(
    @PublishedApi
    internal val value: sqlite3_value,

    /**
     * Scope the value is associated with, determining its lifecycle.
     */
    @PublishedApi
    internal val scope: CloseableScope,
) {

    /**
     * Duplicates the value, returning a [DuplicatedValue].
     */
    public fun duplicate(): DuplicatedValue {
        return DuplicatedValue(sqliteOutOfMemoryCheck(sqlite3_value_dup(value)) {
            "There is not enough memory available to duplicate the value"
        })
    }
}

/**
 * Represents a protected [Value].
 *
 * SQLite may perform an implicit type conversion to match the requested value type. If conversion
 * is not feasible then `null` is returned.
 */
public open class ProtectedValue internal constructor(
    value: sqlite3_value,
    scope: CloseableScope,
) : Value(value, scope) {

    /**
     * Data type of the value.
     */
    public val type: SqliteDataType
        get() = scope.notClosed { sqlite3_value_type(value) }

    /**
     * Returns the type of the value after a numeric conversion has been done. The conversion is
     * only made if the value can be converted.
     */
    public val numericType: SqliteDataType
        get() = scope.notClosed { sqlite3_value_numeric_type(value) }

    /**
     * Returns the subtype of the value.
     */
    public val subtype: UInt
        get() = scope.notClosed { sqlite3_value_subtype(value) }

    /**
     * Whether the value originated from a bound parameter.
     */
    public val isFromBind: Boolean
        get() = scope.notClosed { sqlite3_value_frombind(value) != 0 }

    /**
     * Returns the length of the value in bytes.
     *
     * This is only relevant when [type] returns [ksqlite.types.SqliteDataType.BLOB] or
     * [ksqlite.types.SqliteDataType.TEXT].
     */
    public val bytes: Int
        get() = scope.notClosed { sqlite3_value_bytes(value) }

    /**
     * Returns the current text encoding of the value, assuming that [type] returns
     * [ksqlite.types.SqliteDataType.TEXT].
     */
    public val encoding: SqliteTextEncoding.ValueEncoding
        get() = scope.notClosed { sqlite3_value_encoding(value) }

    /**
     * Returns the value as a [ByteArray].
     */
    public fun getAsByteArray(): ByteArray? =
        scope.notClosed { sqlite3_value_blob(value) }

    /**
     * Returns the value as a [ReadableBuffer].
     */
    public fun getAsBuffer(): ReadableBuffer? = scope.notClosed {
        sqlite3_value_buffer(value)
            ?.let { ReadableBuffer(it, scope) }
    }

    /**
     * Returns the value as an [Int].
     */
    public fun getAsInt(): Int =
        scope.notClosed { sqlite3_value_int(value) }

    /**
     * Returns the value as a [Long].
     */
    public fun getAsLong(): Long =
        scope.notClosed { sqlite3_value_int64(value) }

    /**
     * Returns the value as a [Double].
     */
    public fun getAsDouble(): Double =
        scope.notClosed { sqlite3_value_double(value) }

    /**
     * Returns the value as a [String].
     */
    public fun getAsString(): String? =
        scope.notClosed { sqlite3_value_text(value) }

    /**
     * Returns the value as [Data] or `null` if no data is associated with [type].
     */
    public inline fun <reified Data : Any> getAs(type: String? = null): Data? =
        scope.notClosed { sqlite3_value_pointer(value, type) }
}

/**
 * An unprotected [Value], obtained from [ksqlite.kapi.statement.Row.getValue]. Unlike
 * [ProtectedValue], it does not expose type-converting accessors and remains valid only for as
 * long as the [ksqlite.kapi.statement.Row] it came from does.
 */
public class UnprotectedValue internal constructor(
    value: sqlite3_value,
    scope: CloseableScope
) : Value(value, scope)

/**
 * [Value] obtained from [Value.duplicate] and for which the caller take ownership.
 */
public class DuplicatedValue internal constructor(value: sqlite3_value) : ProtectedValue(
    value = value,
    scope = DelegatingCloseableScope { sqlite3_value_free(value) }
), AutoCloseable {

    /**
     * Frees the value previously obtained using [Value.duplicate].
     * This value can no longer be used after that call.
     */
    override fun close() {
        scope.close()
    }
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the value as a [Boolean], using SQLite's own convention for booleans, `1` for `true`
 * and `0` for `false`, since SQLite has no dedicated boolean storage class.
 *
 * @throws ksqlite.kapi.SQLiteException if the value is neither `0` nor `1`.
 */
public fun ProtectedValue.getAsBoolean(): Boolean = when (val value = getAsInt()) {
    0 -> false
    1 -> true
    else -> throwSQLiteException(
        "Expected 0 or 1 for a boolean value, got $value",
        SqliteResultCode.MISMATCH
    )
}

///////////////////////////////////////////////////////////////////////////
// Factories
///////////////////////////////////////////////////////////////////////////

/**
 * Creates a new [UnprotectedValue] wrapping `this` [sqlite3_value] or returns `null` if the type
 * of `this` value is [ksqlite.types.SqliteDataType.NULL].
 */
internal fun sqlite3_value.toUnprotectedValue(scope: CloseableScope): UnprotectedValue =
    UnprotectedValue(this, scope)

/**
 * Creates a new [ProtectedValue] wrapping `this` [sqlite3_value].
 */
internal fun sqlite3_value.toProtectedValue(scope: CloseableScope): ProtectedValue =
    ProtectedValue(this, scope)

/**
 * Map `this` array of [sqlite3_value] to an array of [ProtectedValue].
 */
internal fun Array<sqlite3_value>.toProtectedValues(scope: CloseableScope): Array<ProtectedValue> {
    return if (isEmpty()) {
        emptyArray()
    } else {
        Array(size) { index ->
            this[index].toProtectedValue(scope)
        }
    }
}