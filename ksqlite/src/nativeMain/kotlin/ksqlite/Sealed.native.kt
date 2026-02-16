package ksqlite

import sqlite.SQLITE_UTF16
import sqlite.SQLITE_UTF16BE
import sqlite.SQLITE_UTF16LE
import sqlite.SQLITE_UTF16_ALIGNED
import sqlite.SQLITE_UTF8

/**
 * Returns the value for  `this` [TextEncoding].
 */
internal fun TextEncoding.value(): Int = when (this) {
    TextEncoding.Utf8 -> SQLITE_UTF8
    TextEncoding.Utf16Le -> SQLITE_UTF16LE
    TextEncoding.Utf16Be -> SQLITE_UTF16BE
    TextEncoding.Utf16 -> SQLITE_UTF16
    TextEncoding.Utf16Aligned -> SQLITE_UTF16_ALIGNED
}