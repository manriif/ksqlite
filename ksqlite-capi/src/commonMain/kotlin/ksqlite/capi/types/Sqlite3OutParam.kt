package ksqlite.capi.types

///////////////////////////////////////////////////////////////////////////
// Param
///////////////////////////////////////////////////////////////////////////

/**
 * Base for output parameter.
 */
public interface Sqlite3OutParam<Value> {

    /**
     * Value written on native side.
     *
     * ## Pointer to sqlite3* structs.
     *
     * For pointers to sqlite3* structs, [Value] is a wrapper enclosing the pointer to the allocated
     * struct which can be `null` in the following cases:
     *
     * - This [Sqlite3OutParam] instance was not passed to any SQLite interface intended to allocate
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
public expect class Sqlite3IntOutParam(initialValue: Int) : Sqlite3OutParam<Int> {
    override val value: Int
}

/**
 * Wrapper around [Long] intended to be passed as parameter and written by SQLite.
 *
 * An [initialValue] can optionally be supplied.
 */
public expect class Sqlite3LongOutParam(initialValue: Long) : Sqlite3OutParam<Long> {
    override val value: Long
}

///////////////////////////////////////////////////////////////////////////
// String
///////////////////////////////////////////////////////////////////////////

/**
 * Wrapper around UTF-8 encoded [String] intended to be passed as parameter and written by SQLite.
 */
public expect class Sqlite3Utf8OutParam() : Sqlite3OutParam<String?> {

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
public expect class Sqlite3DatabaseConnectionOutParam() : Sqlite3OutParam<sqlite3?> {
    override val value: sqlite3?
}

/**
 * Wrapper around [sqlite3_context] intended to be passed as parameter and allocated by SQLite.
 */
public expect class Sqlite3ContextOutParam() : Sqlite3OutParam<sqlite3_context?> {
    override val value: sqlite3_context?
}

/**
 * Wrapper around [sqlite3_stmt] intended to be passed as parameter and allocated by SQLite.
 */
public expect class Sqlite3StatementOutParam() : Sqlite3OutParam<sqlite3_stmt?> {
    override val value: sqlite3_stmt?
}

/**
 * Wrapper around [sqlite3_value] intended to be passed as parameter and allocated by SQLite.
 */
public expect class Sqlite3ValueOutParam() : Sqlite3OutParam<sqlite3_value?> {
    override val value: sqlite3_value?
}