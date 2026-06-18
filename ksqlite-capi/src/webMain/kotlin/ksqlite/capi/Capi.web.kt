@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi

import ksqlite.capi.callbacks.SqliteAuthorizerCallback
import ksqlite.capi.callbacks.SqliteAutoExtensionCallback
import ksqlite.capi.callbacks.SqliteAutovacuumPagesCallback
import ksqlite.capi.callbacks.SqliteBusyHandlerCallback
import ksqlite.capi.callbacks.SqliteCollationCallback
import ksqlite.capi.callbacks.SqliteCollationNeededCallback
import ksqlite.capi.callbacks.SqliteCommitHookCallback
import ksqlite.capi.callbacks.SqliteDestroyCallback
import ksqlite.capi.callbacks.SqliteExecCallback
import ksqlite.capi.callbacks.SqliteFunctionFinalCallback
import ksqlite.capi.callbacks.SqliteFunctionFuncCallback
import ksqlite.capi.callbacks.SqliteFunctionInverseCallback
import ksqlite.capi.callbacks.SqliteFunctionStepCallback
import ksqlite.capi.callbacks.SqliteFunctionValueCallback
import ksqlite.capi.callbacks.SqlitePreupdateHookCallback
import ksqlite.capi.callbacks.SqliteProgressHandlerCallback
import ksqlite.capi.callbacks.SqliteRollbackHookCallback
import ksqlite.capi.callbacks.SqliteTraceCallback
import ksqlite.capi.callbacks.SqliteUpdateHookCallback
import ksqlite.capi.callbacks.SqliteWalHookCallback
import ksqlite.capi.handlers.AuthorizerHandler
import ksqlite.capi.handlers.AutovacuumPagesHandler
import ksqlite.capi.handlers.BusyHandlerHandler
import ksqlite.capi.handlers.CollationCompareHandler
import ksqlite.capi.handlers.CollationNeededHandler
import ksqlite.capi.handlers.CommitHookHandler
import ksqlite.capi.handlers.ConfigLogHandler
import ksqlite.capi.handlers.ConfigSqlLogHandler
import ksqlite.capi.handlers.ExecHandler
import ksqlite.capi.handlers.FunctionFinalHandler
import ksqlite.capi.handlers.FunctionFuncHandler
import ksqlite.capi.handlers.FunctionInverseHandler
import ksqlite.capi.handlers.FunctionStepHandler
import ksqlite.capi.handlers.FunctionValueHandler
import ksqlite.capi.handlers.PreupdateHookHandler
import ksqlite.capi.handlers.ProgressHandlerHandler
import ksqlite.capi.handlers.RollbackHookHandler
import ksqlite.capi.handlers.TraceHandler
import ksqlite.capi.handlers.UpdateHookHandler
import ksqlite.capi.handlers.WalHookHandler
import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.NullPtr
import ksqlite.capi.memory.VariadicValue
import ksqlite.capi.memory.allocateUtf8
import ksqlite.capi.memory.allocateUtf8Array
import ksqlite.capi.memory.allocateUtf8Pointer
import ksqlite.capi.memory.bufferDisposer
import ksqlite.capi.memory.bufferScoped
import ksqlite.capi.memory.globalDisposer
import ksqlite.capi.memory.globalMemory
import ksqlite.capi.memory.heapScoped
import ksqlite.capi.memory.invokeVariadic
import ksqlite.capi.memory.memory
import ksqlite.capi.memory.notNull
import ksqlite.capi.memory.orNull
import ksqlite.capi.memory.readByteArray
import ksqlite.capi.memory.stableRefDisposer
import ksqlite.capi.memory.stackScoped
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.memory.useMemoryManager
import ksqlite.capi.memory.withMemoryManager
import ksqlite.capi.types.Int32OutputParam
import ksqlite.capi.types.Int64OutputParam
import ksqlite.capi.types.SqliteBlobOutputParam
import ksqlite.capi.types.SqliteConfigOption
import ksqlite.capi.types.SqliteDbConfigOption
import ksqlite.capi.types.SqliteOutputParam
import ksqlite.capi.types.SqliteSnapshotOutputParam
import ksqlite.capi.types.SqliteStmtOutputParam
import ksqlite.capi.types.SqliteVTabConfigOption
import ksqlite.capi.types.SqliteValueOutputParam
import ksqlite.capi.types.Utf8OutputParam
import ksqlite.capi.types.overriding
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_backup
import ksqlite.capi.types.sqlite3_blob
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_filename
import ksqlite.capi.types.sqlite3_snapshot
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.capi.types.sqlite3_value
import ksqlite.capi.types.sqlite3_vfs
import ksqlite.capi.types.useParam
import ksqlite.capi.types.useParamStackScoped
import ksqlite.capi.types.useParams
import ksqlite.capi.types.useParamsStackScoped
import ksqlite.capi.vtab.createVTabModule
import ksqlite.capi.vtab.sqlite3_index_info
import ksqlite.capi.vtab.sqlite3_module
import ksqlite.foreign.js.copyTo
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.size
import ksqlite.types.SqliteBlobOpenFlag
import ksqlite.types.SqliteCheckpointMode
import ksqlite.types.SqliteCompleteResult
import ksqlite.types.SqliteConflictResolutionMode
import ksqlite.types.SqliteDataType
import ksqlite.types.SqliteDbReadonlyResult
import ksqlite.types.SqliteDbStatusOption
import ksqlite.types.SqliteDeserializeFlag
import ksqlite.types.SqliteExplainMode
import ksqlite.types.SqliteFileControlOpcode
import ksqlite.types.SqliteFunctionTextEncoding
import ksqlite.types.SqliteOpenFlag
import ksqlite.types.SqlitePrepareFlag
import ksqlite.types.SqliteResultCode
import ksqlite.types.SqliteRuntimeLimit
import ksqlite.types.SqliteStatementStatusCounter
import ksqlite.types.SqliteStatusOption
import ksqlite.types.SqliteTextEncoding
import ksqlite.types.SqliteTraceCode
import ksqlite.types.SqliteTransactionState
import ksqlite.types.internal.convertCompleteResult
import ksqlite.types.internal.convertConflictResolutionMode
import ksqlite.types.internal.convertDataType
import ksqlite.types.internal.convertDbReadonlyResult
import ksqlite.types.internal.convertExplainMode
import ksqlite.types.internal.convertResult
import ksqlite.types.internal.convertTextEncoding
import ksqlite.types.internal.convertTransactionState
import kotlin.js.toJsBigInt
import kotlin.js.toLong

public actual fun sqlite3_auto_extension(callback: SqliteAutoExtensionCallback): SqliteResultCode =
    autoExtensionRegister(callback) { exports.ksqlite_auto_extension(SharedAutoExtensionHandler) }

public actual fun <AppData> sqlite3_autovacuum_pages(
    db: sqlite3,
    appData: AppData,
    destroy: SqliteDestroyCallback<in AppData>?,
    callback: SqliteAutovacuumPagesCallback<in AppData>?
): SqliteResultCode = convertResult(db.withMemoryManager {
    exports.sqlite3_autovacuum_pages(
        db.pointer,
        functionPointer(callback, ::AutovacuumPagesHandler),
        keyedStableRefPointer(KEY_AUTOVACUUM_PAGES, callback, appData, destroy),
        stableRefDisposer(callback, destroy)
    )
})

public actual fun sqlite3_backup_finish(backup: sqlite3_backup): SqliteResultCode = convertResult(
    exports.sqlite3_backup_finish(backup.pointer)
)

public actual fun sqlite3_backup_init(
    destDb: sqlite3,
    destDbName: String,
    srcDb: sqlite3,
    srcDbName: String
): sqlite3_backup? = heapScoped {
    exports.sqlite3_backup_init(
        destDb.pointer,
        destDbName.allocateUtf8Pointer(),
        srcDb.pointer,
        srcDbName.allocateUtf8Pointer()
    ).orNull?.let(::sqlite3_backup)
}

public actual fun sqlite3_backup_pagecount(backup: sqlite3_backup): Int =
    exports.sqlite3_backup_pagecount(backup.pointer)

public actual fun sqlite3_backup_remaining(backup: sqlite3_backup): Int =
    exports.sqlite3_backup_remaining(backup.pointer)

public actual fun sqlite3_backup_step(
    backup: sqlite3_backup,
    nPage: Int
): SqliteResultCode = convertResult(exports.sqlite3_backup_step(backup.pointer, nPage))

public actual fun sqlite3_bind_blob(
    stmt: sqlite3_stmt,
    index: Int,
    bytes: ByteArray,
    size: Int,
    destroy: SqliteDestroyCallback<ByteArray>?
): SqliteResultCode = convertResult(
    exports.sqlite3_bind_blob(
        stmt.pointer,
        index,
        stmt.memory.byteArrayPointer(bytes, destroy),
        size,
        globalDisposer(bytes)
    )
)

public actual fun sqlite3_bind_blob64(
    stmt: sqlite3_stmt,
    index: Int,
    buffer: Buffer,
    size: Long,
    destroy: SqliteDestroyCallback<Buffer>?
): SqliteResultCode = convertResult(
    exports.sqlite3_bind_blob64(
        stmt.pointer,
        index,
        buffer.pointer,
        size.toJsBigInt(),
        bufferDisposer(buffer, destroy)
    )
)

public actual fun sqlite3_bind_double(
    stmt: sqlite3_stmt,
    index: Int,
    value: Double
): SqliteResultCode = convertResult(exports.sqlite3_bind_double(stmt.pointer, index, value))

public actual fun sqlite3_bind_int(
    stmt: sqlite3_stmt,
    index: Int,
    value: Int
): SqliteResultCode = convertResult(exports.sqlite3_bind_int(stmt.pointer, index, value))

public actual fun sqlite3_bind_int64(
    stmt: sqlite3_stmt,
    index: Int,
    value: Long
): SqliteResultCode =
    convertResult(exports.sqlite3_bind_int64(stmt.pointer, index, value.toJsBigInt()))

public actual fun sqlite3_bind_null(
    stmt: sqlite3_stmt,
    index: Int
): SqliteResultCode = convertResult(exports.sqlite3_bind_null(stmt.pointer, index))

public actual fun sqlite3_bind_parameter_count(stmt: sqlite3_stmt): Int =
    exports.sqlite3_bind_parameter_count(stmt.pointer)

public actual fun sqlite3_bind_parameter_index(
    stmt: sqlite3_stmt,
    name: String
): Int = heapScoped {
    exports.sqlite3_bind_parameter_index(stmt.pointer, name.allocateUtf8Pointer())
}

public actual fun sqlite3_bind_parameter_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = exports.sqlite3_bind_parameter_name(stmt.pointer, index)
    .toKStringFromUtf8OrNull()

public actual fun <Data> sqlite3_bind_pointer(
    stmt: sqlite3_stmt,
    index: Int,
    data: Data,
    type: String?,
    destroy: SqliteDestroyCallback<Data>?
): SqliteResultCode = convertResult(with(globalMemory) {
    // Use globalMemory because of lack of information within sqlite3_value_pointer()
    allocateNamedPointer(type, destroy) { ptr, ptrDestroy ->
        exports.sqlite3_bind_pointer(
            stmt.pointer,
            index,
            stableRefPointer(ptr, data, ptrDestroy),
            ptr.name,
            stableRefDisposer(ptr, ptrDestroy)
        )
    }
})

public actual fun sqlite3_bind_text(
    stmt: sqlite3_stmt,
    index: Int,
    value: String
): SqliteResultCode = convertResult(heapScoped {
    val cText = value.allocateUtf8()
    exports.sqlite3_bind_text(stmt.pointer, index, cText.pointer, cText.size, SqliteTransient)
})

public actual fun sqlite3_bind_text64(
    stmt: sqlite3_stmt,
    index: Int,
    buffer: Buffer,
    size: Long,
    encoding: SqliteTextEncoding.BindText,
    destroy: SqliteDestroyCallback<Buffer>?
): SqliteResultCode = convertResult(
    exports.sqlite3_bind_text64(
        stmt.pointer,
        index,
        buffer.pointer,
        size.toJsBigInt(),
        bufferDisposer(buffer, destroy),
        encoding.value
    )
)

public actual fun sqlite3_bind_value(
    stmt: sqlite3_stmt,
    index: Int,
    value: sqlite3_value
): SqliteResultCode = convertResult(exports.sqlite3_bind_value(stmt.pointer, index, value.pointer))

public actual fun sqlite3_bind_zeroblob(
    stmt: sqlite3_stmt,
    index: Int,
    size: Int
): SqliteResultCode = convertResult(exports.sqlite3_bind_zeroblob(stmt.pointer, index, size))

public actual fun sqlite3_bind_zeroblob64(
    stmt: sqlite3_stmt,
    index: Int,
    size: ULong
): SqliteResultCode =
    convertResult(exports.sqlite3_bind_zeroblob64(stmt.pointer, index, size.toLong().toJsBigInt()))

public actual fun sqlite3_blob_bytes(blob: sqlite3_blob): Int =
    exports.sqlite3_blob_bytes(blob.pointer)

public actual fun sqlite3_blob_close(blob: sqlite3_blob): SqliteResultCode =
    convertResult(exports.sqlite3_blob_close(blob.pointer))

public actual fun sqlite3_blob_open(
    db: sqlite3,
    database: String,
    tableName: String,
    columnName: String,
    rowid: Long,
    flags: SqliteBlobOpenFlag,
    outBlob: SqliteBlobOutputParam
): SqliteResultCode = convertResult(heapScoped {
    useParam(outBlob) { blobPtr ->
        exports.sqlite3_blob_open(
            db.pointer,
            database.allocateUtf8Pointer(),
            tableName.allocateUtf8Pointer(),
            columnName.allocateUtf8Pointer(),
            rowid.toJsBigInt(),
            flags.value,
            blobPtr
        )
    }
})

public actual fun sqlite3_blob_read(
    blob: sqlite3_blob,
    output: ByteArray,
    size: Int,
    offset: Int
): SqliteResultCode {
    val memory = wasm
    val pointer = memory.alloc(size)
    val result = convertResult(exports.sqlite3_blob_read(blob.pointer, pointer, size, offset))

    try {
        if (result == SqliteResultCode.OK) {
            val start = pointer.toLong().toInt()
            val end = start + size

            memory.heap8()
                .subarray(start, end)
                .copyTo(output, 0)
        }
    } finally {
        memory.dealloc(pointer)
    }

    return result
}

public actual fun sqlite3_blob_reopen(
    blob: sqlite3_blob,
    rowid: Long
): SqliteResultCode = convertResult(exports.sqlite3_blob_reopen(blob.pointer, rowid.toJsBigInt()))

public actual fun sqlite3_blob_write(
    blob: sqlite3_blob,
    input: ByteArray,
    size: Int,
    offset: Int
): SqliteResultCode = convertResult(bufferScoped(input) { bufferPtr ->
    exports.sqlite3_blob_write(blob.pointer, bufferPtr, size, offset)
})

public actual fun <AppData> sqlite3_busy_handler(
    db: sqlite3,
    appData: AppData,
    callback: SqliteBusyHandlerCallback<AppData>?
): SqliteResultCode = convertResult(db.withMemoryManager {
    exports.sqlite3_busy_handler(
        db.pointer,
        functionPointer(callback, ::BusyHandlerHandler),
        keyedStableRefPointer(KEY_BUSY_HANDLER, callback, appData)
    )
})

public actual fun sqlite3_busy_timeout(
    db: sqlite3,
    millis: Int
): SqliteResultCode = convertResult(exports.sqlite3_busy_timeout(db.pointer, millis))

public actual fun sqlite3_cancel_auto_extension(callback: SqliteAutoExtensionCallback): Int =
    autoExtensionUnregister(callback) {
        exports.ksqlite_cancel_auto_extension(SharedAutoExtensionHandler)
    }

public actual fun sqlite3_changes(db: sqlite3): Int =
    exports.sqlite3_changes(db.pointer)

public actual fun sqlite3_changes64(db: sqlite3): Long =
    exports.sqlite3_changes64(db.pointer).toLong()

public actual fun sqlite3_clear_bindings(stmt: sqlite3_stmt): SqliteResultCode =
    commonClearBindings(stmt, exports.sqlite3_clear_bindings(stmt.pointer))

public actual fun sqlite3_close(db: sqlite3): SqliteResultCode =
    db.deallocate { exports.sqlite3_close(it.pointer) }

public actual fun sqlite3_close_v2(db: sqlite3): SqliteResultCode =
    db.deallocate { exports.sqlite3_close_v2(it.pointer) }

public actual fun <AppData> sqlite3_collation_needed(
    db: sqlite3,
    appData: AppData,
    callback: SqliteCollationNeededCallback<AppData>?,
): SqliteResultCode = convertResult(db.withMemoryManager {
    exports.sqlite3_collation_needed(
        db.pointer,
        keyedStableRefPointer(KEY_COLLATION_NEEDED, callback, appData),
        functionPointer(callback, ::CollationNeededHandler)
    )
})

public actual fun sqlite3_column_blob(
    stmt: sqlite3_stmt,
    index: Int
): ByteArray? = commonColumnByteArray(
    stmt = stmt,
    index = index,
    pointer = exports.sqlite3_column_blob(stmt.pointer, index),
    toByteArray = WasmPointer::readByteArray
)

public actual fun sqlite3_column_bytes(
    stmt: sqlite3_stmt,
    index: Int
): Int = exports.sqlite3_column_bytes(stmt.pointer, index)

public actual fun sqlite3_column_count(stmt: sqlite3_stmt): Int =
    exports.sqlite3_column_count(stmt.pointer)

public actual fun sqlite3_column_database_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = exports.sqlite3_column_database_name(stmt.pointer, index)
    .toKStringFromUtf8OrNull()

public actual fun sqlite3_column_decltype(
    stmt: sqlite3_stmt,
    index: Int
): String? = exports.sqlite3_column_decltype(stmt.pointer, index)
    .toKStringFromUtf8OrNull()

public actual fun sqlite3_column_double(
    stmt: sqlite3_stmt,
    index: Int
): Double = exports.sqlite3_column_double(stmt.pointer, index)

public actual fun sqlite3_column_int(
    stmt: sqlite3_stmt,
    index: Int
): Int = exports.sqlite3_column_int(stmt.pointer, index)

public actual fun sqlite3_column_int64(
    stmt: sqlite3_stmt,
    index: Int
): Long = exports.sqlite3_column_int64(stmt.pointer, index).toLong()

public actual fun sqlite3_column_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = exports.sqlite3_column_name(stmt.pointer, index)
    .toKStringFromUtf8OrNull()

public actual fun sqlite3_column_origin_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = exports.sqlite3_column_origin_name(stmt.pointer, index)
    .toKStringFromUtf8OrNull()

public actual fun sqlite3_column_table_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = exports.sqlite3_column_table_name(stmt.pointer, index)
    .toKStringFromUtf8OrNull()

public actual fun sqlite3_column_text(
    stmt: sqlite3_stmt,
    index: Int
): String? = exports.sqlite3_column_text(stmt.pointer, index)
    .toKStringFromUtf8OrNull()

public actual fun sqlite3_column_type(
    stmt: sqlite3_stmt,
    index: Int
): SqliteDataType = convertDataType(exports.sqlite3_column_type(stmt.pointer, index))

public actual fun sqlite3_column_value(
    stmt: sqlite3_stmt,
    index: Int
): sqlite3_value? = exports.sqlite3_column_value(stmt.pointer, index)
    .orNull?.let(::sqlite3_value)

public actual fun <AppData> sqlite3_commit_hook(
    db: sqlite3,
    appData: AppData,
    callback: SqliteCommitHookCallback<AppData>?
): Unit = db.withMemoryManager {
    val _ = exports.sqlite3_commit_hook(
        db.pointer,
        functionPointer(callback, ::CommitHookHandler),
        keyedStableRefPointer(KEY_COMMIT_HOOK, callback, appData)
    )
}

public actual fun sqlite3_compileoption_get(index: Int): String? =
    exports.sqlite3_compileoption_get(index).toKStringFromUtf8OrNull()

public actual fun sqlite3_compileoption_used(optName: String): Int = heapScoped {
    exports.sqlite3_compileoption_used(optName.allocateUtf8Pointer())
}

public actual fun sqlite3_complete(sql: String): SqliteCompleteResult =
    convertCompleteResult(heapScoped { exports.sqlite3_complete(sql.allocateUtf8Pointer()) })

public actual fun sqlite3_config(option: SqliteConfigOption): SqliteResultCode = commonConfig(
    option = option,
    logFunctionPointer = { cb, _ -> globalMemory.functionPointer(cb, ::ConfigLogHandler) },
    sqllogFunctionPointer = { cb, _ -> globalMemory.functionPointer(cb, ::ConfigSqlLogHandler) },
    bufferPointer = Buffer::pointer,
    keyedStableRefPointer = globalMemory::keyedStableRefPointer,
    outputParamConfig = {
        heapScoped {
            useParam(state) { statePtr ->
                invokeVariadic(::globalMemory, VariadicValue.OfPointer(statePtr)) { vaListPtr ->
                    exports.sqlite3_config(id, vaListPtr)
                }
            }
        }
    },
    nativeConfig = { id, values ->
        invokeVariadic(values, ::globalMemory) { vaListPtr ->
            exports.sqlite3_config(id, vaListPtr)
        }
    }
)

public actual fun sqlite3_context_db_handle(context: sqlite3_context): sqlite3 =
    sqlite3(exports.sqlite3_context_db_handle(context.pointer))

public actual fun <AppData> sqlite3_create_collation_v2(
    db: sqlite3,
    name: String,
    encoding: SqliteTextEncoding.CreateCollation,
    appData: AppData,
    destroy: SqliteDestroyCallback<in AppData>?,
    callback: SqliteCollationCallback<in AppData>?
): SqliteResultCode = convertResult(db.withMemoryManager {
    heapScoped {
        exports.sqlite3_create_collation_v2(
            db.pointer,
            name.allocateUtf8Pointer(),
            encoding.value,
            keyedStableRefPointer(collationKey(name, encoding), callback, appData, destroy),
            functionPointer(callback, ::CollationCompareHandler),
            stableRefDisposer(callback, destroy)
        )
    }
})

public actual fun <AppData> sqlite3_create_function_v2(
    db: sqlite3,
    name: String,
    nArg: Int,
    encoding: SqliteFunctionTextEncoding,
    appData: AppData,
    func: SqliteFunctionFuncCallback<in AppData>?,
    step: SqliteFunctionStepCallback<in AppData>?,
    final: SqliteFunctionFinalCallback<in AppData>?,
    destroy: SqliteDestroyCallback<in AppData>?
): SqliteResultCode = convertResult(db.withMemoryManager {
    heapScoped {
        createFunction(appData, func, step, final, destroy) { fn, fnDestroy ->
            exports.sqlite3_create_function_v2(
                db.pointer,
                name.allocateUtf8Pointer(),
                nArg,
                encoding.value,
                keyedStableRefPointer(
                    key = functionKey(name, nArg, encoding),
                    data = fn,
                    appData = appData,
                    destructor = fnDestroy
                ),
                functionPointer(func, ::FunctionFuncHandler),
                functionPointer(step, ::FunctionStepHandler),
                functionPointer(final, ::FunctionFinalHandler),
                stableRefDisposer(fn, fnDestroy)
            )
        }
    }
})

public actual fun <AppData> sqlite3_create_module_v2(
    db: sqlite3,
    name: String,
    module: sqlite3_module<AppData>?,
    appData: AppData,
    destroy: SqliteDestroyCallback<in AppData>?
): SqliteResultCode = convertResult(db.withMemoryManager {
    heapScoped {
        createVTabModule(module?.callbacks, appData) { vTabModule ->
            exports.sqlite3_create_module_v2(
                db.pointer,
                name.allocateUtf8Pointer(),
                module?.pointer.notNull,
                keyedStableRefPointer(moduleKey(name), vTabModule, appData, destroy),
                stableRefDisposer(vTabModule, destroy)
            )
        }
    }
})

public actual fun <AppData> sqlite3_create_window_function(
    db: sqlite3,
    name: String,
    nArg: Int,
    encoding: SqliteFunctionTextEncoding,
    appData: AppData,
    step: SqliteFunctionStepCallback<in AppData>?,
    final: SqliteFunctionFinalCallback<in AppData>?,
    value: SqliteFunctionValueCallback<in AppData>?,
    inverse: SqliteFunctionInverseCallback<in AppData>?,
    destroy: SqliteDestroyCallback<in AppData>?
): SqliteResultCode = convertResult(db.withMemoryManager {
    heapScoped {
        createWindowFunction(appData, step, final, value, inverse, destroy) { fn, fnDestroy ->
            exports.sqlite3_create_window_function(
                db.pointer,
                name.allocateUtf8Pointer(),
                nArg,
                encoding.value,
                keyedStableRefPointer(
                    key = windowFunctionKey(name, nArg, encoding),
                    data = fn,
                    appData = appData,
                    destructor = fnDestroy
                ),
                functionPointer(step, ::FunctionStepHandler),
                functionPointer(final, ::FunctionFinalHandler),
                functionPointer(value, ::FunctionValueHandler),
                functionPointer(inverse, ::FunctionInverseHandler),
                stableRefDisposer(fn, fnDestroy)
            )
        }
    }
})

public actual fun sqlite3_data_count(stmt: sqlite3_stmt): Int =
    exports.sqlite3_data_count(stmt.pointer)

public actual fun sqlite3_db_cacheflush(db: sqlite3): SqliteResultCode =
    convertResult(exports.sqlite3_db_cacheflush(db.pointer))

public actual fun sqlite3_db_config(
    db: sqlite3,
    option: SqliteDbConfigOption,
): SqliteResultCode = commonDbConfig(
    option = option,
    bufferPointer = Buffer::pointer,
    outParamConfig = {
        heapScoped {
            useParam(state) { statePtr ->
                invokeVariadic(
                    db::memory,
                    VariadicValue.OfInt(value),
                    VariadicValue.OfPointer(statePtr)
                ) { vaListPtr ->
                    exports.sqlite3_db_config(db.pointer, id, vaListPtr)
                }
            }
        }
    },
    nativeConfig = { id, values ->
        invokeVariadic(values, db::memory) { vaListPtr ->
            exports.sqlite3_db_config(db.pointer, id, vaListPtr)
        }
    }
)

public actual fun sqlite3_db_filename(
    db: sqlite3,
    database: String
): sqlite3_filename? = heapScoped {
    exports.sqlite3_db_filename(db.pointer, database.allocateUtf8Pointer())
}.toKStringFromUtf8OrNull()

public actual fun sqlite3_db_handle(stmt: sqlite3_stmt): sqlite3? =
    exports.sqlite3_db_handle(stmt.pointer).orNull?.let(::sqlite3)

public actual fun sqlite3_db_name(
    db: sqlite3,
    index: Int
): String? = exports.sqlite3_db_name(db.pointer, index)
    .toKStringFromUtf8OrNull()

public actual fun sqlite3_db_readonly(
    db: sqlite3,
    database: String
): SqliteDbReadonlyResult = convertDbReadonlyResult(heapScoped {
    exports.sqlite3_db_readonly(db.pointer, database.allocateUtf8Pointer())
})

public actual fun sqlite3_db_release_memory(db: sqlite3): SqliteResultCode =
    convertResult(exports.sqlite3_db_release_memory(db.pointer))

public actual fun sqlite3_db_status(
    db: sqlite3,
    option: SqliteDbStatusOption,
    outCurrent: Int32OutputParam?,
    outHighwater: Int32OutputParam?,
    resetFlag: Int
): SqliteResultCode =
    convertResult(useParamsStackScoped(outCurrent, outHighwater) { curPtr, highPtr ->
        exports.sqlite3_db_status(db.pointer, option.id, curPtr, highPtr, resetFlag)
    })

public actual fun sqlite3_db_status64(
    db: sqlite3,
    option: SqliteDbStatusOption,
    outCurrent: Int64OutputParam?,
    outHighwater: Int64OutputParam?,
    resetFlag: Int
): SqliteResultCode =
    convertResult(useParamsStackScoped(outCurrent, outHighwater) { curPtr, highPtr ->
        exports.sqlite3_db_status64(db.pointer, option.id, curPtr, highPtr, resetFlag)
    })

public actual fun sqlite3_declare_vtab(
    db: sqlite3,
    sql: String
): SqliteResultCode = convertResult(heapScoped {
    exports.sqlite3_declare_vtab(db.pointer, sql.allocateUtf8Pointer())
})

public actual fun sqlite3_deserialize(
    db: sqlite3,
    database: String?,
    buffer: Buffer,
    dbSize: Long,
    bufferSize: Long,
    flags: SqliteDeserializeFlag?
): SqliteResultCode = convertResult(heapScoped {
    exports.sqlite3_deserialize(
        db.pointer,
        database.allocateUtf8Pointer(),
        buffer.pointer,
        dbSize.toJsBigInt(),
        bufferSize.toJsBigInt(),
        flags?.value ?: 0
    )
})

public actual fun sqlite3_drop_modules(
    db: sqlite3,
    keep: Array<String>?
): SqliteResultCode = convertResult(heapScoped {
    exports.sqlite3_drop_modules(db.pointer, allocateUtf8Array(keep))
})

public actual fun sqlite3_errcode(db: sqlite3): SqliteResultCode =
    convertResult(exports.sqlite3_errcode(db.pointer))

public actual fun sqlite3_errmsg(db: sqlite3): String? =
    exports.sqlite3_errmsg(db.pointer).toKStringFromUtf8OrNull()

public actual fun sqlite3_error_offset(db: sqlite3): Int =
    exports.sqlite3_error_offset(db.pointer)

public actual fun sqlite3_errstr(resultCode: SqliteResultCode): String? =
    exports.sqlite3_errstr(resultCode.code).toKStringFromUtf8OrNull()

public actual fun <AppData> sqlite3_exec(
    db: sqlite3,
    sql: String,
    outErrorMessage: Utf8OutputParam?,
    appData: AppData,
    callback: SqliteExecCallback<AppData>?
): SqliteResultCode = convertResult(useMemoryManager {
    heapScoped {
        outErrorMessage.overriding(freeOnRead = true) {
            useParam(outErrorMessage) { errorMessagePtr ->
                exports.sqlite3_exec(
                    db.pointer,
                    sql.allocateUtf8Pointer(),
                    functionPointer(callback, ::ExecHandler),
                    stableRefPointer(callback, appData),
                    errorMessagePtr
                )
            }
        }
    }
})

public actual fun sqlite3_expanded_sql(stmt: sqlite3_stmt): String? {
    val pointer = exports.sqlite3_expanded_sql(stmt.pointer).orNull ?: return null
    val expandedSql = pointer.toKStringFromUtf8()
    exports.sqlite3_free(pointer)
    return expandedSql
}

public actual fun sqlite3_extended_errcode(db: sqlite3): SqliteResultCode =
    convertResult(exports.sqlite3_extended_errcode(db.pointer))

public actual fun sqlite3_extended_result_codes(
    db: sqlite3,
    enabled: Int
): SqliteResultCode = convertResult(exports.sqlite3_extended_result_codes(db.pointer, enabled))

public actual fun sqlite3_file_control(
    db: sqlite3,
    database: String?,
    opcode: SqliteFileControlOpcode
): SqliteResultCode = convertResult(heapScoped {
    exports.sqlite3_file_control(
        db.pointer,
        database.allocateUtf8Pointer(),
        opcode.code,
        NullPtr
    )
})

public actual fun sqlite3_finalize(stmt: sqlite3_stmt): SqliteResultCode =
    stmt.deallocate { exports.sqlite3_finalize(stmt.pointer) }

public actual fun sqlite3_free(buffer: Buffer): Unit =
    exports.sqlite3_free(buffer.pointer)

public actual fun sqlite3_get_autocommit(db: sqlite3): Int =
    exports.sqlite3_get_autocommit(db.pointer)

public actual fun sqlite3_hard_heap_limit64(limit: Long): Long =
    exports.sqlite3_hard_heap_limit64(limit.toJsBigInt()).toLong()

public actual fun sqlite3_initialize(): SqliteResultCode =
    convertResult(exports.sqlite3_initialize())

public actual fun sqlite3_interrupt(db: sqlite3): Unit =
    exports.sqlite3_interrupt(db.pointer)

public actual fun sqlite3_is_interrupted(db: sqlite3): Int =
    exports.sqlite3_is_interrupted(db.pointer)

public actual fun sqlite3_key(
    db: sqlite3,
    key: ByteArray,
    nKey: Int,
): SqliteResultCode = convertResult(bufferScoped(key) { keyPtr ->
    exports.sqlite3_key(db.pointer, keyPtr, nKey)
})

public actual fun sqlite3_key_v2(
    db: sqlite3,
    database: String,
    key: ByteArray,
    nKey: Int,
): SqliteResultCode = convertResult(heapScoped {
    bufferScoped(key, memory) { keyPtr ->
        exports.sqlite3_key_v2(db.pointer, database.allocateUtf8Pointer(), keyPtr, nKey)
    }
})

public actual fun sqlite3_keyword_check(word: String): Int = heapScoped {
    val cWord = word.allocateUtf8()
    exports.sqlite3_keyword_check(cWord.pointer, cWord.size)
}

public actual fun sqlite3_keyword_count(): Int =
    exports.sqlite3_keyword_count()

public actual fun sqlite3_keyword_name(
    index: Int,
    outName: Utf8OutputParam,
): SqliteResultCode = convertResult(stackScoped {
    val outSize = Int32OutputParam(0)

    outName.overriding(customSize = outSize) {
        useParams(outSize, outName) { sizePtr, namePtr ->
            exports.sqlite3_keyword_name(index, namePtr, sizePtr)
        }
    }
})

public actual fun sqlite3_last_insert_rowid(db: sqlite3): Long =
    exports.sqlite3_last_insert_rowid(db.pointer).toLong()

public actual fun sqlite3_libversion(): String =
    exports.sqlite3_libversion().toKStringFromUtf8()

public actual fun sqlite3_libversion_number(): Int =
    exports.sqlite3_libversion_number()

public actual fun sqlite3_limit(
    db: sqlite3,
    id: SqliteRuntimeLimit,
    newVal: Int
): Int = exports.sqlite3_limit(db.pointer, id.id, newVal)

public actual fun sqlite3_log(
    errorCode: Int,
    message: String
): Unit = heapScoped {
    exports.sqlite3_log(errorCode, message.allocateUtf8Pointer(), NullPtr)
}

public actual fun sqlite3_malloc(size: Int): Buffer? =
    exports.sqlite3_malloc(size).orNull?.let { Buffer.from(it, size.toLong()) }

public actual fun sqlite3_malloc64(size: Long): Buffer? =
    exports.sqlite3_malloc64(size.toJsBigInt()).orNull?.let {
        Buffer.from(it, size)
    }

public actual fun sqlite3_memory_used(): Long =
    exports.sqlite3_memory_used().toLong()

public actual fun sqlite3_memory_highwater(resetFlag: Int): Long =
    exports.sqlite3_memory_highwater(resetFlag).toLong()

public actual fun sqlite3_msize(buffer: Buffer): ULong =
    exports.sqlite3_msize(buffer.pointer).toLong().toULong()

public actual fun sqlite3_next_stmt(
    db: sqlite3,
    stmt: sqlite3_stmt
): sqlite3_stmt? = exports.sqlite3_next_stmt(db.pointer, stmt.pointer)
    .orNull?.let(::sqlite3_stmt)

public actual fun sqlite3_open(
    fileName: String,
    outDb: SqliteOutputParam
): SqliteResultCode = convertResult(heapScoped {
    useParam(outDb) { dbPtr ->
        exports.sqlite3_open(fileName.allocateUtf8Pointer(), dbPtr)
    }
})

public actual fun sqlite3_open_v2(
    fileName: String,
    outDb: SqliteOutputParam,
    flags: SqliteOpenFlag.Db,
    vfs: String?
): SqliteResultCode = convertResult(heapScoped {
    useParam(outDb) { dbPtr ->
        exports.sqlite3_open_v2(
            fileName.allocateUtf8Pointer(),
            dbPtr,
            flags.value,
            vfs.allocateUtf8Pointer()
        )
    }
})

public actual fun sqlite3_overload_function(
    db: sqlite3,
    name: String,
    nArg: Int
): SqliteResultCode = convertResult(heapScoped {
    exports.sqlite3_overload_function(db.pointer, name.allocateUtf8Pointer(), nArg)
})

public actual fun sqlite3_prepare_v2(
    db: sqlite3,
    sql: ByteArray,
    maxBytes: Int,
    outStmt: SqliteStmtOutputParam,
    outOffset: Int32OutputParam?
): SqliteResultCode = convertResult(useParamsStackScoped(outStmt, outOffset) { stmtPtr, offsetPtr ->
    bufferScoped(sql, size = maxBytes) { sqlPtr ->
        exports.ksqlite_prepare_v2(db.pointer, sqlPtr, maxBytes, stmtPtr, offsetPtr)
    }
})

public actual fun sqlite3_prepare_v2(
    db: sqlite3,
    sql: String,
    outStmt: SqliteStmtOutputParam
): SqliteResultCode = convertResult(heapScoped {
    useParam(outStmt) { stmtPtr ->
        val cSql = sql.allocateUtf8()
        exports.sqlite3_prepare_v2(db.pointer, cSql.pointer, cSql.size, stmtPtr, NullPtr)
    }
})

public actual fun sqlite3_prepare_v3(
    db: sqlite3,
    sql: ByteArray,
    maxBytes: Int,
    flags: SqlitePrepareFlag?,
    outStmt: SqliteStmtOutputParam,
    outOffset: Int32OutputParam?
): SqliteResultCode = convertResult(useParamsStackScoped(outStmt, outOffset) { stmtPtr, offsetPtr ->
    bufferScoped(sql, size = maxBytes) { sqlPtr ->
        val prepFlags = flags?.value ?: 0
        exports.ksqlite_prepare_v3(db.pointer, sqlPtr, maxBytes, prepFlags, stmtPtr, offsetPtr)
    }
})

public actual fun sqlite3_prepare_v3(
    db: sqlite3,
    sql: String,
    flags: SqlitePrepareFlag?,
    outStmt: SqliteStmtOutputParam
): SqliteResultCode = convertResult(heapScoped {
    useParam(outStmt) { stmtPtr ->
        val cSql = sql.allocateUtf8()
        val prepFlags = flags?.value ?: 0
        exports.sqlite3_prepare_v3(db.pointer, cSql.pointer, cSql.size, prepFlags, stmtPtr, NullPtr)
    }
})

public actual fun sqlite3_preupdate_blobwrite(db: sqlite3): Int =
    exports.sqlite3_preupdate_blobwrite(db.pointer)

public actual fun sqlite3_preupdate_count(db: sqlite3): Int =
    exports.sqlite3_preupdate_count(db.pointer)

public actual fun sqlite3_preupdate_depth(db: sqlite3): Int =
    exports.sqlite3_preupdate_depth(db.pointer)

public actual fun <AppData> sqlite3_preupdate_hook(
    db: sqlite3,
    appData: AppData,
    callback: SqlitePreupdateHookCallback<AppData>?
): Unit = db.withMemoryManager {
    val _ = exports.sqlite3_preupdate_hook(
        db.pointer,
        functionPointer(callback, ::PreupdateHookHandler),
        keyedStableRefPointer(KEY_PREUPDATE_HOOK, callback, appData)
    )
}

public actual fun sqlite3_preupdate_new(
    db: sqlite3,
    index: Int,
    outValue: SqliteValueOutputParam
): SqliteResultCode = convertResult(useParamStackScoped(outValue) { valuePtr ->
    exports.sqlite3_preupdate_new(db.pointer, index, valuePtr)
})

public actual fun sqlite3_preupdate_old(
    db: sqlite3,
    index: Int,
    outValue: SqliteValueOutputParam
): SqliteResultCode = convertResult(useParamStackScoped(outValue) { valuePtr ->
    exports.sqlite3_preupdate_old(db.pointer, index, valuePtr)
})

public actual fun <AppData> sqlite3_progress_handler(
    db: sqlite3,
    nOps: Int,
    appData: AppData,
    callback: SqliteProgressHandlerCallback<AppData>?
): Unit = db.withMemoryManager {
    exports.sqlite3_progress_handler(
        db.pointer,
        nOps,
        functionPointer(callback, ::ProgressHandlerHandler),
        keyedStableRefPointer(KEY_PROGRESS_HANDLER, callback, appData)
    )
}

public actual fun sqlite3_randomness(
    size: Int,
    buffer: Buffer
): Unit = exports.sqlite3_randomness(size, buffer.pointer)

public actual fun sqlite3_realloc(
    buffer: Buffer,
    size: Int
): Buffer? = Buffer.from(
    pointer = exports.sqlite3_realloc(buffer.pointer, size),
    size = size.toLong()
)

public actual fun sqlite3_realloc64(
    buffer: Buffer,
    size: Long
): Buffer? = Buffer.from(
    pointer = exports.sqlite3_realloc64(buffer.pointer, size.toJsBigInt()),
    size = size
)

public actual fun sqlite3_rekey(
    db: sqlite3,
    key: ByteArray,
    nKey: Int,
): SqliteResultCode = convertResult(bufferScoped(key) { keyPtr ->
    exports.sqlite3_rekey(db.pointer, keyPtr, nKey)
})

public actual fun sqlite3_rekey_v2(
    db: sqlite3,
    dbName: String,
    key: ByteArray,
    nKey: Int,
): SqliteResultCode = convertResult(heapScoped {
    bufferScoped(key, memory) { keyPtr ->
        exports.sqlite3_rekey_v2(db.pointer, dbName.allocateUtf8Pointer(), keyPtr, nKey)
    }
})

public actual fun sqlite3_release_memory(size: Int): Int =
    exports.sqlite3_release_memory(size)

public actual fun sqlite3_reset(stmt: sqlite3_stmt): SqliteResultCode =
    convertResult(exports.sqlite3_reset(stmt.pointer))

public actual fun sqlite3_reset_auto_extension(): Unit =
    autoExtensionReset { exports.sqlite3_reset_auto_extension() }

public actual fun sqlite3_result_blob(
    context: sqlite3_context,
    bytes: ByteArray,
    size: Int,
    destroy: SqliteDestroyCallback<ByteArray>?
): Unit = exports.sqlite3_result_blob(
    context.pointer,
    context.db.memory.byteArrayPointer(bytes, destroy),
    size,
    globalDisposer(bytes)
)

public actual fun sqlite3_result_blob64(
    context: sqlite3_context,
    buffer: Buffer,
    size: Long,
    destroy: SqliteDestroyCallback<Buffer>?
): Unit = exports.sqlite3_result_blob64(
    context.pointer,
    buffer.pointer,
    size.toJsBigInt(),
    bufferDisposer(buffer, destroy)
)

public actual fun sqlite3_result_double(
    context: sqlite3_context,
    value: Double
): Unit = exports.sqlite3_result_double(context.pointer, value)

public actual fun sqlite3_result_error(
    context: sqlite3_context,
    message: String
): Unit = heapScoped {
    val cMessage = message.allocateUtf8()
    exports.sqlite3_result_error(context.pointer, cMessage.pointer, cMessage.size)
}

public actual fun sqlite3_result_error_code(
    context: sqlite3_context,
    result: SqliteResultCode.Failure
): Unit = exports.sqlite3_result_error_code(context.pointer, result.code)

public actual fun sqlite3_result_error_nomem(context: sqlite3_context): Unit =
    exports.sqlite3_result_error_nomem(context.pointer)

public actual fun sqlite3_result_error_toobig(context: sqlite3_context): Unit =
    exports.sqlite3_result_error_toobig(context.pointer)

public actual fun sqlite3_result_int(
    context: sqlite3_context,
    value: Int
): Unit = exports.sqlite3_result_int(context.pointer, value)

public actual fun sqlite3_result_int64(
    context: sqlite3_context,
    value: Long
): Unit = exports.sqlite3_result_int64(context.pointer, value.toJsBigInt())

public actual fun sqlite3_result_null(context: sqlite3_context): Unit =
    exports.sqlite3_result_null(context.pointer)

public actual fun <Data> sqlite3_result_pointer(
    context: sqlite3_context,
    data: Data,
    type: String?,
    destroy: SqliteDestroyCallback<Data>?
): Unit = with(globalMemory) {
    // Use globalMemory because of lack of information within sqlite3_value_pointer()
    allocateNamedPointer(type, destroy) { ptr, ptrDestroy ->
        exports.sqlite3_result_pointer(
            context.pointer,
            stableRefPointer(ptr, data, ptrDestroy),
            ptr.name,
            stableRefDisposer(ptr, ptrDestroy)
        )
    }
}

public actual fun sqlite3_result_subtype(
    context: sqlite3_context,
    subtype: UInt
): Unit = exports.sqlite3_result_subtype(context.pointer, subtype.toInt())

public actual fun sqlite3_result_text(
    context: sqlite3_context,
    value: String
): Unit = heapScoped {
    val cText = value.allocateUtf8()
    exports.sqlite3_result_text(context.pointer, cText.pointer, cText.size, SqliteTransient)
}

public actual fun sqlite3_result_text64(
    context: sqlite3_context,
    buffer: Buffer,
    size: Long,
    encoding: SqliteTextEncoding.ResultText,
    destroy: SqliteDestroyCallback<Buffer>?
): Unit = exports.sqlite3_result_text64(
    context.pointer,
    buffer.pointer,
    size.toJsBigInt(),
    bufferDisposer(buffer, destroy),
    encoding.value
)

public actual fun sqlite3_result_value(
    context: sqlite3_context,
    value: sqlite3_value,
): Unit = exports.sqlite3_result_value(context.pointer, value.pointer)

public actual fun sqlite3_result_zeroblob(
    context: sqlite3_context,
    size: Int
): Unit = exports.sqlite3_result_zeroblob(context.pointer, size)

public actual fun sqlite3_result_zeroblob64(
    context: sqlite3_context,
    size: ULong
): SqliteResultCode =
    convertResult(exports.sqlite3_result_zeroblob64(context.pointer, size.toLong().toJsBigInt()))

public actual fun <AppData> sqlite3_rollback_hook(
    db: sqlite3,
    appData: AppData,
    callback: SqliteRollbackHookCallback<AppData>?
): Unit = db.withMemoryManager {
    val _ = exports.sqlite3_rollback_hook(
        db.pointer,
        functionPointer(callback, ::RollbackHookHandler),
        keyedStableRefPointer(KEY_ROLLBACK_HOOK, callback, appData)
    )
}

public actual fun <AppData> sqlite3_set_authorizer(
    db: sqlite3,
    appData: AppData,
    callback: SqliteAuthorizerCallback<AppData>?
): SqliteResultCode = convertResult(db.withMemoryManager {
    exports.sqlite3_set_authorizer(
        db.pointer,
        functionPointer(callback, ::AuthorizerHandler),
        keyedStableRefPointer(KEY_SET_AUTHORIZER, callback, appData)
    )
})

public actual fun sqlite3_set_errmsg(
    db: sqlite3,
    errorCode: SqliteResultCode,
    errorMessage: String?
): SqliteResultCode = convertResult(heapScoped {
    exports.sqlite3_set_errmsg(db.pointer, errorCode.code, errorMessage.allocateUtf8Pointer())
})

public actual fun sqlite3_set_last_insert_rowid(
    db: sqlite3,
    rowId: Long
): Unit = exports.sqlite3_set_last_insert_rowid(db.pointer, rowId.toJsBigInt())

public actual fun sqlite3_shutdown(): SqliteResultCode =
    convertResult(exports.sqlite3_shutdown())

public actual fun sqlite3_snapshot_cmp(
    snapshot1: sqlite3_snapshot,
    snapshot2: sqlite3_snapshot
): Int = exports.sqlite3_snapshot_cmp(snapshot1.pointer, snapshot2.pointer)

public actual fun sqlite3_snapshot_free(snapshot: sqlite3_snapshot): Unit =
    exports.sqlite3_snapshot_free(snapshot.pointer)

public actual fun sqlite3_snapshot_get(
    db: sqlite3,
    name: String?,
    outSnapshot: SqliteSnapshotOutputParam
): SqliteResultCode = convertResult(heapScoped {
    useParam(outSnapshot) { snapshotPtr ->
        exports.sqlite3_snapshot_get(db.pointer, name.allocateUtf8Pointer(), snapshotPtr)
    }
})

public actual fun sqlite3_snapshot_open(
    db: sqlite3,
    name: String?,
    snapshot: sqlite3_snapshot
): SqliteResultCode = convertResult(heapScoped {
    exports.sqlite3_snapshot_open(db.pointer, name.allocateUtf8Pointer(), snapshot.pointer)
})

public actual fun sqlite3_snapshot_recover(
    db: sqlite3,
    name: String?
): SqliteResultCode = convertResult(heapScoped {
    exports.sqlite3_snapshot_recover(db.pointer, name.allocateUtf8Pointer())
})

public actual fun sqlite3_soft_heap_limit64(limit: Long): Long =
    exports.sqlite3_soft_heap_limit64(limit.toJsBigInt()).toLong()

public actual fun sqlite3_sourceid(): String =
    exports.sqlite3_sourceid().toKStringFromUtf8()

public actual fun sqlite3_sql(stmt: sqlite3_stmt): String =
    exports.sqlite3_sql(stmt.pointer).toKStringFromUtf8()

public actual fun sqlite3_status(
    option: SqliteStatusOption,
    outCurrent: Int32OutputParam,
    outHighwater: Int32OutputParam,
    resetFlag: Int
): SqliteResultCode =
    convertResult(useParamsStackScoped(outCurrent, outHighwater) { curPtr, highPtr ->
        exports.sqlite3_status(option.id, curPtr, highPtr, resetFlag)
    })

public actual fun sqlite3_status64(
    option: SqliteStatusOption,
    outCurrent: Int64OutputParam,
    outHighwater: Int64OutputParam,
    resetFlag: Int
): SqliteResultCode =
    convertResult(useParamsStackScoped(outCurrent, outHighwater) { curPtr, highPtr ->
        exports.sqlite3_status64(option.id, curPtr, highPtr, resetFlag)
    })

public actual fun sqlite3_step(stmt: sqlite3_stmt): SqliteResultCode =
    convertResult(exports.sqlite3_step(stmt.pointer))

public actual fun sqlite3_stmt_busy(stmt: sqlite3_stmt): Int =
    exports.sqlite3_stmt_busy(stmt.pointer)

public actual fun sqlite3_stmt_explain(
    stmt: sqlite3_stmt,
    mode: SqliteExplainMode
): SqliteResultCode = convertResult(exports.sqlite3_stmt_explain(stmt.pointer, mode.id))

public actual fun sqlite3_stmt_isexplain(stmt: sqlite3_stmt): SqliteExplainMode =
    convertExplainMode(exports.sqlite3_stmt_isexplain(stmt.pointer))

public actual fun sqlite3_stmt_readonly(stmt: sqlite3_stmt): Int =
    exports.sqlite3_stmt_readonly(stmt.pointer)

public actual fun sqlite3_stmt_status(
    stmt: sqlite3_stmt,
    counter: SqliteStatementStatusCounter,
    resetFlag: Int
): Int = exports.sqlite3_stmt_status(stmt.pointer, counter.id, resetFlag)

public actual fun sqlite3_strglob(
    globPattern: String,
    input: String
): Int = heapScoped {
    exports.sqlite3_strglob(globPattern.allocateUtf8Pointer(), input.allocateUtf8Pointer())
}

public actual fun sqlite3_stricmp(
    first: String,
    second: String
): Int = heapScoped {
    exports.sqlite3_stricmp(first.allocateUtf8Pointer(), second.allocateUtf8Pointer())
}

public actual fun sqlite3_strlike(
    likePattern: String,
    input: String,
    escapeCharacter: Char
): Int = heapScoped {
    exports.sqlite3_strlike(
        likePattern.allocateUtf8Pointer(),
        input.allocateUtf8Pointer(),
        escapeCharacter.code
    )
}

public actual fun sqlite3_strnicmp(
    first: String,
    second: String,
    maxCharacters: Int
): Int = heapScoped {
    exports.sqlite3_strnicmp(
        first.allocateUtf8Pointer(),
        second.allocateUtf8Pointer(),
        maxCharacters
    )
}

public actual fun sqlite3_system_errno(db: sqlite3): Int =
    exports.sqlite3_system_errno(db.pointer)

public actual fun sqlite3_table_column_metadata(
    db: sqlite3,
    dbName: String?,
    tableName: String,
    columnName: String,
    outDataType: Utf8OutputParam?,
    outCollationName: Utf8OutputParam?,
    outNotNull: Int32OutputParam?,
    outPrimaryKey: Int32OutputParam?,
    outAutoIncrement: Int32OutputParam?
): SqliteResultCode = convertResult(heapScoped {
    val dataTypePtr = outDataType?.attach(this)
    val collationNamePtr = outCollationName?.attach(this)
    val notNullPtr = outNotNull?.attach(this)
    val primaryKeyPtr = outPrimaryKey?.attach(this)
    val autoIncrementPtr = outAutoIncrement?.attach(this)

    try {
        exports.sqlite3_table_column_metadata(
            db.pointer,
            dbName.allocateUtf8Pointer(),
            tableName.allocateUtf8Pointer(),
            columnName.allocateUtf8Pointer(),
            dataTypePtr.notNull,
            collationNamePtr.notNull,
            notNullPtr.notNull,
            primaryKeyPtr.notNull,
            autoIncrementPtr.notNull
        )
    } finally {
        dataTypePtr?.let(outDataType::detach)
        collationNamePtr?.let(outCollationName::detach)
        notNullPtr?.let(outNotNull::detach)
        primaryKeyPtr?.let(outPrimaryKey::detach)
        autoIncrementPtr?.let(outAutoIncrement::detach)
    }
})

public actual fun sqlite3_total_changes(db: sqlite3): Int =
    exports.sqlite3_total_changes(db.pointer)

public actual fun sqlite3_total_changes64(db: sqlite3): Long =
    exports.sqlite3_total_changes64(db.pointer).toLong()

public actual fun <AppData> sqlite3_trace_v2(
    db: sqlite3,
    mask: SqliteTraceCode?,
    appData: AppData,
    callback: SqliteTraceCallback<AppData>?
): SqliteResultCode = convertResult(db.withMemoryManager {
    exports.sqlite3_trace_v2(
        db.pointer,
        mask?.code ?: 0,
        functionPointer(callback, ::TraceHandler),
        keyedStableRefPointer(KEY_TRACE, callback, appData)
    )
})

public actual fun sqlite3_txn_state(
    db: sqlite3,
    database: String?
): SqliteTransactionState? = convertTransactionState(heapScoped {
    exports.sqlite3_txn_state(db.pointer, database.allocateUtf8Pointer())
})

public actual fun <AppData> sqlite3_update_hook(
    db: sqlite3,
    appData: AppData,
    callback: SqliteUpdateHookCallback<AppData>?
): Unit = db.withMemoryManager {
    val _ = exports.sqlite3_update_hook(
        db.pointer,
        functionPointer(callback, ::UpdateHookHandler),
        keyedStableRefPointer(KEY_UPDATE_HOOK, callback, appData)
    )
}

public actual fun sqlite3_uri_boolean(
    fileName: sqlite3_filename,
    parameter: String,
    default: Int
): Int = heapScoped {
    exports.sqlite3_uri_boolean(
        fileName.allocateUtf8Pointer(),
        parameter.allocateUtf8Pointer(),
        default
    )
}

public actual fun sqlite3_uri_int64(
    fileName: sqlite3_filename,
    parameter: String,
    default: Long
): Long = heapScoped {
    exports.sqlite3_uri_int64(
        fileName.allocateUtf8Pointer(),
        parameter.allocateUtf8Pointer(),
        default.toJsBigInt()
    )
}.toLong()

public actual fun sqlite3_uri_key(
    fileName: sqlite3_filename,
    index: Int
): String? = heapScoped {
    exports.sqlite3_uri_key(fileName.allocateUtf8Pointer(), index)
}.toKStringFromUtf8OrNull()

public actual fun sqlite3_uri_parameter(
    fileName: sqlite3_filename,
    parameter: String
): String? = heapScoped {
    exports.sqlite3_uri_parameter(fileName.allocateUtf8Pointer(), parameter.allocateUtf8Pointer())
}.toKStringFromUtf8OrNull()

public actual fun sqlite3_value_blob(value: sqlite3_value): ByteArray? = commonValueByteArray(
    value = value,
    pointer = exports.sqlite3_value_blob(value.pointer),
    toByteArray = WasmPointer::readByteArray
)

public actual fun sqlite3_value_bytes(value: sqlite3_value): Int =
    exports.sqlite3_value_bytes(value.pointer)

public actual fun sqlite3_value_double(value: sqlite3_value): Double =
    exports.sqlite3_value_double(value.pointer)

public actual fun sqlite3_value_dup(value: sqlite3_value): sqlite3_value? =
    exports.sqlite3_value_dup(value.pointer).orNull?.let(::sqlite3_value)

public actual fun sqlite3_value_encoding(value: sqlite3_value): SqliteTextEncoding.ValueEncoding =
    convertTextEncoding(exports.sqlite3_value_encoding(value.pointer))

public actual fun sqlite3_value_free(value: sqlite3_value): Unit =
    exports.sqlite3_value_free(value.pointer)

public actual fun sqlite3_value_frombind(value: sqlite3_value): Int =
    exports.sqlite3_value_frombind(value.pointer)

public actual fun sqlite3_value_int(value: sqlite3_value): Int =
    exports.sqlite3_value_int(value.pointer)

public actual fun sqlite3_value_int64(value: sqlite3_value): Long =
    exports.sqlite3_value_int64(value.pointer).toLong()

public actual fun sqlite3_value_nochange(value: sqlite3_value): Int =
    exports.sqlite3_value_nochange(value.pointer)

public actual fun sqlite3_value_numeric_type(value: sqlite3_value): SqliteDataType =
    convertDataType(exports.sqlite3_value_numeric_type(value.pointer))

public actual fun sqlite3_value_subtype(value: sqlite3_value): UInt =
    exports.sqlite3_value_subtype(value.pointer).toUInt()

public actual fun sqlite3_value_text(value: sqlite3_value): String? =
    exports.sqlite3_value_text(value.pointer).toKStringFromUtf8OrNull()

public actual fun sqlite3_value_type(value: sqlite3_value): SqliteDataType =
    convertDataType(exports.sqlite3_value_type(value.pointer))

public actual fun sqlite3_vfs_find(name: String?): sqlite3_vfs? = heapScoped {
    exports.sqlite3_vfs_find(name.allocateUtf8Pointer()).orNull?.let(::sqlite3_vfs)
}

public actual fun sqlite3_vfs_register(
    vfs: sqlite3_vfs,
    makeDefault: Int
): SqliteResultCode = convertResult(exports.sqlite3_vfs_register(vfs.pointer, makeDefault))

public actual fun sqlite3_vfs_unregister(vfs: sqlite3_vfs): SqliteResultCode =
    convertResult(exports.sqlite3_vfs_unregister(vfs.pointer))

public actual fun sqlite3_vtab_collation(
    info: sqlite3_index_info,
    index: Int
): String = exports.sqlite3_vtab_collation(info.pointer, index)
    .toKStringFromUtf8()

public actual fun sqlite3_vtab_config(
    db: sqlite3,
    option: SqliteVTabConfigOption
): SqliteResultCode = commonVtabConfig(option) { id, values ->
    invokeVariadic(values, db::memory) { vaListPtr ->
        exports.sqlite3_vtab_config(db.pointer, id, vaListPtr)
    }
}

public actual fun sqlite3_vtab_distinct(info: sqlite3_index_info): Int =
    exports.sqlite3_vtab_distinct(info.pointer)

public actual fun sqlite3_vtab_in(
    info: sqlite3_index_info,
    index: Int,
    handle: Int
): Int = exports.sqlite3_vtab_in(info.pointer, index, handle)

public actual fun sqlite3_vtab_in_first(
    value: sqlite3_value,
    outValue: SqliteValueOutputParam?
): SqliteResultCode = convertResult(useParamStackScoped(outValue) { valuePtr ->
    exports.sqlite3_vtab_in_first(value.pointer, valuePtr)
})

public actual fun sqlite3_vtab_in_next(
    value: sqlite3_value,
    outValue: SqliteValueOutputParam?
): SqliteResultCode = convertResult(useParamStackScoped(outValue) { valuePtr ->
    exports.sqlite3_vtab_in_next(value.pointer, valuePtr)
})

public actual fun sqlite3_vtab_nochange(context: sqlite3_context): Int =
    exports.sqlite3_vtab_nochange(context.pointer)

public actual fun sqlite3_vtab_on_conflict(db: sqlite3): SqliteConflictResolutionMode =
    convertConflictResolutionMode(exports.sqlite3_vtab_on_conflict(db.pointer))

public actual fun sqlite3_vtab_rhs_value(
    info: sqlite3_index_info,
    index: Int,
    outValue: SqliteValueOutputParam?
): SqliteResultCode = convertResult(useParamStackScoped(outValue) { valuePtr ->
    exports.sqlite3_vtab_rhs_value(info.pointer, index, valuePtr)
})

public actual fun sqlite3_wal_autocheckpoint(
    db: sqlite3,
    nFrame: Int
): SqliteResultCode = convertResult(exports.sqlite3_wal_autocheckpoint(db.pointer, nFrame))

public actual fun sqlite3_wal_checkpoint(
    db: sqlite3,
    name: String?
): SqliteResultCode = convertResult(heapScoped {
    exports.sqlite3_wal_checkpoint(db.pointer, name.allocateUtf8Pointer())
})

public actual fun sqlite3_wal_checkpoint_v2(
    db: sqlite3,
    name: String?,
    mode: SqliteCheckpointMode,
    outNLog: Int32OutputParam?,
    outNCkpt: Int32OutputParam?
): SqliteResultCode = convertResult(heapScoped {
    useParams(outNLog, outNCkpt) { nLogPtr, nCkptPtr ->
        exports.sqlite3_wal_checkpoint_v2(
            db.pointer,
            name.allocateUtf8Pointer(),
            mode.id,
            nLogPtr,
            nCkptPtr
        )
    }
})

public actual fun <AppData> sqlite3_wal_hook(
    db: sqlite3,
    appData: AppData,
    callback: SqliteWalHookCallback<AppData>?
): Unit = db.withMemoryManager {
    exports.sqlite3_wal_hook(
        db.pointer,
        functionPointer(callback, ::WalHookHandler),
        keyedStableRefPointer(KEY_WAL_HOOK, callback, appData)
    )
}