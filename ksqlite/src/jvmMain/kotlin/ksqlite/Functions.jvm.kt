@file:JvmName("Ksqlite")
@file:Suppress("FunctionName")

package ksqlite

import sqlite.sqliteLoadLibrary
import java.lang.foreign.MemorySegment
import sqlite.sqlite3 as nativeSqlite3

/**
 * Workaround to load the native library at file level.
 */
@Suppress("unused")
private val nativeInit = run { sqliteLoadLibrary() }

public actual fun sqlite3_libversion(): String {
    return nativeSqlite3.sqlite3_libversion().getString(0)
}

public actual fun sqlite3_aggregate_context(
    context: sqlite3_context,
    nBytes: Int
): pointer? = wrap(
    nativeSqlite3.sqlite3_aggregate_context(
        context.segment,
        nBytes
    )
)

public actual fun sqlite3_bind_blob(
    stmt: sqlite3_stmt,
    index: Int,
    zData: ByteArray?,
    nData: Int
): Int = nativeSqlite3.sqlite3_bind_blob(
    stmt.segment,
    index,
    zData?.let(MemorySegment::ofArray) ?: MemorySegment.NULL,
    nData,
    SqliteTransient
)