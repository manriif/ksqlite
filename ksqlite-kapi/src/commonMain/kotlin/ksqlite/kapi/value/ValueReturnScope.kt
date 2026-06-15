package ksqlite.kapi.value

import ksqlite.capi.memory.Buffer
import ksqlite.types.SqliteTextEncoding

/**
 * Scope used in functions where SQLite is expecting a value to be returned using one of
 * `sqlite3_result_xx` APIs.
 */
public interface ValueReturnScope {

    /**
     * Sets the result as `null`.
     * This function maps to `sqlite3_result_null()`.
     */
    public fun setResult(value: Nothing?)

    /**
     * Sets the function result to be a buffer of the given [size] with all bytes set to `zero`.
     * This function maps to `sqlite3_result_zeroblob()`.
     */
    public fun setResult(
        value: Nothing?,
        size: Int
    )

    /**
     * Sets the function result to be a buffer of the given [size] with all bytes set to `zero`.
     * This function maps to `sqlite3_result_zeroblob64()`.
     */
    public fun setResult(
        value: Nothing?,
        size: ULong
    )

    /**
     * Sets the bytes buffer [value] as the function result.
     * This function maps to `sqlite3_result_blob()`.
     */
    public fun setResult(
        value: ByteArray,
        size: Int = value.size
    )

    /**
     * Sets the bytes buffer [value] as the function result.
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
     * Sets the 32-bit signed integer [value] as the function result.
     * This function maps to `sqlite3_result_int()`.
     */
    public fun setResult(value: Int)

    /**
     * Sets the 64-bit signed integer [value] as the function result.
     * This function maps to `sqlite3_result_int64()`.
     */
    public fun setResult(value: Long)

    /**
     * Sets the 64-bit floating point [value] as the function result.
     * This function maps to `sqlite3_result_double()`.
     */
    public fun setResult(value: Double)

    /**
     * Sets the text [value] as the UTF-8 encoded function result.
     * This function maps to `sqlite3_result_text()`.
     */
    public fun setResult(value: String)

    /**
     * Sets the text buffer [value] as the function result.
     * This function maps to `sqlite3_result_text64()`.
     *
     * When SQLite no longer needs the [value], it will invoke [cleanup].
     */
    public fun setResult(
        value: Buffer,
        encoding: SqliteTextEncoding.Set1,
        size: Long = value.byteSize,
        cleanup: ((Buffer) -> Unit)? = null
    )

    /**
     * Sets the [value] as the function result.
     * This function maps to `sqlite3_result_pointer()`.
     *
     * If [value] implements [AutoCloseable] then [AutoCloseable.close] is invoked on [value] when
     * SQLite finalize it.
     */
    public fun setResult(
        value: Any,
        type: String? = null
    )

    /**
     * Sets the [value] as the function result.
     * This function maps to `sqlite3_result_value()`.
     *
     * The [value] can be a protected or unprotected SQLite value.
     */
    public fun setResult(value: Value)
}