package ksqlite.types

import sqlite.SQLITE_UTF16
import sqlite.SQLITE_UTF16BE
import sqlite.SQLITE_UTF16LE
import sqlite.SQLITE_UTF16_ALIGNED
import sqlite.SQLITE_UTF8

/**
 * Returns the value for  `this` [Sqlite3TextEncoding].
 */
internal fun Sqlite3TextEncoding.value(): Int = when (this) {
    Sqlite3TextEncoding.UTF8 -> SQLITE_UTF8
    Sqlite3TextEncoding.UFT16LE -> SQLITE_UTF16LE
    Sqlite3TextEncoding.UTF16BE -> SQLITE_UTF16BE
    Sqlite3TextEncoding.UTF16 -> SQLITE_UTF16
    Sqlite3TextEncoding.UTF16_ALIGNED -> SQLITE_UTF16_ALIGNED
}