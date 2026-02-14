@file:JvmName("Ksqlite")
@file:Suppress("FunctionName")

package ksqlite

import ksqlite.handler.AutovacuumPagesHandler
import sqlite.sqliteLoadLibrary
import sqlite.sqlite3 as nativeSqlite3

/**
 * Workaround to load the native library at file level.
 */
@Suppress("unused")
private val nativeInit = run { sqliteLoadLibrary() }

public actual fun sqlite3_aggregate_context(
    context: sqlite3_context,
    nBytes: Int
): pointer? = wrap(
    nativeSqlite3.sqlite3_aggregate_context(
        context.pointer,
        nBytes
    )
)

public actual fun <Data> sqlite3_autovacuum_pages(
    db: sqlite3,
    pArg: Data?,
    xCallback: AutoVacuumPagesCallback<Data>?
): Int = nativeSqlite3.sqlite3_autovacuum_pages(
    db.pointer,
    db.functionPointer(xCallback?.let { { AutovacuumPagesHandler<Data>(it) } }),
    db.referencePointer(AutovacuumPages(pArg, xCallback)),
    db.destructorFunctionPointer
)

public actual fun sqlite3_bind_blob(
    stmt: sqlite3_stmt,
    index: Int,
    zData: ByteArray?,
    nData: Int
): Int = nativeSqlite3.sqlite3_bind_blob(
    stmt.pointer,
    index,
    zData.pointer(),
    nData,
    SqliteTransient
)

public actual fun sqlite3_bind_pointer(
    stmt: sqlite3_stmt,
    index: Int,
    data: Any?,
    ptrType: String
): Int = nativeSqlite3.sqlite3_bind_pointer(
    stmt.pointer,
    index,
    stmt.referencePointer(data),
    stmt.stringPointer(ptrType),
    SqliteStatic
)