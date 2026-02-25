@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi

import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import ksqlite.SQLITE_OK
import ksqlite.capi.memory.data
import ksqlite.capi.memory.globalDisposer
import ksqlite.capi.memory.refDisposer
import ksqlite.capi.types.Sqlite3AutoExtensionCallback
import ksqlite.capi.types.Sqlite3AutoVacuumPagesCallback
import ksqlite.capi.types.Sqlite3DestructorCallback
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_backup
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_mutable_pointer
import ksqlite.capi.types.sqlite3_pointer
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.capi.utils.transform

public actual fun sqlite3_aggregate_context(
    context: sqlite3_context,
    nBytes: Int
): sqlite3_mutable_pointer? = sqlite3_mutable_pointer.from(
    pointer = ksqlite.sqlite3_aggregate_context(
        arg0 = context.pointer,
        nBytes = nBytes
    ),
    size = nBytes.toLong()
)

public actual fun sqlite3_auto_extension(callback: Sqlite3AutoExtensionCallback): Sqlite3Result {
    var result = SQLITE_OK

    if (AutoExtensions.isEmpty()) {
        result = ksqlite.ksqlite_auto_extension(staticCFunction(::autoExtensionHandler))
    }

    if (result == SQLITE_OK) {
        AutoExtensions.add(callback)
    }

    return convertResult(result)
}

public actual fun sqlite3_autovacuum_pages(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    destructor: Sqlite3DestructorCallback?,
    callback: Sqlite3AutoVacuumPagesCallback?
): Sqlite3Result = convertResult(
    ksqlite.sqlite3_autovacuum_pages(
        db = db.pointer,
        arg1 = callback?.let {
            staticCFunction { refPointer, zSchema, nDbPage, nFreePage, nBytePerPage ->
                val (callback, userData) = refPointer.data<Sqlite3AutoVacuumPagesCallback>()

                callback.invoke(
                    userData,
                    zSchema!!.toKString(),
                    nDbPage,
                    nFreePage,
                    nBytePerPage
                )
            }
        },
        arg2 = db.memory.refPointer(callback, destructor, userData),
        arg3 = refDisposer(callback, destructor)
    )
)

public actual fun sqlite3_backup_finish(backup: sqlite3_backup): Sqlite3Result = convertResult(
    ksqlite.sqlite3_backup_finish(backup.pointer)
)

public actual fun sqlite3_backup_init(
    destDb: sqlite3,
    destDbName: String,
    srcDb: sqlite3,
    srcDbName: String
): sqlite3_backup? = transform(::sqlite3_backup) {
    ksqlite.sqlite3_backup_init(
        pDest = destDb.pointer,
        zDestName = destDbName,
        pSource = srcDb.pointer,
        zSourceName = srcDbName
    )
}

public actual fun sqlite3_backup_pagecount(backup: sqlite3_backup): Int =
    ksqlite.sqlite3_backup_pagecount(backup.pointer)

public actual fun sqlite3_backup_remaining(backup: sqlite3_backup): Int =
    ksqlite.sqlite3_backup_remaining(backup.pointer)

public actual fun sqlite3_backup_step(
    backup: sqlite3_backup,
    nPage: Int
): Sqlite3Result = convertResult(
    ksqlite.sqlite3_backup_step(
        p = backup.pointer,
        nPage = nPage
    )
)

public actual fun sqlite3_bind_blob(
    stmt: sqlite3_stmt,
    index: Int,
    data: ByteArray?,
    size: Int
): Sqlite3Result = convertResult(
    ksqlite.sqlite3_bind_blob(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = stmt.memory.bufferPointer(data),
        n = size,
        arg4 = globalDisposer(data)
    )
)

public actual fun sqlite3_bind_blob64(
    stmt: sqlite3_stmt,
    index: Int,
    data: sqlite3_mutable_pointer?,
    size: Long,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(
    ksqlite.sqlite3_bind_blob64(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = data?.region?.nativeBuffer,
        arg3 = size.toULong(),
        arg4 =
    )
)

/*
public actual fun sqlite3_bind_pointer(
    stmt: sqlite3_stmt,
    index: Int,
    data: sqlite3_pointer?,
    type: String,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(
    ksqlite.sqlite3_bind_pointer(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = stmt.memory.refPointer(data),
        arg3 = stmt.memory.stringPointer(type),
        arg4 = ksqlite.SQLITE_STATIC
    )
)

public actual fun sqlite3_bind_text(
    stmt: sqlite3_stmt,
    index: Int,
    zData: String?,
    nData: Int
): Sqlite3Result = convertResult(
    ksqlite.sqlite3_bind_text(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = zData,
        arg3 = nData,
        arg4 = ksqlite.SQLITE_TRANSIENT
    )
)

public actual fun sqlite3_bind_text64(
    stmt: sqlite3_stmt,
    index: Int,
    data: String?,
    nData: ULong,
    encoding: Sqlite3TextEncoding.Set1
): Sqlite3Result = convertResult(
    ksqlite.sqlite3_bind_text64(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = data,
        arg3 = nData,
        arg4 = ksqlite.SQLITE_TRANSIENT,
        encoding = encoding.value.toUByte()
    )
)

public actual fun sqlite3_bind_value(
    stmt: sqlite3_stmt,
    index: Int,
    value: sqlite3_value
): Sqlite3Result = convertResult(
    ksqlite.sqlite3_bind_value(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = value.pointer
    )
)

public actual fun sqlite3_busy_handler(
    db: sqlite3,
    callback: Sqlite3BusyHandlerCallback?
): Sqlite3Result = convertResult(
    ksqlite.sqlite3_busy_handler(
        arg0 = db.pointer,
        arg1 = callback?.let {
            staticCFunction { userPtr, count ->
                userPtr.data<Sqlite3BusyHandlerCallback>()
                    .invoke(count)
            }
        },
        arg2 = db.memory.refPointer(callback)
    )
)

public actual fun sqlite3_close(db: sqlite3): Sqlite3Result = db.deallocate { pointer ->
    ksqlite.sqlite3_close(pointer)
}

public actual fun sqlite3_close_v2(db: sqlite3): Sqlite3Result = db.deallocate { pointer ->
    ksqlite.sqlite3_close_v2(pointer)
}

public actual fun sqlite3_exec(
    db: sqlite3,
    sql: String,
    callback: Sqlite3ExecCallback?,
    errMsg: Sqlite3StringUtf8Param?
): Sqlite3Result = useMemoryManager {
    convertResult(
        ksqlite.sqlite3_exec(
            arg0 = db.pointer,
            sql = sql,
            callback = callback?.let {
                staticCFunction { userPtr, columnCount, values, names ->
                    val columnValues = emptyArray<String?>() // TODO
                    val columnNames = emptyArray<String>() // TODO

                    userPtr.data<Sqlite3ExecCallback>()
                        .invoke(columnCount, columnValues, columnNames)
                }
            },
            arg3 = refPointer(callback),
            errmsg = paramPointer(errMsg)
        )
    )
}*/