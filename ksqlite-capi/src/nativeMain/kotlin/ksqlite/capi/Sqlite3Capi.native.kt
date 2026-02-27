@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.convert
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toCStringArray
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.SQLITE_OK
import ksqlite.SQLITE_TRANSIENT
import ksqlite.capi.handlers.AutoExtensionHandler
import ksqlite.capi.handlers.AutoExtensions
import ksqlite.capi.handlers.AutoVacuumPagesHandler
import ksqlite.capi.handlers.BusyHandlerHandler
import ksqlite.capi.handlers.CollationNeededHandler
import ksqlite.capi.handlers.CommitHookHandler
import ksqlite.capi.handlers.ConfigLogHandler
import ksqlite.capi.handlers.ConfigSqlLogHandler
import ksqlite.capi.handlers.CreateCollationHandler
import ksqlite.capi.handlers.CreateFunctionFinalHandler
import ksqlite.capi.handlers.CreateFunctionFuncHandler
import ksqlite.capi.handlers.CreateFunctionInverseHandler
import ksqlite.capi.handlers.CreateFunctionStepHandler
import ksqlite.capi.handlers.CreateFunctionValueHandler
import ksqlite.capi.handlers.ExecHandler
import ksqlite.capi.handlers.handle
import ksqlite.capi.handlers.uniqueFunctionHandlerName
import ksqlite.capi.memory.deallocateNullable
import ksqlite.capi.memory.globalDisposer
import ksqlite.capi.memory.globalMemory
import ksqlite.capi.memory.keyedRefPointer
import ksqlite.capi.memory.memory
import ksqlite.capi.memory.refDisposer
import ksqlite.capi.memory.useMemoryManager
import ksqlite.capi.memory.userDataDestructor
import ksqlite.capi.types.Sqlite3AutoExtensionCallback
import ksqlite.capi.types.Sqlite3AutoVacuumPagesCallback
import ksqlite.capi.types.Sqlite3BlobOpenFlag
import ksqlite.capi.types.Sqlite3BlobParam
import ksqlite.capi.types.Sqlite3BusyHandlerCallback
import ksqlite.capi.types.Sqlite3CollationNeededCallback
import ksqlite.capi.types.Sqlite3CommitHookCallback
import ksqlite.capi.types.Sqlite3CompleteResult
import ksqlite.capi.types.Sqlite3ConfigOption
import ksqlite.capi.types.Sqlite3CreateCollationCallback
import ksqlite.capi.types.Sqlite3CreateFunctionFinalCallback
import ksqlite.capi.types.Sqlite3CreateFunctionFuncCallback
import ksqlite.capi.types.Sqlite3CreateFunctionInverseCallback
import ksqlite.capi.types.Sqlite3CreateFunctionStepCallback
import ksqlite.capi.types.Sqlite3CreateFunctionValueCallback
import ksqlite.capi.types.Sqlite3DataType
import ksqlite.capi.types.Sqlite3DatabaseConnectionParam
import ksqlite.capi.types.Sqlite3DbConfigOption
import ksqlite.capi.types.Sqlite3DbStatusOption
import ksqlite.capi.types.Sqlite3DeserializeFlag
import ksqlite.capi.types.Sqlite3DestructorCallback
import ksqlite.capi.types.Sqlite3ExecCallback
import ksqlite.capi.types.Sqlite3FileControlOpcode
import ksqlite.capi.types.Sqlite3FileOpenFlag
import ksqlite.capi.types.Sqlite3IntParam
import ksqlite.capi.types.Sqlite3Limit
import ksqlite.capi.types.Sqlite3LongParam
import ksqlite.capi.types.Sqlite3PreUpdateHookCallback
import ksqlite.capi.types.Sqlite3PrepareFlag
import ksqlite.capi.types.Sqlite3ProgressHandlerCallback
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.Sqlite3StatementParam
import ksqlite.capi.types.Sqlite3StringUtf8Param
import ksqlite.capi.types.Sqlite3TextEncoding
import ksqlite.capi.types.Sqlite3ValueParam
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_backup
import ksqlite.capi.types.sqlite3_blob
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_filename
import ksqlite.capi.types.sqlite3_module
import ksqlite.capi.types.sqlite3_mutable_pointer
import ksqlite.capi.types.sqlite3_pointer
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.capi.types.sqlite3_value
import ksqlite.capi.types.useParam
import ksqlite.capi.types.useParamMemScoped
import ksqlite.capi.types.useParams
import ksqlite.capi.types.useParamsMemScoped
import ksqlite.ksqlite_auto_extension
import ksqlite.ksqlite_cancel_auto_extension
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
import ksqlite.sqlite3_collation_needed
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
import ksqlite.sqlite3_context_db_handle
import ksqlite.sqlite3_create_collation
import ksqlite.sqlite3_create_collation_v2
import ksqlite.sqlite3_create_function
import ksqlite.sqlite3_create_function_v2
import ksqlite.sqlite3_create_module
import ksqlite.sqlite3_create_module_v2
import ksqlite.sqlite3_create_window_function
import ksqlite.sqlite3_data_count
import ksqlite.sqlite3_db_cacheflush
import ksqlite.sqlite3_db_config
import ksqlite.sqlite3_db_filename
import ksqlite.sqlite3_db_handle
import ksqlite.sqlite3_db_name
import ksqlite.sqlite3_db_readonly
import ksqlite.sqlite3_db_release_memory
import ksqlite.sqlite3_db_status
import ksqlite.sqlite3_db_status64
import ksqlite.sqlite3_declare_vtab
import ksqlite.sqlite3_deserialize
import ksqlite.sqlite3_drop_modules
import ksqlite.sqlite3_errcode
import ksqlite.sqlite3_errmsg
import ksqlite.sqlite3_error_offset
import ksqlite.sqlite3_errstr
import ksqlite.sqlite3_exec
import ksqlite.sqlite3_expanded_sql
import ksqlite.sqlite3_extended_errcode
import ksqlite.sqlite3_extended_result_codes
import ksqlite.sqlite3_file_control
import ksqlite.sqlite3_finalize
import ksqlite.sqlite3_free
import ksqlite.sqlite3_get_autocommit
import ksqlite.sqlite3_get_auxdata
import ksqlite.sqlite3_hard_heap_limit64
import ksqlite.sqlite3_initialize
import ksqlite.sqlite3_interrupt
import ksqlite.sqlite3_is_interrupted
import ksqlite.sqlite3_keyword_check
import ksqlite.sqlite3_keyword_count
import ksqlite.sqlite3_keyword_name
import ksqlite.sqlite3_last_insert_rowid
import ksqlite.sqlite3_libversion
import ksqlite.sqlite3_libversion_number
import ksqlite.sqlite3_limit
import ksqlite.sqlite3_log
import ksqlite.sqlite3_malloc
import ksqlite.sqlite3_malloc64
import ksqlite.sqlite3_memory_highwater
import ksqlite.sqlite3_memory_used
import ksqlite.sqlite3_msize
import ksqlite.sqlite3_next_stmt
import ksqlite.sqlite3_open
import ksqlite.sqlite3_open_v2
import ksqlite.sqlite3_overload_function
import ksqlite.sqlite3_prepare_v2
import ksqlite.sqlite3_prepare_v3
import ksqlite.sqlite3_preupdate_blobwrite
import ksqlite.sqlite3_preupdate_count
import ksqlite.sqlite3_preupdate_depth
import ksqlite.sqlite3_preupdate_hook
import ksqlite.sqlite3_progress_handler

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
        arg1 = AutoVacuumPagesHandler.handle(callback),
        arg2 = db.memory.keyedRefPointer("autovacuum_pages", callback, userData, destructor),
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
): sqlite3_backup? = sqlite3_backup_init(
    pDest = destDb.pointer,
    zDestName = destDbName,
    pSource = srcDb.pointer,
    zSourceName = srcDbName
)?.let(::sqlite3_backup)

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
        encoding = encoding.utf8OrThrow().value.convert()
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
): Sqlite3Result = convertResult(memScoped {
    useParam(outBlob) { blob ->
        sqlite3_blob_open(
            arg0 = db.pointer,
            zDb = databaseName.cstr.ptr,
            zTable = tableName.cstr.ptr,
            zColumn = columnName.cstr.ptr,
            iRow = rowIndex,
            flags = flags.value,
            ppBlob = blob
        )
    }
})

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
        arg1 = BusyHandlerHandler.handle(callback),
        arg2 = db.memory.keyedRefPointer("busy_handler", callback, userData)
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
        val _ = ksqlite_cancel_auto_extension(AutoExtensionHandler)
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
    sqlite3_collation_needed(
        arg0 = db.pointer,
        arg1 = db.memory.keyedRefPointer("collation_needed", callback, userData),
        arg2 = CollationNeededHandler.handle(callback)
    )
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
    callback: Sqlite3CommitHookCallback?
): sqlite3_mutable_pointer? = sqlite3_mutable_pointer.fromDisposeRef(
    sqlite3_commit_hook(
        arg0 = db.pointer,
        arg1 = CommitHookHandler.handle(callback),
        arg2 = db.memory.keyedRefPointer("commit_hook", callback, userData)
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
            return convertResult(useParamMemScoped(param) { pointer ->
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
                ConfigLogHandler.handle(callback),
                globalMemory.keyedRefPointer("config_log", callback, userData)
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
                ConfigSqlLogHandler.handle(callback),
                globalMemory.keyedRefPointer("config_sqllog", callback, userData)
            )

            is Sqlite3ConfigOption.STMTJRNL_SPILL -> arrayOf(nByte)
            is Sqlite3ConfigOption.URI -> arrayOf(value)
            is Sqlite3ConfigOption.WIN32_HEAPSIZE -> arrayOf(nByte)
        }
    }

    return convertResult(sqlite3_config(option.id, *args))
}

public actual fun sqlite3_context_db_handle(context: sqlite3_context): sqlite3? =
    sqlite3_context_db_handle(context.pointer)?.let { sqlite3(it) }

public actual fun sqlite3_create_collation(
    db: sqlite3,
    name: String,
    encoding: Sqlite3TextEncoding.Set0,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3CreateCollationCallback?
): Sqlite3Result = convertResult(
    sqlite3_create_collation(
        arg0 = db.pointer,
        zName = name,
        eTextRep = encoding.utf8OrThrow().value,
        pArg = db.memory.keyedRefPointer("create_collation", callback, userData),
        xCompare = CreateCollationHandler.handle(callback)
    )
)

public actual fun sqlite3_create_collation_v2(
    db: sqlite3,
    name: String,
    encoding: Sqlite3TextEncoding.Set0,
    userData: sqlite3_mutable_pointer?,
    destructor: Sqlite3DestructorCallback?,
    callback: Sqlite3CreateCollationCallback?
): Sqlite3Result = convertResult(
    sqlite3_create_collation_v2(
        arg0 = db.pointer,
        zName = name,
        eTextRep = encoding.utf8OrThrow().value,
        pArg = db.memory.keyedRefPointer("create_collation", callback, userData),
        xCompare = CreateCollationHandler.handle(callback),
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
    sqlite3_create_function(
        db = db.pointer,
        zFunctionName = name,
        nArg = nArg,
        eTextRep = encoding.utf8OrThrow().value,
        pApp = db.memory.keyedRefPointer(
            key = uniqueFunctionHandlerName(name, nArg, encoding),
            data = CreateFunctionCallbacks(
                func = func,
                step = step,
                final = final
            ),
            userData = userData
        ),
        xFunc = CreateFunctionFuncHandler.handle(func),
        xStep = CreateFunctionStepHandler.handle(step),
        xFinal = CreateFunctionFinalHandler.handle(final)
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
    sqlite3_create_function_v2(
        db = db.pointer,
        zFunctionName = name,
        nArg = nArg,
        eTextRep = encoding.utf8OrThrow().value,
        pApp = db.memory.keyedRefPointer(
            key = uniqueFunctionHandlerName(name, nArg, encoding),
            data = CreateFunctionCallbacks(
                func = func,
                step = step,
                final = final
            ),
            userData = userData
        ),
        xFunc = CreateFunctionFuncHandler.handle(func),
        xStep = CreateFunctionStepHandler.handle(step),
        xFinal = CreateFunctionFinalHandler.handle(final),
        xDestroy = refDisposer(0, destructor)
    )
)

public actual fun sqlite3_create_module(
    db: sqlite3,
    name: String,
    module: sqlite3_module?,
    userData: sqlite3_mutable_pointer?
): Sqlite3Result = convertResult(
    sqlite3_create_module(
        db = db.pointer,
        zName = name,
        p = module?.pointer,
        pClientData = userData?.block?.pointer
    )
)

public actual fun sqlite3_create_module_v2(
    db: sqlite3,
    name: String,
    module: sqlite3_module?,
    userData: sqlite3_mutable_pointer?,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(
    sqlite3_create_module_v2(
        db = db.pointer,
        zName = name,
        p = module?.pointer,
        pClientData = userData?.block?.pointer,
        xDestroy = userDataDestructor(userData, destructor)
    )
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
    sqlite3_create_window_function(
        db = db.pointer,
        zFunctionName = name,
        nArg = nArg,
        eTextRep = encoding.utf8OrThrow().value,
        pApp = db.memory.keyedRefPointer(
            key = uniqueFunctionHandlerName(name, nArg, encoding),
            data = CreateFunctionCallbacks(
                step = step,
                final = final,
                value = value,
                inverse = inverse
            ),
            userData = userData
        ),
        xStep = CreateFunctionStepHandler.handle(step),
        xFinal = CreateFunctionFinalHandler.handle(final),
        xValue = CreateFunctionValueHandler.handle(value),
        xInverse = CreateFunctionInverseHandler.handle(inverse),
        xDestroy = refDisposer(0, destructor)
    )
)

public actual fun sqlite3_data_count(stmt: sqlite3_stmt): Int =
    sqlite3_data_count(stmt.pointer)


public actual fun sqlite3_db_cacheflush(db: sqlite3): Sqlite3Result =
    convertResult(sqlite3_db_cacheflush(db.pointer))

public actual fun sqlite3_db_config(
    db: sqlite3,
    option: Sqlite3DbConfigOption,
): Sqlite3Result {
    val args: Array<Any?> = with(option) {
        if (this is Sqlite3DbConfigOption.IntOutput && state != null) {
            return convertResult(useParamMemScoped(state) { pointer ->
                sqlite3_config(id, value, pointer)
            })
        }

        when (this) {
            is Sqlite3DbConfigOption.IntOutput -> arrayOf(value, null)
            is Sqlite3DbConfigOption.LOOKASIDE -> arrayOf(buf?.block?.pointer, sz, cnt)
            is Sqlite3DbConfigOption.MAINDBNAME -> arrayOf(name)
        }
    }

    return convertResult(sqlite3_db_config(db.pointer, option.id, *args))
}

public actual fun sqlite3_db_filename(
    db: sqlite3,
    name: String
): sqlite3_filename? = sqlite3_db_filename(
    db = db.pointer,
    zDbName = name
)?.toKStringFromUtf8()

public actual fun sqlite3_db_handle(stmt: sqlite3_stmt): sqlite3? =
    sqlite3_db_handle(stmt.pointer)?.let(::sqlite3)

public actual fun sqlite3_db_name(
    db: sqlite3,
    index: Int
): String? = sqlite3_db_name(
    db = db.pointer,
    N = index
)?.toKStringFromUtf8()

public actual fun sqlite3_db_readonly(
    db: sqlite3,
    name: String
): Int = sqlite3_db_readonly(
    db = db.pointer,
    zDbName = name
)

public actual fun sqlite3_db_release_memory(db: sqlite3): Sqlite3Result =
    convertResult(sqlite3_db_release_memory(db.pointer))

public actual fun sqlite3_db_status(
    db: sqlite3,
    option: Sqlite3DbStatusOption,
    current: Sqlite3IntParam?,
    highwtr: Sqlite3IntParam?,
    resetFlag: Int
): Sqlite3Result = convertResult(useParamsMemScoped(current, highwtr) { curPtr, highPtr ->
    sqlite3_db_status(
        arg0 = db.pointer,
        op = option.id,
        pCur = curPtr,
        pHiwtr = highPtr,
        resetFlg = resetFlag
    )
})

public actual fun sqlite3_db_status64(
    db: sqlite3,
    option: Sqlite3DbStatusOption,
    current: Sqlite3LongParam?,
    highwtr: Sqlite3LongParam?,
    resetFlag: Int
): Sqlite3Result = convertResult(useParamsMemScoped(current, highwtr) { curPtr, highPtr ->
    sqlite3_db_status64(
        arg0 = db.pointer,
        arg1 = option.id,
        arg2 = curPtr,
        arg3 = highPtr,
        arg4 = resetFlag
    )
})

public actual fun sqlite3_declare_vtab(
    db: sqlite3,
    sql: String
): Sqlite3Result = convertResult(
    sqlite3_declare_vtab(
        arg0 = db.pointer,
        zSQL = sql
    )
)

public actual fun sqlite3_deserialize(
    db: sqlite3,
    schema: String?,
    data: sqlite3_mutable_pointer?,
    dbSize: Long,
    dataSize: Long,
    flags: Sqlite3DeserializeFlag?
): Sqlite3Result = convertResult(
    sqlite3_deserialize(
        db = db.pointer,
        zSchema = schema,
        pData = data?.block?.pointer?.reinterpret(),
        szDb = dbSize,
        szBuf = dataSize,
        mFlags = flags?.value?.convert() ?: 0U
    )
)

public actual fun sqlite3_drop_modules(
    db: sqlite3,
    keep: Array<String>?
): Sqlite3Result = convertResult(memScoped {
    sqlite3_drop_modules(
        db = db.pointer,
        azKeep = keep?.toCStringArray(this)
    )
})

public actual fun sqlite3_errcode(db: sqlite3): Int =
    sqlite3_errcode(db.pointer)

public actual fun sqlite3_errmsg(db: sqlite3): String? =
    sqlite3_errmsg(db.pointer)?.toKStringFromUtf8()

public actual fun sqlite3_errstr(resultCode: Int): String? =
    sqlite3_errstr(resultCode)?.toKStringFromUtf8()

public actual fun sqlite3_error_offset(db: sqlite3): Int =
    sqlite3_error_offset(db.pointer)

public actual fun sqlite3_exec(
    db: sqlite3,
    sql: String,
    errorMessage: Sqlite3StringUtf8Param?,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3ExecCallback?
): Sqlite3Result = convertResult(useMemoryManager {
    placement.useParam(errorMessage) { errorMessagePointer ->
        sqlite3_exec(
            arg0 = db.pointer,
            sql = stringPointer(sql),
            callback = ExecHandler.handle(callback),
            arg3 = refPointer(callback, userData),
            errmsg = errorMessagePointer
        )
    }
})

public actual fun sqlite3_expanded_sql(stmt: sqlite3_stmt): String? {
    val pointer = sqlite3_expanded_sql(stmt.pointer) ?: return null
    val expandedSql = pointer.toKStringFromUtf8()
    sqlite3_free(arg0 = pointer)
    return expandedSql
}

public actual fun sqlite3_extended_errcode(db: sqlite3): Int =
    sqlite3_extended_errcode(db.pointer)

public actual fun sqlite3_extended_result_codes(
    db: sqlite3,
    enabled: Int
): Sqlite3Result = convertResult(
    sqlite3_extended_result_codes(
        arg0 = db.pointer,
        onoff = enabled
    )
)

public actual fun sqlite3_file_control(
    db: sqlite3,
    name: String?,
    opcode: Sqlite3FileControlOpcode,
    userData: sqlite3_mutable_pointer?
): Sqlite3Result = convertResult(
    sqlite3_file_control(
        arg0 = db.pointer,
        zDbName = name,
        op = opcode.code,
        arg3 = userData?.block?.pointer
    )
)

public actual fun sqlite3_finalize(stmt: sqlite3_stmt): Sqlite3Result =
    convertResult(sqlite3_finalize(stmt.pointer))

public actual fun sqlite3_free(data: sqlite3_mutable_pointer?): Unit =
    sqlite3_free(data?.block?.pointer)

public actual fun sqlite3_get_autocommit(db: sqlite3): Int =
    sqlite3_get_autocommit(db.pointer)

public actual fun sqlite3_get_auxdata(
    context: sqlite3_context,
    index: Int
): sqlite3_mutable_pointer? = sqlite3_get_auxdata(
    arg0 = context.pointer,
    N = index
)?.asStableRef<sqlite3_mutable_pointer>()?.get()

public actual fun sqlite3_hard_heap_limit64(limit: Long): Long =
    sqlite3_hard_heap_limit64(limit)

public actual fun sqlite3_initialize(): Sqlite3Result =
    convertResult(sqlite3_initialize())

public actual fun sqlite3_interrupt(db: sqlite3): Unit =
    sqlite3_interrupt(db.pointer)

public actual fun sqlite3_is_interrupted(db: sqlite3): Int =
    sqlite3_is_interrupted(db.pointer)

public actual fun sqlite3_keyword_count(): Int =
    sqlite3_keyword_count()

public actual fun sqlite3_keyword_name(
    index: Int,
    name: Sqlite3StringUtf8Param,
): Sqlite3Result = convertResult(memScoped {
    useParam(name) { namePointer ->
        val size = Sqlite3IntParam(0)

        useParam(size) { sizePointer ->
            sqlite3_keyword_name(
                arg0 = index,
                arg1 = namePointer,
                arg2 = sizePointer
            )
        }.also {
            name.size = size.value
        }
    }
})

public actual fun sqlite3_keyword_check(
    word: String,
    size: Int?
): Int = memScoped {
    val cword = word.cstr

    sqlite3_keyword_check(
        arg0 = cword,
        arg1 = size ?: cword.size
    )
}

public actual fun sqlite3_last_insert_rowid(db: sqlite3): Long =
    sqlite3_last_insert_rowid(db.pointer)

public actual fun sqlite3_libversion(): String =
    sqlite3_libversion()!!.toKStringFromUtf8()

public actual fun sqlite3_libversion_number(db: sqlite3): Int =
    sqlite3_libversion_number()

public actual fun sqlite3_limit(
    db: sqlite3,
    id: Sqlite3Limit,
    newVal: Int
): Int = sqlite3_limit(
    arg0 = db.pointer,
    id = id.id,
    newVal = newVal
)

public actual fun sqlite3_log(
    errCode: Int,
    message: String
): Unit = sqlite3_log(
    iErrCode = errCode,
    zFormat = message
)

public actual fun sqlite3_malloc(size: Int): sqlite3_mutable_pointer? =
    sqlite3_malloc(size)?.let { sqlite3_mutable_pointer.from(it, size.toLong()) }

public actual fun sqlite3_malloc64(size: Long): sqlite3_mutable_pointer? =
    sqlite3_malloc64(size.toULong())?.let { sqlite3_mutable_pointer.from(it, size) }

public actual fun sqlite3_memory_used(): Long =
    sqlite3_memory_used()

public actual fun sqlite3_memory_highwater(resetFlag: Int): Long =
    sqlite3_memory_highwater(resetFlag)

public actual fun sqlite3_msize(data: sqlite3_mutable_pointer?): ULong =
    sqlite3_msize(data?.block?.pointer)

public actual fun sqlite3_next_stmt(
    db: sqlite3,
    stmt: sqlite3_stmt?
): sqlite3_stmt? = sqlite3_next_stmt(
    pDb = db.pointer,
    pStmt = stmt?.pointer
)?.let(::sqlite3_stmt)

public actual fun sqlite3_open(
    fileName: String,
    outDb: Sqlite3DatabaseConnectionParam
): Sqlite3Result = convertResult(memScoped {
    useParam(outDb) { dbPointer ->
        sqlite3_open(
            filename = fileName.cstr.ptr,
            ppDb = dbPointer
        )
    }
})

public actual fun sqlite3_open_v2(
    fileName: String,
    outDb: Sqlite3DatabaseConnectionParam,
    flags: Sqlite3FileOpenFlag.Valid,
    vfs: String?
): Sqlite3Result = convertResult(memScoped {
    useParam(outDb) { dbPointer ->
        sqlite3_open_v2(
            filename = fileName.cstr.ptr,
            ppDb = dbPointer,
            flags = flags.value,
            zVfs = vfs?.cstr?.ptr
        )
    }
})

public actual fun sqlite3_overload_function(
    db: sqlite3,
    name: String,
    nArg: Int
): Sqlite3Result = convertResult(
    sqlite3_overload_function(
        arg0 = db.pointer,
        zFuncName = name,
        nArg = nArg
    )
)

public actual fun sqlite3_prepare_v2(
    db: sqlite3,
    sql: String,
    size: Int?,
    outStmt: Sqlite3StatementParam,
    outTail: Sqlite3StringUtf8Param?
): Sqlite3Result = convertResult(memScoped {
    useParams(outStmt, outTail) { stmtPointer, tailPinter ->
        val csql = sql.cstr

        sqlite3_prepare_v2(
            db = db.pointer,
            zSql = csql.ptr,
            nByte = size ?: csql.size,
            ppStmt = stmtPointer,
            pzTail = tailPinter
        )
    }
})

public actual fun sqlite3_prepare_v3(
    db: sqlite3,
    sql: String,
    size: Int?,
    flags: Sqlite3PrepareFlag?,
    outStmt: Sqlite3StatementParam,
    outTail: Sqlite3StringUtf8Param?
): Sqlite3Result = convertResult(memScoped {
    useParams(outStmt, outTail) { stmtPointer, tailPinter ->
        val csql = sql.cstr

        sqlite3_prepare_v3(
            db = db.pointer,
            zSql = csql.ptr,
            nByte = size ?: csql.size,
            prepFlags = flags?.value?.convert() ?: 0U,
            ppStmt = stmtPointer,
            pzTail = tailPinter
        )
    }
})

public actual fun sqlite3_preupdate_blobwrite(db: sqlite3): Int =
    sqlite3_preupdate_blobwrite(db.pointer)

public actual fun sqlite3_preupdate_count(db: sqlite3): Int =
    sqlite3_preupdate_count(db.pointer)

public actual fun sqlite3_preupdate_depth(db: sqlite3): Int =
    sqlite3_preupdate_depth(db.pointer)

public actual fun sqlite3_preupdate_hook(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3PreUpdateHookCallback
): sqlite3_mutable_pointer? = sqlite3_preupdate_hook(
    db = db.pointer,
    xPreUpdate = TODO()
)

public actual fun sqlite3_preupdate_new(
    db: sqlite3,
    index: Int,
    outValue: Sqlite3ValueParam
): Sqlite3Result

public actual fun sqlite3_preupdate_old(
    db: sqlite3,
    index: Int,
    outValue: Sqlite3ValueParam
): Sqlite3Result

public actual fun sqlite3_progress_handler(
    db: sqlite3,
    nOps: Int,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3ProgressHandlerCallback
) = sqlite3_progress_handler(
    arg0 = db.pointer,
    arg1 = nOps,
    arg2 = TODO(),
    arg3 = db.memory.keyedRefPointer("progress_handler", callback, userData)
)

public actual fun sqlite3_randomness(
    size: Int,
    data: sqlite3_pointer?
)

public actual fun sqlite3_realloc(
    data: sqlite3_mutable_pointer?,
    size: Int
): sqlite3_mutable_pointer?

public actual fun sqlite3_realloc64(
    data: sqlite3_mutable_pointer?,
    size: Long
): sqlite3_mutable_pointer?