package ksqlite.kapi

import ksqlite.capi.memory.ReadableBuffer
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
import ksqlite.capi.sqlite3_value_nochange
import ksqlite.capi.sqlite3_value_numeric_type
import ksqlite.capi.sqlite3_value_pointer
import ksqlite.capi.sqlite3_value_subtype
import ksqlite.capi.sqlite3_value_text
import ksqlite.capi.sqlite3_value_type
import ksqlite.capi.types.Sqlite3DataType
import ksqlite.capi.types.Sqlite3TextEncoding
import ksqlite.capi.types.sqlite3_value
import kotlin.concurrent.Volatile

/**
 * Represents an `sqlite3_value` opaque structure.
 *
 * SQLite may perform an implicit type conversion to match the requested value type. If conversion
 * is not feasible then a [ClassCastException] is thrown.
 *
 * Internal note: [SQLiteValue] is exposed as a class because of [getAs] which is required to be
 * an inline function with a reified type.
 */
public class SQLiteValue private constructor(
    @PublishedApi
    internal val value: sqlite3_value,
    /**
     * Data type of the value.
     */
    public val type: Sqlite3DataType.NotNull,
    private val isDuplicate: Boolean,
) {

    @PublishedApi
    @Volatile
    internal var closed: Boolean = false

    /**
     * Returns the type of the value after a numeric conversion as been done. The conversion is only
     * made if the value can be converted.
     */
    public val numericType: Sqlite3DataType
        get() = notClosed { sqlite3_value_numeric_type(value) }

    /**
     * Returns the subtype of the value.
     */
    public val subtype: UInt
        get() = notClosed { sqlite3_value_subtype(value) }

    /**
     * Whether the value originated from a bound parameter.
     */
    public val isFromBind: Boolean
        get() = notClosed { sqlite3_value_frombind(value) != 0 }

    /**
     * Whether the column is unchanged in an UPDATE against a virtual table.
     */
    public val isNoChange: Boolean
        get() = notClosed { sqlite3_value_nochange(value) != 0 }

    /**
     * Returns the length of the value in bytes.
     *
     * This is only relevant when [type] returns [Sqlite3DataType.BLOB] or
     * [Sqlite3DataType.TEXT].
     */
    public val byteLength: Int
        get() = notClosed { sqlite3_value_bytes(value) }

    /**
     * Returns the current text encoding of the value, assuming that [type] returns
     * [Sqlite3DataType.TEXT].
     */
    public val encoding: Sqlite3TextEncoding.Set2
        get() = notClosed { sqlite3_value_encoding(value) }

    /**
     * Returns the value as [Data] or `null` if no data is associated with [type].
     */
    public inline fun <reified Data : Any> getAs(type: String? = null): Data? =
        notClosed { sqlite3_value_pointer(value, type) }

    /**
     * Returns the value as [Int].
     */
    public fun getAsInt(): Int = notClosed { sqlite3_value_int(value) }

    /**
     * Returns the value as [Long].
     */
    public fun getAsLong(): Long = notClosed { sqlite3_value_int64(value) }

    /**
     * Returns the value as [Double].
     */
    public fun getAsDouble(): Double = notClosed { sqlite3_value_double(value) }

    /**
     * Invokes [convert] and throws [ClassCastException] if it returns `null`.
     */
    private inline fun <reified T> tryCast(convert: (sqlite3_value) -> T?): T = notClosed {
        convert(value) ?: throw ClassCastException(
            "Value of type $type cannot be converted to ${T::class.simpleName}"
        )
    }

    /**
     * Returns the value as a [ksqlite.capi.memory.ReadableBuffer].
     */
    public fun getAsBuffer(): ReadableBuffer = tryCast(::sqlite3_value_buffer)

    /**
     * Returns the value as [ByteArray].
     */
    public fun getAsByteArray(): ByteArray = tryCast(::sqlite3_value_blob)

    /**
     * Returns the value as [String].
     */
    public fun getAsString(): String = tryCast(::sqlite3_value_text)

    /**
     * Duplicates the value.
     */
    public fun duplicate(): SQLiteValue = notClosed {
        val duplicate = checkOutOfMemory(sqlite3_value_dup(value)) {
            "There is not enough memory available to duplicate the value"
        }

        val duplicateType = sqlite3_value_type(duplicate)

        check(duplicateType == type) {
            "Expected duplicated value type to be $type but it was $duplicateType"
        }

        SQLiteValue(duplicate, type, true)
    }

    /**
     * Frees the value previously obtained using [duplicate].
     * This value can no longer be used after that call.
     */
    public fun free() {
        check(isDuplicate) { "free() can only be invoked on a duplicated value" }
        notClosed { sqlite3_value_free(value) }
        close()
    }

    /**
     * Returns [block]'s result or throws [IllegalStateException] if [closed] is `true`.
     */
    @PublishedApi
    internal inline fun <R> notClosed(block: () -> R): R {
        check(!closed) { "Value is closed" }
        return block()
    }

    /**
     * Closes this value making all members inaccessible.
     */
    internal fun close() {
        if (!closed) {
            closed = true
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Companion
    ///////////////////////////////////////////////////////////////////////////

    public companion object {

        /**
         * Creates a new [SQLiteValue] wrapping [value] or returns `null` if the type of [value] is
         * [Sqlite3DataType.NULL].
         */
        internal fun from(
            value: sqlite3_value,
            isDuplicate: Boolean = false
        ): SQLiteValue? = when (val type = sqlite3_value_type(value)) {
            is NotNull -> SQLiteValue(value, type, isDuplicate)
            NULL -> null
        }
    }
}