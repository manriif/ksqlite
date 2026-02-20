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