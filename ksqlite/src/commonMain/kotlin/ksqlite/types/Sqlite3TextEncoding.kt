@file:Suppress("ClassName")

package ksqlite.types

/**
 * These constants define integer codes that represent the various text encodings supported by
 * SQLite.
 *
 * [Text Encodings](https://sqlite.org/c3ref/c_any.html)
 */
public sealed interface Sqlite3TextEncoding {

    /**
     * Set of [Sqlite3TextEncoding] including [UTF8], [UFT16LE], [UTF16BE] and [UTF16].
     */
    public sealed interface Set1 : Sqlite3TextEncoding

    /**
     * Set of [Sqlite3TextEncoding] including [UTF8], [UFT16LE], [UTF16BE].
     */
    public sealed interface Set2 : Sqlite3TextEncoding

    /**
     * IMP: R-37514-35566.
     */
    public data object UTF8 : Set1, Set2

    /**
     * IMP: R-03371-37637.
     */
    public data object UFT16LE : Set1, Set2

    /**
     * IMP: R-51971-34154
     */
    public data object UTF16BE : Set1, Set2

    /**
     * Use native byte order.
     */
    public data object UTF16 : Set1

    /**
     * sqlite3_create_collation() only.
     */
    public data object UTF16_ALIGNED : Sqlite3TextEncoding
}