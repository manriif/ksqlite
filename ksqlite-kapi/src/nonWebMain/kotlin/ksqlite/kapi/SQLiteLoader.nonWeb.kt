package ksqlite.kapi

/**
 * Loader for the SQLite library.
 */
public actual interface SQLiteLoader

internal object SQLiteLoaderImpl : SQLiteLoader

internal actual fun sqliteLoader(): SQLiteLoader = SQLiteLoaderImpl