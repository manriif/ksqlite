package ksqlite.types

/**
 * Callback for [ksqlite.sqlite3_autovacuum_pages].
 */
public typealias Sqlite3AutoVacuumPagesCallback = (
    zSchema: String,
    nDbPage: UInt,
    nFreePage: UInt,
    nBytePerPage: UInt
) -> UInt

/**
 * Callback for [ksqlite.sqlite3_wal_hook].
 */
public typealias Sqlite3WalCallback = (
    userData: sqlite3_pointer?,
    db: Sqlite3Param<sqlite3>,
    dbName: String,
    nPage: Int
) -> Sqlite3Result.OkOrFailure