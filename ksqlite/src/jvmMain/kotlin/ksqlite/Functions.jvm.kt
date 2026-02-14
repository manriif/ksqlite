@file:JvmName("Ksqlite")
@file:Suppress("FunctionName")

package ksqlite

import sqlite.sqliteLoadLibrary
import sqlite.sqlite3 as nativeSqlite3
import java.lang.foreign.MemoryLayout as Pointer
import java.lang.foreign.ValueLayout.ADDRESS as POINTER
import java.lang.foreign.ValueLayout.JAVA_INT as INT

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

public actual fun <Data : Any> sqlite3_autovacuum_pages(
    db: sqlite3,
    pArg: Data?,
    xCallback: AutoVacuumPagesCallback<Data>?
): Int = nativeSqlite3.sqlite3_autovacuum_pages(
    db.pointer,
    segment(xCallback) {
        db.pointer(
            POINTER,
            POINTER,
            INT,
            INT,
            INT,
            returnLayout = INT,
            function = { data: Pointer, zSchema: Pointer, nDbPage: Int, nFreePage: Int, nBytePerPage: Int ->
                data
            }
        )
    },
    db.pointer(AutovacuumPages(db, pArg, xCallback)),
    db.pointer(POINTER, function = { data: Pointer ->

    })
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
    stmt.pointer(data),
    stmt.pointer(ptrType),
    SqliteStatic
)