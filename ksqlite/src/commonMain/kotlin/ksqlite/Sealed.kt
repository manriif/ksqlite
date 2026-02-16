package ksqlite

///////////////////////////////////////////////////////////////////////////
// Data type
///////////////////////////////////////////////////////////////////////////

/**
 * Every value in SQLite has one of five fundamental datatypes:
 * These constants are codes for each of those types.
 *
 * [Fundamental Datatypes](https://sqlite.org/c3ref/c_blob.html)
 */
public sealed interface DataType {

    /**
     * 64-bit signed integer.
     */
    public data object Integer : DataType

    /**
     * 64-bit IEEE floating point number.
     */
    public data object Float : DataType

    /**
     * String.
     */
    public data object Text : DataType

    /**
     * BLOB.
     */
    public data object Blob : DataType

    /**
     * NULL.
     */
    public data object Null : DataType
}

///////////////////////////////////////////////////////////////////////////
// Encoding
///////////////////////////////////////////////////////////////////////////

/**
 * These constants define integer codes that represent the various text encodings supported by
 * SQLite.
 *
 * [Text Encodings](https://sqlite.org/c3ref/c_any.html)
 */
public sealed interface TextEncoding {

    /**
     * Set of [TextEncoding] including [Utf8], [Utf16Le], [Utf16Be] and [Utf16].
     */
    public sealed interface Set1 : TextEncoding

    /**
     * Set of [TextEncoding] including [Utf8], [Utf16Le], [Utf16Be].
     */
    public sealed interface Set2 : TextEncoding

    /**
     * IMP: R-37514-35566.
     */
    public data object Utf8 : Set1, Set2

    /**
     * IMP: R-03371-37637.
     */
    public data object Utf16Le : Set1, Set2

    /**
     * IMP: R-51971-34154
     */
    public data object Utf16Be : Set1, Set2

    /**
     * Use native byte order.
     */
    public data object Utf16 : Set1

    /**
     * [sqlite3_create_collation] only.
     */
    public data object Utf16Aligned : TextEncoding
}