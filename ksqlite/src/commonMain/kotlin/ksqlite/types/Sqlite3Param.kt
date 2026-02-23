package ksqlite.types

///////////////////////////////////////////////////////////////////////////
// Param
///////////////////////////////////////////////////////////////////////////

/**
 * Base for output parameter.
 */
public interface Sqlite3Param<Value> {

    /**
     * Value written on native side.
     *
     * ## Pointer
     *
     * For pointers, [Value] is a wrapper enclosing the pointer to the allocated struct.
     * The wrapper can be `null` in the following cases:
     *
     * - This instance was not passed to any SQLite interface intended to allocate the struct
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
public expect class Sqlite3IntParam(initialValue: Int) : Sqlite3Param<Int> {
    override val value: Int
}

/**
 * Wrapper around [Long] intended to be passed as parameter and written by SQLite.
 *
 * An [initialValue] can optionally be supplied.
 */
public expect class Sqlite3LongParam(initialValue: Long) : Sqlite3Param<Long> {
    override val value: Long
}

///////////////////////////////////////////////////////////////////////////
// String
///////////////////////////////////////////////////////////////////////////

/**
 * Wrapper around UTF-8 encoded [String] intended to be passed as parameter and written by SQLite.
 */
public expect class Sqlite3StringUtf8Param() : Sqlite3Param<String?> {

    /**
     * UTF-8 encoded [String] or `null` if no string has been allocated or allocation failed.
     */
    override val value: String?
}

///////////////////////////////////////////////////////////////////////////
// Structs
///////////////////////////////////////////////////////////////////////////

/**
 * Wrapper around [sqlite3] intended to be passed as parameter and allocated by SQLite.
 */
public expect class Sqlite3DatabaseConnectionParam() : Sqlite3Param<sqlite3?> {
    override val value: sqlite3?
}

/**
 * Wrapper around [sqlite3_context] intended to be passed as parameter and allocated by SQLite.
 */
public expect class Sqlite3ContextParam() : Sqlite3Param<sqlite3_context?> {
    override val value: sqlite3_context?
}

/**
 * Wrapper around [sqlite3_stmt] intended to be passed as parameter and allocated by SQLite.
 */
public expect class Sqlite3StatementParam() : Sqlite3Param<sqlite3_stmt?> {
    override val value: sqlite3_stmt?
}

/**
 * Wrapper around [sqlite3_value] intended to be passed as parameter and allocated by SQLite.
 */
public expect class Sqlite3ValueParam() : Sqlite3Param<sqlite3_value?> {
    override val value: sqlite3_value?
}