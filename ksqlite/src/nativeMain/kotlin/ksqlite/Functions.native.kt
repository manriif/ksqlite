@file:Suppress("FunctionName")

package ksqlite

import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString

public actual fun sqlite3_aggregate_context(
    context: sqlite3_context,
    nBytes: Int
): pointer? = wrap(
    sqlite.sqlite3_aggregate_context(
        arg0 = context.pointer,
        nBytes = nBytes
    )
)

public actual fun <Data> sqlite3_autovacuum_pages(
    db: sqlite3,
    pArg: Data?,
    xCallback: AutoVacuumPagesCallback<Data>?
): Int = sqlite.sqlite3_autovacuum_pages(
    db = db.pointer,
    arg1 = xCallback?.let {
        staticCFunction { pointer, zSchema, nDbPage, nFreePage, nBytePerPage ->
            pointer!!.asStableRef<AutovacuumPages<Data>>().get().run {
                callback?.invoke(data, zSchema!!.toKString(), nDbPage, nFreePage, nBytePerPage)
            } ?: 0U
        }
    },
    arg2 = StableRef.create(AutovacuumPages(pArg, xCallback)).asCPointer(),
    arg3 = staticCFunction { pointer ->
        pointer!!.asStableRef<AutovacuumPages<Data>>().dispose()
    }
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
    arg2 = stmt.referencePointer(data),
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
    zData: String?,
    nData: ULong,
    encoding: TextEncoding.Common
): Int = sqlite.sqlite3_bind_text64(
    arg0 = stmt.pointer,
    arg1 = index,
    arg2 = zData,
    arg3 = nData,
    arg4 = sqlite.SQLITE_TRANSIENT,
    encoding = encoding.value
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