package ksqlite.capi.types

/**
 * Wrapper around [sqlite3_snapshot] intended to be passed as parameter and written by SQLite.
 */
public expect class SqliteSnapshotOutputParam() : OutputParam<sqlite3_snapshot?> {
    override val value: sqlite3_snapshot?
}