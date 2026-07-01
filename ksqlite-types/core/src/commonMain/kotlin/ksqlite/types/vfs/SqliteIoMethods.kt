package ksqlite.types.vfs

/**
 * Describes an [`sqlite3_io_methods`](https://sqlite.org/c3ref/io_methods.html) struct.
 */
public interface SqliteIoMethods {

    /**
     * Version of the structure.
     */
    public val iVersion: SqliteIoMethodsVersion
}