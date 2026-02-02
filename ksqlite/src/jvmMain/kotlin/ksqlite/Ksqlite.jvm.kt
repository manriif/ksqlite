@file:JvmName("Ksqlite")

package ksqlite

/**
 * Workaround to load the native library at file level.
 */
@Suppress("unused")
private val nativeInit = run { ksqliteLoadLibrary() }

public actual val sqliteLibVersion: String
    get() = sqlite3.sqlite3_libversion().getString(0, Charsets.US_ASCII)