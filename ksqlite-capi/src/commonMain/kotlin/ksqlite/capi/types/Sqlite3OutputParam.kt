package ksqlite.capi.types

///////////////////////////////////////////////////////////////////////////
// Param
///////////////////////////////////////////////////////////////////////////

/**
 * Base for output parameter.
 */
public interface OutputParameter<Value> {

    /**
     * Value written on native side.
     *
     * ## Pointer to sqlite3* structs.
     *
     * For pointers to sqlite3* structs, [Value] is a wrapper enclosing the pointer to the allocated
     * struct which can be `null` in the following cases:
     *
     * - This [OutputParameter] instance was not passed to any SQLite interface intended to allocate
     * the struct
     * - The SQLite interface failed to make the allocation
     *
     * If the returned wrapper was passed to an SQLite interface intended to deallocate the
     * struct, then it should no longer be used.
     */
    public val value: Value
}

///////////////////////////////////////////////////////////////////////////
// Primitives
///////////////////////////////////////////////////////////////////////////

/**
 * Wrapper around [Int] intended to be passed as parameter and written by SQLite.
 *
 * An [initialValue] can optionally be supplied.
 */
public expect class IntOutputParam(initialValue: Int) : OutputParameter<Int> {
    override val value: Int
}

/**
 * Wrapper around [Long] intended to be passed as parameter and written by SQLite.
 *
 * An [initialValue] can optionally be supplied.
 */
public expect class LongOutputParam(initialValue: Long) : OutputParameter<Long> {
    override val value: Long
}

///////////////////////////////////////////////////////////////////////////
// String
///////////////////////////////////////////////////////////////////////////

/**
 * Wrapper around UTF-8 encoded [String] intended to be passed as parameter and written by SQLite.
 */
public expect class Utf8OutputParam() : OutputParameter<String?> {

    /**
     * UTF-8 encoded [String] or `null` if no string has been allocated or allocation failed.
     */
    override val value: String?
}

///////////////////////////////////////////////////////////////////////////
// Structs
///////////////////////////////////////////////////////////////////////////

/**
 * Wrapper around [sqlite3] intended to be passed as parameter and written by SQLite.
 */
public expect class Sqlite3OutputParam() : OutputParameter<sqlite3?> {
    override val value: sqlite3?
}

/**
 * Wrapper around [sqlite3_blob] intended to be passed as parameter and written by SQLite.
 */
public expect class Sqlite3BlobOutputParam() : OutputParameter<sqlite3_blob?> {
    override val value: sqlite3_blob?
}

/**
 * Wrapper around [sqlite3_stmt] intended to be passed as parameter and written by SQLite.
 */
public expect class Sqlite3StmtOutputParam() : OutputParameter<sqlite3_stmt?> {
    override val value: sqlite3_stmt?
}

/**
 * Wrapper around [sqlite3_value] intended to be passed as parameter and written by SQLite.
 */
public expect class Sqlite3ValueOutputParam() : OutputParameter<sqlite3_value?> {
    override val value: sqlite3_value?
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the value as [Boolean] such as:
 *
 * - `false` if value == `0`
 * - `true` if value is positive
 * - throws ISE if value is negative
 */
public val IntOutputParam.booleanValue: Boolean
    get() {
        val intValue = value

        return when {
            intValue == 0 -> false
            intValue >= 1 -> true
            else -> error("Value $intValue cannot be converted to boolean")
        }
    }

/**
 * Returns the value as [Boolean] such as:
 *
 * - `false` if value == `0`
 * - `true` if value == `1`
 * - throws ISE otherwise
 */
public val IntOutputParam.booleanValueStrict: Boolean
    get() = when (val intValue = value) {
        0 -> false
        1 -> true
        else -> error("Value $intValue cannot be converted to boolean")
    }