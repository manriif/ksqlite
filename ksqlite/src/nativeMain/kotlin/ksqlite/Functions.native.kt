@file:Suppress("FunctionName")

package ksqlite

import kotlinx.cinterop.toKString
import sqlite.SQLITE_STATIC

public actual fun sqlite3_libversion(): String = sqlite.sqlite3_libversion()!!.toKString()

public actual fun sqlite3_aggregate_context(
    context: sqlite3_context,
    nBytes: Int
): pointer? = wrap(
    sqlite.sqlite3_aggregate_context(
        arg0 = context.pointer,
        nBytes = nBytes
    )
)

public actual fun sqlite3_bind_blob(
    stmt: sqlite3_stmt,
    index: Int,
    zData: ByteArray?,
    nData: Int
): Int = sqlite.sqlite3_bind_blob(
    arg0 = stmt.pointer,
    arg1 = index,
    arg2 = stmt.pin(zData),
    n = nData,
    arg4 = SQLITE_STATIC
)