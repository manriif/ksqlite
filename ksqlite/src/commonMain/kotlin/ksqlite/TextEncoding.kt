package ksqlite

/**
 * These constants define integer codes that represent the various text encodings supported by
 * SQLite.
 *
 * [Text Encodings](https://sqlite.org/c3ref/c_any.html)
 */
public sealed class TextEncoding(internal val constant: UShort) {

    /**
     * Encoding available everywhere.
     */
    public sealed class Common(constant: UShort) : TextEncoding(constant)

    /**
     * IMP: R-37514-35566.
     */
    public data object Utf8 : Common(1u)

    /**
     * IMP: R-03371-37637.
     */
    public data object Utf16Le : Common(2u)

    /**
     * IMP: R-51971-34154
     */
    public data object Utf16Be : Common(3u)

    /**
     * Use native byte order.
     */
    public data object Utf16 : Common(4u)

    /**
     * [sqlite3_create_collation] only.
     */
    public data object Utf16Aligned : TextEncoding(8u)
}