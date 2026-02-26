@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.SQLITE_OK
import ksqlite.SQLITE_TRANSIENT
import ksqlite.capi.memory.deallocateNullable
import ksqlite.capi.memory.globalDisposer
import ksqlite.capi.memory.globalRefPointer
import ksqlite.capi.memory.memory
import ksqlite.capi.memory.refData
import ksqlite.capi.memory.refDisposer
import ksqlite.capi.memory.userDataDestructor
import ksqlite.capi.types.Sqlite3AutoExtensionCallback
import ksqlite.capi.types.Sqlite3AutoVacuumPagesCallback
import ksqlite.capi.types.Sqlite3BlobOpenFlag
import ksqlite.capi.types.Sqlite3BlobParam
import ksqlite.capi.types.Sqlite3BusyHandlerCallback
import ksqlite.capi.types.Sqlite3CollationCompareCallback
import ksqlite.capi.types.Sqlite3CollationNeededCallback
import ksqlite.capi.types.Sqlite3CommitCallback
import ksqlite.capi.types.Sqlite3CompleteResult
import ksqlite.capi.types.Sqlite3ConfigOption
import ksqlite.capi.types.Sqlite3CreateFunctionFinalCallback
import ksqlite.capi.types.Sqlite3CreateFunctionFuncCallback
import ksqlite.capi.types.Sqlite3CreateFunctionInverseCallback
import ksqlite.capi.types.Sqlite3CreateFunctionStepCallback
import ksqlite.capi.types.Sqlite3CreateFunctionValueCallback
import ksqlite.capi.types.Sqlite3DataType
import ksqlite.capi.types.Sqlite3DestructorCallback
import ksqlite.capi.types.Sqlite3LogCallback
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.Sqlite3SqlLogCallback
import ksqlite.capi.types.Sqlite3TextEncoding
import ksqlite.capi.types.s3
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_backup
import ksqlite.capi.types.sqlite3_blob
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_module
import ksqlite.capi.types.sqlite3_mutable_pointer
import ksqlite.capi.types.sqlite3_pointer
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.capi.types.sqlite3_value
import ksqlite.capi.types.useMemScoped
import ksqlite.capi.utils.toKString
import ksqlite.capi.utils.transform
import ksqlite.ksqlite_auto_extension
import ksqlite.sqlite3_aggregate_context
import ksqlite.sqlite3_autovacuum_pages
import ksqlite.sqlite3_backup_finish
import ksqlite.sqlite3_backup_init
import ksqlite.sqlite3_backup_pagecount
import ksqlite.sqlite3_backup_remaining
import ksqlite.sqlite3_backup_step
import ksqlite.sqlite3_bind_blob
import ksqlite.sqlite3_bind_blob64
import ksqlite.sqlite3_bind_double
import ksqlite.sqlite3_bind_int
import ksqlite.sqlite3_bind_int64
import ksqlite.sqlite3_bind_null
import ksqlite.sqlite3_bind_parameter_count
import ksqlite.sqlite3_bind_parameter_index
import ksqlite.sqlite3_bind_parameter_name
import ksqlite.sqlite3_bind_pointer
import ksqlite.sqlite3_bind_text
import ksqlite.sqlite3_bind_text64
import ksqlite.sqlite3_bind_value
import ksqlite.sqlite3_bind_zeroblob
import ksqlite.sqlite3_bind_zeroblob64
import ksqlite.sqlite3_blob_bytes
import ksqlite.sqlite3_blob_close
import ksqlite.sqlite3_blob_open
import ksqlite.sqlite3_blob_read
import ksqlite.sqlite3_blob_reopen
import ksqlite.sqlite3_blob_write
import ksqlite.sqlite3_busy_handler
import ksqlite.sqlite3_busy_timeout
import ksqlite.sqlite3_changes
import ksqlite.sqlite3_changes64
import ksqlite.sqlite3_clear_bindings
import ksqlite.sqlite3_close
import ksqlite.sqlite3_close_v2
import ksqlite.sqlite3_column_blob
import ksqlite.sqlite3_column_bytes
import ksqlite.sqlite3_column_count
import ksqlite.sqlite3_column_database_name
import ksqlite.sqlite3_column_decltype
import ksqlite.sqlite3_column_double
import ksqlite.sqlite3_column_int
import ksqlite.sqlite3_column_int64
import ksqlite.sqlite3_column_name
import ksqlite.sqlite3_column_origin_name
import ksqlite.sqlite3_column_table_name
import ksqlite.sqlite3_column_text
import ksqlite.sqlite3_column_type
import ksqlite.sqlite3_column_value
import ksqlite.sqlite3_commit_hook
import ksqlite.sqlite3_compileoption_get
import ksqlite.sqlite3_compileoption_used
import ksqlite.sqlite3_complete
import ksqlite.sqlite3_config

public actual fun sqlite3_aggregate_context(
    context: sqlite3_context,
    nBytes: Int
): sqlite3_mutable_pointer? = sqlite3_mutable_pointer.from(
    pointer = sqlite3_aggregate_context(
        arg0 = context.pointer,
        nBytes = nBytes
    ),
    size = nBytes.toLong()
)

public actual fun sqlite3_auto_extension(callback: Sqlite3AutoExtensionCallback): Sqlite3Result {
    var result = SQLITE_OK

    if (AutoExtensions.isEmpty()) {
        result = ksqlite_auto_extension(AutoExtensionHandler)
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
    sqlite3_autovacuum_pages(
        db = db.pointer,
        arg1 = callback?.let {
            staticCFunction { refPointer, zSchema, nDbPage, nFreePage, nBytePerPage ->
                val (callback, userData) = refData<Sqlite3AutoVacuumPagesCallback>(refPointer)

                callback(
                    userData,
                    zSchema!!.toKStringFromUtf8(),
                    nDbPage,
                    nFreePage,
                    nBytePerPage
                )
            }
        },
        arg2 = db.memory.refPointer(callback, userData, destructor),
        arg3 = refDisposer(callback, destructor)
    )
)

public actual fun sqlite3_backup_finish(backup: sqlite3_backup): Sqlite3Result = convertResult(
    sqlite3_backup_finish(backup.pointer)
)

public actual fun sqlite3_backup_init(
    destDb: sqlite3,
    destDbName: String,
    srcDb: sqlite3,
    srcDbName: String
): sqlite3_backup? = transform(::sqlite3_backup) {
    sqlite3_backup_init(
        pDest = destDb.pointer,
        zDestName = destDbName,
        pSource = srcDb.pointer,
        zSourceName = srcDbName
    )
}

public actual fun sqlite3_backup_pagecount(backup: sqlite3_backup): Int =
    sqlite3_backup_pagecount(backup.pointer)

public actual fun sqlite3_backup_remaining(backup: sqlite3_backup): Int =
    sqlite3_backup_remaining(backup.pointer)

public actual fun sqlite3_backup_step(
    backup: sqlite3_backup,
    nPage: Int
): Sqlite3Result = convertResult(
    sqlite3_backup_step(
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
    sqlite3_bind_blob(
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
    sqlite3_bind_blob64(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = data?.block?.pointer,
        arg3 = size.convert(),
        arg4 = userDataDestructor(data, destructor)
    )
)

public actual fun sqlite3_bind_double(
    stmt: sqlite3_stmt,
    index: Int,
    value: Double
): Sqlite3Result = convertResult(
    sqlite3_bind_double(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = value
    )
)

public actual fun sqlite3_bind_int(
    stmt: sqlite3_stmt,
    index: Int,
    value: Int
): Sqlite3Result = convertResult(
    sqlite3_bind_int(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = value
    )
)

public actual fun sqlite3_bind_int64(
    stmt: sqlite3_stmt,
    index: Int,
    value: Long
): Sqlite3Result = convertResult(
    sqlite3_bind_int64(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = value
    )
)

public actual fun sqlite3_bind_null(
    stmt: sqlite3_stmt,
    index: Int
): Sqlite3Result = convertResult(
    sqlite3_bind_null(
        arg0 = stmt.pointer,
        arg1 = index,
    )
)

public actual fun sqlite3_bind_parameter_count(stmt: sqlite3_stmt): Int =
    sqlite3_bind_parameter_count(stmt.pointer)

public actual fun sqlite3_bind_parameter_index(
    stmt: sqlite3_stmt,
    name: String
): Int = sqlite3_bind_parameter_index(
    arg0 = stmt.pointer,
    zName = name
)

public actual fun sqlite3_bind_parameter_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = sqlite3_bind_parameter_name(
    arg0 = stmt.pointer,
    arg1 = index
)?.toKStringFromUtf8()

public actual fun sqlite3_bind_pointer(
    stmt: sqlite3_stmt,
    index: Int,
    data: sqlite3_mutable_pointer?,
    type: String?,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(
    sqlite3_bind_pointer(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = data?.block?.pointer,
        arg3 = stmt.memory.stringPointer(type),
        arg4 = userDataDestructor(data, destructor)
    )
)

public actual fun sqlite3_bind_text(
    stmt: sqlite3_stmt,
    index: Int,
    text: String?,
    size: Int
): Sqlite3Result = convertResult(
    sqlite3_bind_text(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = text,
        arg3 = size,
        arg4 = SQLITE_TRANSIENT
    )
)

public actual fun sqlite3_bind_text64(
    stmt: sqlite3_stmt,
    index: Int,
    data: sqlite3_mutable_pointer?,
    size: Long,
    encoding: Sqlite3TextEncoding.Set1,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(
    sqlite3_bind_text64(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = data?.block?.pointer,
        arg3 = size.convert(),
        arg4 = userDataDestructor(data, destructor),
        encoding = encoding.value.convert()
    )
)

public actual fun sqlite3_bind_value(
    stmt: sqlite3_stmt,
    index: Int,
    value: sqlite3_value
): Sqlite3Result = convertResult(
    sqlite3_bind_value(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = value.pointer
    )
)

public actual fun sqlite3_bind_zeroblob(
    stmt: sqlite3_stmt,
    index: Int,
    size: Int
): Sqlite3Result = convertResult(
    sqlite3_bind_zeroblob(
        arg0 = stmt.pointer,
        arg1 = index,
        n = size
    )
)

public actual fun sqlite3_bind_zeroblob64(
    stmt: sqlite3_stmt,
    index: Int,
    size: ULong
): Sqlite3Result = convertResult(
    sqlite3_bind_zeroblob64(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = size
    )
)

public actual fun sqlite3_blob_bytes(blob: sqlite3_blob): Int =
    sqlite3_blob_bytes(blob.pointer)

public actual fun sqlite3_blob_close(blob: sqlite3_blob): Sqlite3Result = convertResult(
    sqlite3_blob_close(blob.pointer)
)

public actual fun sqlite3_blob_open(
    db: sqlite3,
    databaseName: String,
    tableName: String,
    columnName: String,
    rowIndex: Long,
    flags: Sqlite3BlobOpenFlag,
    outBlob: Sqlite3BlobParam
): Sqlite3Result = convertResult(
    outBlob.useMemScoped { blob ->
        sqlite3_blob_open(
            arg0 = db.pointer,
            zDb = databaseName,
            zTable = tableName,
            zColumn = columnName,
            iRow = rowIndex,
            flags = flags.value,
            ppBlob = blob
        )
    }
)

public actual fun sqlite3_blob_read(
    blob: sqlite3_blob,
    buffer: ByteArray,
    size: Int,
    offset: Int
): Sqlite3Result = convertResult(
    sqlite3_blob_read(
        arg0 = blob.pointer,
        Z = buffer.refTo(0),
        N = size,
        iOffset = offset
    )
)

public actual fun sqlite3_blob_reopen(
    blob: sqlite3_blob,
    rowIndex: Long
): Sqlite3Result = convertResult(
    sqlite3_blob_reopen(
        arg0 = blob.pointer,
        arg1 = rowIndex
    )
)

public actual fun sqlite3_blob_write(
    blob: sqlite3_blob,
    buffer: ByteArray,
    size: Int,
    offset: Int
): Sqlite3Result = convertResult(
    sqlite3_blob_write(
        arg0 = blob.pointer,
        z = buffer.refTo(0),
        n = size,
        iOffset = offset
    )
)

public actual fun sqlite3_busy_handler(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3BusyHandlerCallback?
): Sqlite3Result = convertResult(
    sqlite3_busy_handler(
        arg0 = db.pointer,
        arg1 = callback?.let {
            staticCFunction { refPointer, count ->
                val (callback, userData) = refData<Sqlite3BusyHandlerCallback>(refPointer)
                callback(userData, count)
            }
        },
        arg2 = db.memory.refPointer(callback, userData)
    )
)

public actual fun sqlite3_busy_timeout(
    db: sqlite3,
    millis: Int
): Sqlite3Result = convertResult(
    sqlite3_busy_timeout(
        arg0 = db.pointer,
        ms = millis
    )
)

public actual fun sqlite3_cancel_auto_extension(callback: Sqlite3AutoExtensionCallback): Int {
    val removed = AutoExtensions.remove(callback)

    if (!removed) {
        return 0
    }

    if (AutoExtensions.isEmpty()) {
        val _ = ksqlite.ksqlite_cancel_auto_extension(AutoExtensionHandler)
    }

    return 1
}

public actual fun sqlite3_changes(db: sqlite3): Int = sqlite3_changes(db.pointer)

public actual fun sqlite3_changes64(db: sqlite3): Long = sqlite3_changes64(db.pointer)

public actual fun sqlite3_clear_bindings(stmt: sqlite3_stmt): Sqlite3Result =
    convertResult(sqlite3_clear_bindings(stmt.pointer))

public actual fun sqlite3_close(db: sqlite3?): Sqlite3Result =
    db.deallocateNullable { sqlite3_close(it?.pointer) }

public actual fun sqlite3_close_v2(db: sqlite3?): Sqlite3Result =
    db.deallocateNullable { sqlite3_close_v2(it?.pointer) }

public actual fun sqlite3_collation_needed(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3CollationNeededCallback?,
): Sqlite3Result = convertResult(
    ksqlite.collation_needed()
)

public actual fun sqlite3_column_blob(
    stmt: sqlite3_stmt,
    index: Int
): sqlite3_pointer? = sqlite3_pointer.from(
    pointer = sqlite3_column_blob(
        arg0 = stmt.pointer,
        iCol = index
    ),
    size = sqlite3_column_bytes(
        arg0 = stmt.pointer,
        iCol = index
    ).toLong()
)

public actual fun sqlite3_column_bytes(
    stmt: sqlite3_stmt,
    index: Int
): Int = sqlite3_column_bytes(
    arg0 = stmt.pointer,
    iCol = index
)

public actual fun sqlite3_column_count(stmt: sqlite3_stmt): Int =
    sqlite3_column_count(stmt.pointer)

public actual fun sqlite3_column_database_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = sqlite3_column_database_name(
    arg0 = stmt.pointer,
    arg1 = index
)?.toKStringFromUtf8()

public actual fun sqlite3_column_decltype(
    stmt: sqlite3_stmt,
    index: Int
): String? = sqlite3_column_decltype(
    arg0 = stmt.pointer,
    arg1 = index
)?.toKStringFromUtf8()

public actual fun sqlite3_column_double(
    stmt: sqlite3_stmt,
    index: Int
): Double = sqlite3_column_double(
    arg0 = stmt.pointer,
    iCol = index
)

public actual fun sqlite3_column_int(
    stmt: sqlite3_stmt,
    index: Int
): Int = sqlite3_column_int(
    arg0 = stmt.pointer,
    iCol = index
)

public actual fun sqlite3_column_int64(
    stmt: sqlite3_stmt,
    index: Int
): Long = sqlite3_column_int64(
    arg0 = stmt.pointer,
    iCol = index
)

public actual fun sqlite3_column_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = sqlite3_column_name(
    arg0 = stmt.pointer,
    N = index
)?.toKStringFromUtf8()

public actual fun sqlite3_column_origin_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = sqlite3_column_origin_name(
    arg0 = stmt.pointer,
    arg1 = index
)?.toKStringFromUtf8()

public actual fun sqlite3_column_table_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = sqlite3_column_table_name(
    arg0 = stmt.pointer,
    arg1 = index
)?.toKStringFromUtf8()

public actual fun sqlite3_column_text(
    stmt: sqlite3_stmt,
    index: Int
): String? = sqlite3_column_text(
    arg0 = stmt.pointer,
    iCol = index
)?.reinterpret<ByteVar>()
    ?.toKStringFromUtf8()

public actual fun sqlite3_column_type(
    stmt: sqlite3_stmt,
    index: Int
): Sqlite3DataType = convertDataType(
    sqlite3_column_type(
        arg0 = stmt.pointer,
        iCol = index
    )
)

public actual fun sqlite3_column_value(
    stmt: sqlite3_stmt,
    index: Int
): sqlite3_value? = sqlite3_column_value(
    arg0 = stmt.pointer,
    iCol = index
)?.let(::sqlite3_value)

public actual fun sqlite3_commit_hook(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3CommitCallback?
): sqlite3_mutable_pointer? = sqlite3_mutable_pointer.fromDisposeRef(
    pointer = sqlite3_commit_hook(
        arg0 = db.pointer,
        arg1 = callback?.let {
            staticCFunction { refPointer ->
                val (callback, userData) = refData<Sqlite3CommitCallback>(refPointer)
                callback(userData)
            }
        },
        arg2 = db.memory.refPointer(callback, userData)
    )
)

public actual fun sqlite3_compileoption_get(index: Int): String? =
    sqlite3_compileoption_get(index)?.toKStringFromUtf8()

public actual fun sqlite3_compileoption_used(optName: String): Int =
    sqlite3_compileoption_used(optName)

public actual fun sqlite3_complete(sql: String): Sqlite3CompleteResult =
    convertCompleteResult(sqlite3_complete(sql))

public actual fun sqlite3_config(option: Sqlite3ConfigOption): Sqlite3Result {
    val args: Array<Any?> = with(option) {
        if (this is Sqlite3ConfigOption.ROWID_IN_VIEW) {
            return convertResult(param.useMemScoped { pointer ->
                sqlite3_config(id, pointer)
            })
        }

        when (this) {
            Sqlite3ConfigOption.SERIALIZED,
            Sqlite3ConfigOption.MULTITHREAD,
            Sqlite3ConfigOption.SINGLETHREAD -> emptyArray()

            is Sqlite3ConfigOption.COVERING_INDEX_SCAN -> arrayOf(enabled)
            is Sqlite3ConfigOption.HEAP -> arrayOf(pMem?.block?.pointer, nBytes, min)

            is Sqlite3ConfigOption.LOG -> arrayOf(
                callback?.let {
                    staticCFunction { refPointer: COpaquePointer?,
                                      errCode: Int,
                                      errMsg: CPointer<ByteVar>? ->
                        val (callback, userData) = refData<Sqlite3LogCallback>(refPointer)
                        callback(userData, errCode, errMsg?.toKStringFromUtf8())
                    }
                },
                globalRefPointer("sqlite3_config_log", callback, userData)
            )

            is Sqlite3ConfigOption.LOOKASIDE -> arrayOf(sz, cnt)
            is Sqlite3ConfigOption.MEMDB_MAXSIZE -> arrayOf(maxSize)
            is Sqlite3ConfigOption.MEMSTATUS -> arrayOf(enabled)
            is Sqlite3ConfigOption.MMAP_SIZE -> arrayOf(sz, mx)
            is Sqlite3ConfigOption.PAGECACHE -> arrayOf(pMem?.block?.pointer, sz, n)
            is Sqlite3ConfigOption.PMASZ -> arrayOf(szPma)
            is Sqlite3ConfigOption.SMALL_MALLOC -> arrayOf(enabled)
            is Sqlite3ConfigOption.SORTERREF_SIZE -> arrayOf(nByte)

            is Sqlite3ConfigOption.SQLLOG -> arrayOf(
                callback?.let {
                    staticCFunction { ptr: COpaquePointer?,
                                      db: CPointer<s3>?,
                                      name: CPointer<ByteVar>?,
                                      type: Int ->
                        val (callback, userData) = refData<Sqlite3SqlLogCallback>(ptr)

                        dispatchSqlLogEvent(
                            callback = callback,
                            userData = userData,
                            type = type,
                            db = sqlite3(db!!),
                            name = name?.toKStringFromUtf8()
                        )
                    }
                },
                globalRefPointer("sqlite3_config_sqllog", callback, userData)
            )

            is Sqlite3ConfigOption.STMTJRNL_SPILL -> arrayOf(nByte)
            is Sqlite3ConfigOption.URI -> arrayOf(value)
            is Sqlite3ConfigOption.WIN32_HEAPSIZE -> arrayOf(nByte)
        }
    }

    return convertResult(sqlite3_config(option.id, *args))
}

public actual fun sqlite3_context_db_handle(context: sqlite3_context): sqlite3? =
    ksqlite.sqlite3_context_db_handle(context.pointer)?.let { sqlite3(it) }

public actual fun sqlite3_create_collation(
    db: sqlite3,
    name: String,
    encoding: Sqlite3TextEncoding.Set0,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3CollationCompareCallback?
): Sqlite3Result = convertResult(
    ksqlite.sqlite3_create_collation(
        arg0 = db.pointer,
        zName = name,
        eTextRep = encoding.utf8OrThrow().value,
        pArg = db.memory.refPointer(callback, userData),
        xCompare = callback?.let {
            staticCFunction { pointer, size1, text1, size2, text2 ->
                val (callback, userData) = refData<Sqlite3CollationCompareCallback>(pointer)
                val left = text1!!.toKString(size1)
                val right = text2!!.toKString(size2)
                callback(userData, left, right)
            }
        }
    )
)

public actual fun sqlite3_create_collation_v2(
    db: sqlite3,
    name: String,
    encoding: Sqlite3TextEncoding.Set0,
    userData: sqlite3_mutable_pointer?,
    destructor: Sqlite3DestructorCallback?,
    callback: Sqlite3CollationCompareCallback?
): Sqlite3Result = convertResult(
    ksqlite.sqlite3_create_collation_v2(
        arg0 = db.pointer,
        zName = name,
        eTextRep = encoding.utf8OrThrow().value,
        pArg = db.memory.refPointer(callback, userData),
        xCompare = callback?.let {
            staticCFunction { pointer, size1, text1, size2, text2 ->
                val (callback, userData) = refData<Sqlite3CollationCompareCallback>(pointer)
                val left = text1!!.toKString(size1)
                val right = text2!!.toKString(size2)
                callback(userData, left, right)
            }
        },
        xDestroy = refDisposer(callback, destructor)
    )
)

public actual fun sqlite3_create_function(
    db: sqlite3,
    name: String,
    nArg: Int,
    encoding: Sqlite3TextEncoding,
    userData: sqlite3_mutable_pointer?,
    func: Sqlite3CreateFunctionFuncCallback?,
    step: Sqlite3CreateFunctionStepCallback?,
    final: Sqlite3CreateFunctionFinalCallback?
): Sqlite3Result = convertResult(
    ksqlite.sqlite3_create_function(
        db = db.pointer,
        zFunctionName = name,
        nArg = nArg,
        eTextRep = encoding.utf8OrThrow().value,
        pApp = db.memory.refPointer(CreateFunctionCallbacks(func, step, final), userData),
        xFunc = func?.let {
            staticCFunction { context, a, b ->
                val refPointer = ksqlite.sqlite3_user_data(context)
                val (callbacks, userData) = refData<CreateFunctionCallbacks>(refPointer)
                callbacks.func!!.invoke()
            }
        },
        xStep = step?.let {
            staticCFunction { a, b, c ->

            }
        },
        xFinal = final?.let {
            staticCFunction { a ->

            }
        }
    )
)

public actual fun sqlite3_create_function_v2(
    db: sqlite3,
    name: String,
    nArg: Int,
    encoding: Sqlite3TextEncoding,
    userData: sqlite3_mutable_pointer?,
    func: Sqlite3CreateFunctionFuncCallback?,
    step: Sqlite3CreateFunctionStepCallback?,
    final: Sqlite3CreateFunctionFinalCallback?,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(
    ksqlite.
)

public actual fun sqlite3_create_module(
    db: sqlite3,
    name: String,
    module: sqlite3_module?
): Sqlite3Result = convertResult(
    ksqlite.
)

public actual fun sqlite3_create_module_v2(
    db: sqlite3,
    name: String,
    module: sqlite3_module?,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(
    ksqlite.
)

public actual fun sqlite3_create_window_function(
    db: sqlite3,
    name: String,
    nArg: Int,
    encoding: Sqlite3TextEncoding,
    userData: sqlite3_mutable_pointer?,
    step: Sqlite3CreateFunctionStepCallback?,
    final: Sqlite3CreateFunctionFinalCallback?,
    value: Sqlite3CreateFunctionValueCallback?,
    inverse: Sqlite3CreateFunctionInverseCallback?,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(
    ksqlite.
)

public actual fun sqlite3_data_count(stmt: sqlite3_stmt): Int

/*public actual fun sqlite3_exec(
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