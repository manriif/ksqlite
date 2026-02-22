@file:Suppress("FunctionName")

package ksqlite

import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import ksqlite.memory.getReferencedData
import ksqlite.memory.referenceDestructor
import ksqlite.memory.useMemoryManager
import ksqlite.types.Sqlite3AutoVacuumPagesCallback
import ksqlite.types.Sqlite3BusyHandlerCallback
import ksqlite.types.Sqlite3DestructorCallback
import ksqlite.types.Sqlite3ExecCallback
import ksqlite.types.Sqlite3Result
import ksqlite.types.Sqlite3TextEncoding
import ksqlite.types.Sqlite3Utf8Param
import ksqlite.types.sqlite3
import ksqlite.types.sqlite3_context
import ksqlite.types.sqlite3_pointer
import ksqlite.types.sqlite3_stmt
import ksqlite.types.sqlite3_value
import sqlite.sqlite3_aggregate_context

public actual fun sqlite3_aggregate_context(
    context: sqlite3_context,
    nBytes: Int
): sqlite3_pointer? = sqlite3_pointer.from(
    pointer = sqlite3_aggregate_context(
        arg0 = context.pointer,
        nBytes = nBytes
    ),
    size = nBytes.toLong()
)

public actual fun sqlite3_autovacuum_pages(
    db: sqlite3,
    callback: Sqlite3AutoVacuumPagesCallback?,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(
    sqlite.sqlite3_autovacuum_pages(
        db = db.pointer,
        arg1 = callback?.let {
            staticCFunction { userPtr, zSchema, nDbPage, nFreePage, nBytePerPage ->
                userPtr.getReferencedData<Sqlite3AutoVacuumPagesCallback>()
                    .invoke(zSchema!!.toKString(), nDbPage, nFreePage, nBytePerPage)
            }
        },
        arg2 = db.memory.referencePointer(callback, destructor),
        arg3 = referenceDestructor(callback, destructor)
    )
)

public actual fun sqlite3_bind_blob(
    stmt: sqlite3_stmt,
    index: Int,
    zData: ByteArray?,
    nData: Int
): Sqlite3Result = convertResult(
    sqlite.sqlite3_bind_blob(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = stmt.memory.bufferPointer(zData),
        n = nData,
        arg4 = sqlite.SQLITE_STATIC
    )
)

public actual fun sqlite3_bind_pointer(
    stmt: sqlite3_stmt,
    index: Int,
    data: Any?,
    ptrType: String
): Sqlite3Result = convertResult(
    sqlite.sqlite3_bind_pointer(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = stmt.memory.referencePointer(data),
        arg3 = stmt.memory.stringPointer(ptrType),
        arg4 = sqlite.SQLITE_STATIC
    )
)

public actual fun sqlite3_bind_text(
    stmt: sqlite3_stmt,
    index: Int,
    zData: String?,
    nData: Int
): Sqlite3Result = convertResult(
    sqlite.sqlite3_bind_text(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = zData,
        arg3 = nData,
        arg4 = sqlite.SQLITE_TRANSIENT
    )
)

public actual fun sqlite3_bind_text64(
    stmt: sqlite3_stmt,
    index: Int,
    data: String?,
    nData: ULong,
    encoding: Sqlite3TextEncoding.Set1
): Sqlite3Result = convertResult(
    sqlite.sqlite3_bind_text64(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = data,
        arg3 = nData,
        arg4 = sqlite.SQLITE_TRANSIENT,
        encoding = encoding.value.toUByte()
    )
)

public actual fun sqlite3_bind_value(
    stmt: sqlite3_stmt,
    index: Int,
    value: sqlite3_value
): Sqlite3Result = convertResult(
    sqlite.sqlite3_bind_value(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = value.pointer
    )
)

public actual fun sqlite3_busy_handler(
    db: sqlite3,
    callback: Sqlite3BusyHandlerCallback?
): Sqlite3Result = convertResult(
    sqlite.sqlite3_busy_handler(
        arg0 = db.pointer,
        arg1 = callback?.let {
            staticCFunction { userPtr, count ->
                userPtr.getReferencedData<Sqlite3BusyHandlerCallback>()
                    .invoke(count)
            }
        },
        arg2 = db.memory.referencePointer(callback)
    )
)

public actual fun sqlite3_close(db: sqlite3): Sqlite3Result = db.deallocate { pointer ->
    sqlite.sqlite3_close(pointer)
}

public actual fun sqlite3_close_v2(db: sqlite3): Sqlite3Result = db.deallocate { pointer ->
    sqlite.sqlite3_close_v2(pointer)
}

public actual fun sqlite3_exec(
    db: sqlite3,
    sql: String,
    callback: Sqlite3ExecCallback?,
    errMsg: Sqlite3Utf8Param?
): Sqlite3Result = useMemoryManager {
    convertResult(
        sqlite.sqlite3_exec(
            arg0 = db.pointer,
            sql = sql,
            callback = callback?.let {
                staticCFunction { userPtr, columnCount, values, names ->
                    val columnValues = emptyArray<String?>() // TODO
                    val columnNames = emptyArray<String>() // TODO

                    userPtr.getReferencedData<Sqlite3ExecCallback>()
                        .invoke(columnCount, columnValues, columnNames)
                }
            },
            arg3 = referencePointer(callback),
            errmsg = paramPointer(errMsg)
        )
    )
}

public actual fun sqlite3_open(
    name: String,
    db: sqlite3
): Sqlite3Result = db.allocate { pointer ->
    sqlite.sqlite3_open(name, pointer)
}