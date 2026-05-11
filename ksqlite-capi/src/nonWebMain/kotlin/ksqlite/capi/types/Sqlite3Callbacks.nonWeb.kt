package ksqlite.capi.types

/**
 * Callback for [ksqlite.capi.sqlite3_wal_hook].
 */
public typealias Sqlite3WalHookCallback = (
    userData: sqlite3_mutable_pointer?,
    db: sqlite3,
    dbName: String,
    nPage: Int
) -> Sqlite3Result.OkOrFailure