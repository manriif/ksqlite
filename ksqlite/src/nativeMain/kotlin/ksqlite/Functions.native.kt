@file:Suppress("FunctionName")

package ksqlite

import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import ksqlite.memory.bufferPointer
import ksqlite.memory.getManaged
import ksqlite.memory.managedDestructor
import ksqlite.memory.managedPointer
import ksqlite.memory.wrap
import ksqlite.types.AutoVacuumPagesCallback
import ksqlite.types.Sqlite3BusyHandlerCallback
import ksqlite.types.Sqlite3CollationCompareCallback
import ksqlite.types.Sqlite3TextEncoding
import ksqlite.types.sqlite3
import ksqlite.types.sqlite3_context
import ksqlite.types.sqlite3_stmt
import ksqlite.types.sqlite3_value
import ksqlite.types.value
import sqlite.sqlite3_aggregate_context

public actual fun sqlite3_aggregate_context(
    context: sqlite3_context,
    nBytes: Int
): ksqlite.types.pointer? = wrap(
    sqlite3_aggregate_context(
        arg0 = context.pointer,
        nBytes = nBytes
    )
)

public actual fun sqlite3_autovacuum_pages(
    db: sqlite3,
    callback: AutoVacuumPagesCallback?
): Int = sqlite.sqlite3_autovacuum_pages(
    db = db.pointer,
    arg1 = callback?.let {
        staticCFunction { pointer, zSchema, nDbPage, nFreePage, nBytePerPage ->
            pointer.getManaged<AutoVacuumPagesCallback>()
                .invoke(zSchema!!.toKString(), nDbPage, nFreePage, nBytePerPage)
        }
    },
    arg2 = db.managedPointer(callback),
    arg3 = callback.managedDestructor()
)

public actual fun sqlite3_bind_blob(
    stmt: sqlite3_stmt,
    index: Int,
    zData: ByteArray?,
    nData: Int
): Int = sqlite.sqlite3_bind_blob(
    arg0 = stmt.pointer,
    arg1 = index,
    arg2 = stmt.bufferPointer(zData),
    n = nData,
    arg4 = sqlite.SQLITE_STATIC
)

public actual fun sqlite3_bind_pointer(
    stmt: sqlite3_stmt,
    index: Int,
    data: Any?,
    ptrType: String
): Int = sqlite.sqlite3_bind_pointer(
    arg0 = stmt.pointer,
    arg1 = index,
    arg2 = stmt.managedPointer(data),
    arg3 = stmt.stringPointer(ptrType),
    arg4 = sqlite.SQLITE_STATIC
)

public actual fun sqlite3_bind_text(
    stmt: sqlite3_stmt,
    index: Int,
    zData: String?,
    nData: Int
): Int = sqlite.sqlite3_bind_text(
    arg0 = stmt.pointer,
    arg1 = index,
    arg2 = zData,
    arg3 = nData,
    arg4 = sqlite.SQLITE_TRANSIENT
)

public actual fun sqlite3_bind_text64(
    stmt: sqlite3_stmt,
    index: Int,
    data: String?,
    nData: ULong,
    encoding: Sqlite3TextEncoding.Set1
): Int = sqlite.sqlite3_bind_text64(
    arg0 = stmt.pointer,
    arg1 = index,
    arg2 = data,
    arg3 = nData,
    arg4 = sqlite.SQLITE_TRANSIENT,
    encoding = encoding.value().toUByte()
)

public actual fun sqlite3_bind_value(
    stmt: sqlite3_stmt,
    index: Int,
    value: sqlite3_value
): Int = sqlite.sqlite3_bind_value(
    arg0 = stmt.pointer,
    arg1 = index,
    arg2 = value.pointer
)

public actual fun sqlite3_busy_handler(
    db: sqlite3,
    callback: Sqlite3BusyHandlerCallback?
): Int = sqlite.sqlite3_busy_handler(
    arg0 = db.pointer,
    arg1 = callback?.let {
        staticCFunction { pointer, count ->
            pointer.getManaged<Sqlite3BusyHandlerCallback>().invoke(count)
        }
    },
    arg2 = db.managedPointer(callback)
)