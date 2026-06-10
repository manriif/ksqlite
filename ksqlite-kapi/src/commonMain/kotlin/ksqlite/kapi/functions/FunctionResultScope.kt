package ksqlite.kapi.functions

import ksqlite.capi.memory.Buffer
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.Sqlite3TextEncoding
import ksqlite.kapi.SQLiteValue

/**
 * Scope for use with [ScalarFunction.func], [AggregateFunction.final] and [WindowFunction.value].
 */
public interface FunctionResultScope : FunctionScope {

    /**
     * Sets the result as `null`.
     * The underlying SQLite function used is `sqlite3_result_null()`.
     */
    public fun setResult(value: Nothing?)

    /**
     * Sets the function result to be a buffer of the given [size] with all bytes set to `zero`.
     * The underlying SQLite function used is `sqlite3_result_zeroblob()`.
     */
    public fun setResult(
        value: Nothing?,
        size: Int
    )

    /**
     * Sets the function result to be a buffer of the given [size] with all bytes set to `zero`.
     * The underlying SQLite function used is `sqlite3_result_zeroblob64()`.
     */
    public fun setResult(
        value: Nothing?,
        size: ULong
    )

    /**
     * Sets the buffer [value] as the function result.
     * The underlying SQLite function used is `sqlite3_result_blob()`.
     */
    public fun setResult(
        value: ByteArray,
        size: Int = value.size
    )

    /**
     * Sets the 32-bit signed integer [value] as the function result.
     * The underlying SQLite function used is `sqlite3_result_int()`.
     */
    public fun setResult(value: Int)

    /**
     * Sets the 64-bit signed integer [value] as the function result.
     * The underlying SQLite function used is `sqlite3_result_int64()`.
     */
    public fun setResult(value: Long)

    /**
     * Sets the 64-bit floating point [value] as the function result.
     * The underlying SQLite function used is `sqlite3_result_double()`.
     */
    public fun setResult(value: Double)

    /**
     * Sets the text [value] as the UTF-8 encoded function result.
     * The underlying SQLite function used is `sqlite3_result_text()`.
     */
    public fun setResult(value: String)

    /**
     * Sets the buffer [value] as the function result.
     *
     * The underlying SQLite function used depends on the [encoding] parameter. If [encoding] is
     * supplied then `sqlite3_result_text64()` is used, `sqlite3_result_blob64()` is used otherwise.
     *
     * When SQLite no longer needs the [value], it will invoke [cleanup].
     */
    public fun setResult(
        value: Buffer,
        encoding: Sqlite3TextEncoding.Set1? = null,
        size: Long = value.byteSize,
        cleanup: ((Buffer) -> Unit)? = null
    )

    /**
     * Sets the [value] as the function result.
     * The [value] can be a protected or unprotected SQLite value.
     * The underlying SQLite function used is `sqlite3_result_value()`.
     */
    public fun setResult(value: SQLiteValue)

    /**
     * Sets the [value] as the function result.
     * The underlying SQLite function used is `sqlite3_result_pointer()`.
     *
     * If [value] implements [AutoCloseable] then [AutoCloseable.close] is invoked on [value] when
     * SQLite finalize it.
     */
    public fun setResult(
        value: Any,
        type: String? = null
    )

    /**
     * Causes SQLite to throw an exception with [message].
     *
     * By default, SQLite sets the error code to [Sqlite3Result.ERROR] but it can be overridden by
     * supplying an appropriate error [result].
     */
    public fun setResultError(
        message: String,
        result: Sqlite3Result.Failure? = null
    )

    /**
     * Causes SQLite to throw an error indicating that a memory allocation failed.
     */
    public fun setResultErrorNoMem()

    /**
     * Causes SQLite to throw an error indicating that a string or BLOB is too long to represent.
     */
    public fun setResultErrorTooBig()
}