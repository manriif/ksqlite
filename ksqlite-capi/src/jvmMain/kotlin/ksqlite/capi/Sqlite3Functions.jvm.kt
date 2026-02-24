@file:JvmName("Ksqlite")
@file:Suppress("FunctionName")

package ksqlite.capi

import ksqlite.capi.handlers.AutovacuumPagesHandler
import ksqlite.capi.memory.functionPointer
import ksqlite.capi.memory.pointer
import ksqlite.capi.types.Sqlite3AutoVacuumPagesCallback
import ksqlite.capi.types.Sqlite3DestructorCallback
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_pointer
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.sqliteLoadLibrary
import ksqlite.sqlite3 as nativeSqlite3

/**
 * Workaround to load the native library at file level.
 */
@Suppress("unused")
private val nativeInit = run { sqliteLoadLibrary() }

public actual fun sqlite3_aggregate_context(
    context: sqlite3_context,
    nBytes: Int
): sqlite3_pointer? = sqlite3_pointer.from(
    segment = nativeSqlite3.sqlite3_aggregate_context(
        context.pointer,
        nBytes
    ),
    size = nBytes.toLong()
)

public actual fun sqlite3_autovacuum_pages(
    db: sqlite3,
    userData: sqlite3_pointer?,
    destructor: Sqlite3DestructorCallback?,
    callback: Sqlite3AutoVacuumPagesCallback?
): Sqlite3Result = convertResult(
    nativeSqlite3.sqlite3_autovacuum_pages(
        db.pointer,
        db.memory.functionPointer(callback, ::AutovacuumPagesHandler),
        db.memory.referencePointer(callback, destructor),
        db.memory.destructorFunctionPointer
    )
)

public actual fun sqlite3_bind_blob(
    stmt: sqlite3_stmt,
    index: Int,
    data: ByteArray?,
    size: Int,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(
    nativeSqlite3.sqlite3_bind_blob(
        stmt.pointer,
        index,
        data.pointer(),
        size,
        SqliteTransient
    )
)

public actual fun sqlite3_bind_pointer(
    stmt: sqlite3_stmt,
    index: Int,
    data: sqlite3_pointer?,
    type: String,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(
    nativeSqlite3.sqlite3_bind_pointer(
        stmt.pointer,
        index,
        stmt.memory.referencePointer(data),
        stmt.memory.stringPointer(type),
        SqliteStatic
    )
)