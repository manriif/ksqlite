package ksqlite.capi.types

/**
 * Base for output parameter.
 */
public interface OutputParam<Value> {

    /**
     * Value written on native side.
     *
     * ## Pointer to sqlite3* structs.
     *
     * For pointers to sqlite3* structs, [Value] is a wrapper enclosing the pointer to the allocated
     * struct which can be `null` in the following cases:
     *
     * - This [OutputParam] instance was not passed to any SQLite interface intended to allocate
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
public expect class Int32OutputParam(initialValue: Int = 0) : OutputParam<Int> {
    override val value: Int
}

/**
 * Wrapper around [Long] intended to be passed as parameter and written by SQLite.
 *
 * An [initialValue] can optionally be supplied.
 */
public expect class Int64OutputParam(initialValue: Long = 0L) : OutputParam<Long> {
    override val value: Long
}

///////////////////////////////////////////////////////////////////////////
// String
///////////////////////////////////////////////////////////////////////////

/**
 * Wrapper around UTF-8 encoded [String] intended to be passed as parameter and written by SQLite.
 */
public expect class Utf8OutputParam() : OutputParam<String?> {

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
public expect class SqliteOutputParam() : OutputParam<sqlite3?> {
    override val value: sqlite3?
}

/**
 * Wrapper around [sqlite3_blob] intended to be passed as parameter and written by SQLite.
 */
public expect class SqliteBlobOutputParam() : OutputParam<sqlite3_blob?> {
    override val value: sqlite3_blob?
}

/**
 * Wrapper around [sqlite3_snapshot] intended to be passed as parameter and written by SQLite.
 */
public expect class SqliteSnapshotOutputParam() : OutputParam<sqlite3_snapshot?> {
    override val value: sqlite3_snapshot?
}

/**
 * Wrapper around [sqlite3_stmt] intended to be passed as parameter and written by SQLite.
 */
public expect class SqliteStmtOutputParam() : OutputParam<sqlite3_stmt?> {
    override val value: sqlite3_stmt?
}

/**
 * Wrapper around [sqlite3_value] intended to be passed as parameter and written by SQLite.
 */
public expect class SqliteValueOutputParam() : OutputParam<sqlite3_value?> {
    override val value: sqlite3_value?
}

///////////////////////////////////////////////////////////////////////////
// Pointers
///////////////////////////////////////////////////////////////////////////

/**
 * Throws if [value] is not null.
 *
 * Allowing reuse of pointer based [OutputParam] would require to initialize a pointer to the value
 * it is currently holding. There is currently not such use case.
 */
internal fun ensurePointerInitialValueIsNull(value: Any?) {
    check(value == null) {
        "Pointer based OutputParam cannot be reused"
    }
}