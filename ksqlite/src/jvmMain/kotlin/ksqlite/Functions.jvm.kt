@file:JvmName("Ksqlite")
@file:Suppress("FunctionName")

package ksqlite

import ksqlite.handlers.AutovacuumPagesHandler
import ksqlite.memory.pointer
import ksqlite.types.Sqlite3AutoVacuumPagesCallback
import ksqlite.types.Sqlite3Buffer
import ksqlite.types.Sqlite3DbConfigOption
import ksqlite.types.Sqlite3DestructorCallback
import ksqlite.types.Sqlite3Result
import ksqlite.types.createBuffer
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
): Sqlite3Buffer? = createBuffer(
    segment = nativeSqlite3.sqlite3_aggregate_context(
        context.pointer,
        nBytes
    ),
    size = nBytes
)

public actual fun sqlite3_autovacuum_pages(
    db: sqlite3,
    callback: Sqlite3AutoVacuumPagesCallback?,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(
    nativeSqlite3.sqlite3_autovacuum_pages(
        db.pointer,
        db.functionPointer(callback?.let { { AutovacuumPagesHandler(it) } }),
        db.referencePointer(callback, destructor),
        db.destructorFunctionPointer
    )
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
): Sqlite3Result = convertResult(
    nativeSqlite3.sqlite3_bind_pointer(
        stmt.pointer,
        index,
        stmt.referencePointer(data),
        stmt.stringPointer(ptrType),
        SqliteStatic
    )
)