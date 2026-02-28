package ksqlite.capi.types

/**
 * Wrapper around [sqlite3_blob] intended to be passed as parameter and allocated by SQLite.
 */
public expect class Sqlite3BlobOutParam() : Sqlite3OutParam<sqlite3_blob?> {
    override val value: sqlite3_blob?
}

/**
 * Wrapper around [sqlite3_snapshot] intended to be passed as parameter and allocated by SQLite.
 */
public expect class Sqlite3SnapshotOutParam() : Sqlite3OutParam<sqlite3_snapshot?> {
    override val value: sqlite3_snapshot?
}