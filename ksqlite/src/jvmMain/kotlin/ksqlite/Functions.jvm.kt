@file:JvmName("Ksqlite")
@file:Suppress("FunctionName")

package ksqlite

import ksqlite.handlers.AutovacuumPagesHandler
import ksqlite.memory.functionPointer
import ksqlite.memory.pointer
import ksqlite.memory.referencePointer
import ksqlite.memory.wrap
import ksqlite.types.AutoVacuumPagesCallback
import ksqlite.types.Sqlite3Result
import ksqlite.types.pointer
import ksqlite.types.sqlite3
import ksqlite.types.sqlite3_context
import ksqlite.types.sqlite3_stmt
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

public actual fun sqlite3_autovacuum_pages(
    db: sqlite3,
    callback: AutoVacuumPagesCallback?
): Sqlite3Result = nativeSqlite3.sqlite3_autovacuum_pages(
    db.pointer,
    db.functionPointer(callback?.let { { AutovacuumPagesHandler(it) } }),
    db.referencePointer(callback),
    db.destructorFunctionPointer
)

public actual fun sqlite3_bind_blob(
    stmt: sqlite3_stmt,
    index: Int,
    zData: ByteArray?,
    nData: Int
): Sqlite3Result = convertResult(
    nativeSqlite3.sqlite3_bind_blob(
        stmt.pointer,
        index,
        zData.pointer(),
        nData,
        SqliteTransient
    )
)

public actual fun sqlite3_bind_pointer(
    stmt: sqlite3_stmt,
    index: Int,
    data: Any?,
    ptrType: String
): Sqlite3Result = nativeSqlite3.sqlite3_bind_pointer(
    stmt.pointer,
    index,
    stmt.referencePointer(data),
    stmt.stringPointer(ptrType),
    SqliteStatic
)