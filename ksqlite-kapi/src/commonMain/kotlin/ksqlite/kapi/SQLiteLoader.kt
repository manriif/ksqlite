package ksqlite.kapi

/**
 * Loader for the SQLite library.
 */
public expect interface SQLiteLoader

/**
 * Returns an [SQLiteLoader] instance.
 */
internal expect fun sqliteLoader(): SQLiteLoader