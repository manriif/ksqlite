package ksqlite.types

/**
 * Flags for opening a BLOB.
 *
 * [Open A BLOB For Incremental I/O](https://sqlite.org/c3ref/blob_open.html)
 */
public sealed class SqliteBlobOpenFlag(public val value: Int) {

    /**
     * The BLOB is opened for read-only access.
     */
    public data object READONLY : SqliteBlobOpenFlag(0)

    /**
     * The BLOB is opened for read and write access.
     */
    public data object READWRITE : SqliteBlobOpenFlag(1)
}