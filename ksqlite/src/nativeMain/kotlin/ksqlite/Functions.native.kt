@file:Suppress("FunctionName")

package ksqlite

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.toKString
import sqlite.SQLITE_TRANSIENT

public actual fun sqlite3_libversion(): String {
    return sqlite.sqlite3_libversion()!!.toKString()
}

public actual fun sqlite3_bind_blob(
    stmt: sqlite3_stmt,
    index: Int,
    zData: ByteArray?,
    nData: Int
): Int = sqlite.sqlite3_bind_blob(
    arg0 = stmt.toCPointer(),
    arg1 = index,
    arg2 = dataPointer?.addressOf(0),
    n = nData,
    arg4 = SQLITE_TRANSIENT
)