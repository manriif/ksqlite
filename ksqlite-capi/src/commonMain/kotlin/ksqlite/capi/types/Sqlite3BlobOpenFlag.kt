package ksqlite.capi.types

/**
 * Flags for opening a BLOB.
 *
 * [Open A BLOB For Incremental I/O](https://sqlite.org/c3ref/blob_open.html)
 */
public sealed class Sqlite3BlobOpenFlag(internal val value: Int) {

    /**
     * The BLOB is opened for read-only access.
     */
    public data object READONLY : Sqlite3BlobOpenFlag(0)

    /**
     * The BLOB is opened for read and write access.
     */
    public data object READWRITE : Sqlite3BlobOpenFlag(1)
}