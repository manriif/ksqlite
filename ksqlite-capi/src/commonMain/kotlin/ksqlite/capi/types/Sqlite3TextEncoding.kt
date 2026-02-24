@file:Suppress("ClassName")

package ksqlite.capi.types

/**
 * These constants define integer codes that represent the various text encodings supported by
 * SQLite.
 *
 * [Text Encodings](https://sqlite.org/c3ref/c_any.html)
 */
public sealed class Sqlite3TextEncoding(internal open val value: Int) {

    /**
     * Set of [Sqlite3TextEncoding] including [UTF8], [UFT16LE], [UTF16BE].
     */
    public sealed class Set2(value: Int) : Sqlite3TextEncoding(value)

    /**
     * Set of [Sqlite3TextEncoding] including [Set2] and [UTF16].
     */
    public sealed class Set1(value: Int) : Set2(value)

    /**
     * Set of [Sqlite3TextEncoding] including [Set1] and [UTF16_ALIGNED].
     */
    public sealed class Set0(value: Int) : Set1(value)

    /**
     * IMP: R-37514-35566.
     */
    public data object UTF8 : Set2(1)

    /**
     * IMP: R-03371-37637.
     */
    public data object UFT16LE : Set2(2)

    /**
     * IMP: R-51971-34154
     */
    public data object UTF16BE : Set2(3)

    /**
     * Use native byte order.
     */
    public data object UTF16 : Set1(4)

    /**
     * sqlite3_create_collation() only.
     */
    public data object UTF16_ALIGNED : Set0(8)

    /**
     * [Sqlite3TextEncoding] applying a mask and that can be used with create functions routines.
     */
    @ConsistentCopyVisibility
    public data class Masked internal constructor(override val value: Int) :
        Sqlite3TextEncoding(value)

    /**
     * Returns an [Sqlite3TextEncoding] which is ORed with [flag].
     */
    public infix fun or(flag: Sqlite3FunctionFlag): Sqlite3TextEncoding {
        return Masked(value or flag.value)
    }
}