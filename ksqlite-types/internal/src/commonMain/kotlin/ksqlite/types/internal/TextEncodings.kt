package ksqlite.types.internal

import ksqlite.types.SqliteTextEncoding

/**
 * Returns all [SqliteTextEncoding]s.
 */
private fun sqliteTextEncodings(): Set<SqliteTextEncoding> = setOf(
    SqliteTextEncoding.UTF8,
    SqliteTextEncoding.UFT16LE,
    SqliteTextEncoding.UTF16BE,
    SqliteTextEncoding.UTF16,
    SqliteTextEncoding.UTF16_ALIGNED,
)


/**
 * [SqliteTextEncoding]s associated by their integer value.
 */
@PublishedApi
internal val SqliteTextEncodings: Set<SqliteTextEncoding> = sqliteTextEncodings()

/**
 * Converts [encoding] into [SqliteTextEncoding].
 */
public inline fun <reified E : SqliteTextEncoding> convertTextEncoding(encoding: Int): E {
    val value = SqliteTextEncodings.firstOrNull { (encoding and it.value) == it.value }
    checkNotNull(value) { "Unknown sqlite3 text encoding $encoding" }
    check(value is E) { "Unexpected encoding type $value" }
    return value
}
