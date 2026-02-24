@file:Suppress("SpellCheckingInspection")

package ksqlite.types

/**
 * The following are allowed values for the 6th argument (the F argument) to the
 * sqlite3_deserialize(D,S,P,N,M,F) interface.
 *
 * [Flags for sqlite3_deserialize()](https://sqlite.org/c3ref/c_deserialize_freeonclose.html).
 */
public sealed class Sqlite3DeserializeFlag(internal open val value: Int) {

    /**
     * Flag that is a constant.
     */
    public sealed class Constant(value: Int) : Sqlite3DeserializeFlag(value)

    /**
     * The SQLITE_DESERIALIZE_FREEONCLOSE means that the database serialization in the P argument is
     * held in memory obtained from sqlite3_malloc64() and that SQLite should take ownership of this
     * memory and automatically free it when it has finished using it. Without this flag, the caller
     * is responsible for freeing any dynamically allocated memory.
     */
    public data object FREEONCLOSE : Constant(1)

    /**
     * The SQLITE_DESERIALIZE_RESIZEABLE flag means that SQLite is allowed to grow the size of the
     * database using calls to sqlite3_realloc64(). This flag should only be used if
     * SQLITE_DESERIALIZE_FREEONCLOSE is also used. Without this flag, the deserialized database
     * cannot increase in size beyond the number of bytes specified by the M parameter.
     */
    public data object RESIZEABLE : Constant(2)

    /**
     * The SQLITE_DESERIALIZE_READONLY flag means that the deserialized database should be treated
     * as read-only.
     */
    public data object READONLY : Constant(4)

    /**
     * Holder for the flags to be passed to the deserialize API function.
     */
    @ConsistentCopyVisibility
    public data class Masked internal constructor(override val value: Int) :
        Sqlite3DeserializeFlag(value)

    /**
     * Returns an [Sqlite3DeserializeFlag] which is ORed with [flag].
     */
    public infix fun or(flag: Sqlite3DeserializeFlag): Sqlite3DeserializeFlag {
        return Masked(value or flag.value)
    }
}