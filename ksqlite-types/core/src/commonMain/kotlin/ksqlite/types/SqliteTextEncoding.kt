@file:Suppress("ClassName")

package ksqlite.types

/**
 * These constants define integer codes that represent the various text encodings supported by
 * SQLite.
 *
 * [Text Encodings](https://sqlite.org/c3ref/c_any.html)
 */
public sealed class SqliteTextEncoding(public open val value: Int) {

    /**
     * Set of [SqliteTextEncoding] including [UTF8], [UFT16LE], [UTF16BE].
     */
    public sealed class Set2(value: Int) : SqliteTextEncoding(value)

    /**
     * Set of [SqliteTextEncoding] including [Set2] and [UTF16].
     */
    public sealed class Set1(value: Int) : Set2(value)

    /**
     * Set of [SqliteTextEncoding] including [Set1] and [UTF16_ALIGNED].
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

    ///////////////////////////////////////////////////////////////////////////
    // Masking
    ///////////////////////////////////////////////////////////////////////////

    /**
     * [SqliteTextEncoding] applying a mask and that can be used with create functions routines.
     */
    @ConsistentCopyVisibility
    public data class Mask internal constructor(override val value: Int) :
        SqliteTextEncoding(value)

    /**
     * Returns an [SqliteTextEncoding] which is ORed with [flag].
     */
    public infix fun or(flag: SqliteFunctionFlag): SqliteTextEncoding {
        return Mask(value or flag.value)
    }
}