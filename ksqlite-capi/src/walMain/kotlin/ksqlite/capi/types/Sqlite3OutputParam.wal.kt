package ksqlite.capi.types

/**
 * Wrapper around [sqlite3_snapshot] intended to be passed as parameter and written by SQLite.
 */
public expect class Sqlite3SnapshotOutputParam() : OutputParam<sqlite3_snapshot?> {
    override val value: sqlite3_snapshot?
}