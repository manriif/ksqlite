package ksqlite.types

/**
 * The following are allowed values for the 4th argument (the F argument) to the
 * sqlite3_serialize(D,S,P,F) interface.
 *
 * [Flags for sqlite3_serialize()](https://sqlite.org/c3ref/serialize.html).
 */
public sealed class SqliteSerializeFlag(public open val value: Int) {

    /**
     * Flag that is a constant.
     */
    public sealed class Constant(value: Int) : SqliteSerializeFlag(value)

    /**
     * If the F argument contains the SQLITE_SERIALIZE_NOCOPY bit, then no memory allocations are
     * made, and the sqlite3_serialize() function will return a pointer to the contiguous memory
     * representation of the database that SQLite is currently using for that database, or NULL if
     * no such contiguous memory representation of the database exist.
     */
    public data object NOCOPY : Constant(0x001)

    ///////////////////////////////////////////////////////////////////////////
    // Masking
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Holder for the flags to be passed to the serialize API function.
     */
    @ConsistentCopyVisibility
    public data class Mask internal constructor(override val value: Int) :
        SqliteSerializeFlag(value)

    /**
     * Returns an [SqliteSerializeFlag] which is ORed with [flag].
     */
    public infix fun or(flag: SqliteSerializeFlag): SqliteSerializeFlag {
        return Mask(value or flag.value)
    }
}