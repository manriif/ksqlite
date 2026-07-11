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
import ksqlite.capi.handlers.CollationHandler
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
import ksqlite.capi.memory.Int32OutputParam
import ksqlite.capi.memory.Int64OutputParam
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.NullPtr
import ksqlite.capi.memory.OpaqueBuffer
import ksqlite.capi.memory.OutputParamBase
import ksqlite.capi.memory.Utf8OutputParam
import ksqlite.capi.memory.allocate
import ksqlite.capi.memory.allocateUtf8
import ksqlite.capi.memory.toCStringArray
import ksqlite.capi.memory.bufferDisposer
import ksqlite.capi.memory.contentSize
import ksqlite.capi.memory.globalDisposer
import ksqlite.capi.memory.globalMemory
import ksqlite.capi.memory.invokeVariadic
import ksqlite.capi.memory.memScoped
import ksqlite.capi.memory.memory
import ksqlite.capi.memory.notNull
import ksqlite.capi.memory.orNull
import ksqlite.capi.memory.overriding
import ksqlite.capi.memory.reading
import ksqlite.capi.memory.stableRefDisposable
import ksqlite.capi.memory.stableRefDisposer
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.memory.useMemoryManager
import ksqlite.capi.memory.useParam
import ksqlite.capi.memory.useParamMemScoped
import ksqlite.capi.memory.useParams
import ksqlite.capi.memory.useParamsMemScoped
import ksqlite.capi.memory.withMemoryManager
import ksqlite.capi.memory.wrapOrNull
import ksqlite.capi.types.SqliteConfigOption
import ksqlite.capi.types.SqliteDbConfigOption
import ksqlite.capi.types.SqliteFileControlOpcode
import ksqlite.capi.vfs.sqlite3_vfs
import ksqlite.capi.vtab.SqliteVtabConfigOption
import ksqlite.capi.vtab.createVtabModule
import ksqlite.capi.vtab.sqlite3_index_info
import ksqlite.capi.vtab.sqlite3_module
import ksqlite.foreign.ksqliteLoadLibrary
import ksqlite.types.SqliteBlobOpenFlag
import ksqlite.types.SqliteCheckpointMode
import ksqlite.types.SqliteCompleteResult
import ksqlite.types.SqliteConflictResolutionMode
import ksqlite.types.SqliteDataType
import ksqlite.types.SqliteDbReadonlyResult
import ksqlite.types.SqliteDbStatusOption
import ksqlite.types.SqliteDeserializeFlag
import ksqlite.types.SqliteExplainMode
import ksqlite.types.SqliteFunctionTextEncoding
import ksqlite.types.SqliteOpenFlag
import ksqlite.types.SqlitePrepareFlag
import ksqlite.types.SqliteResultCode
import ksqlite.types.SqliteRuntimeLimit
import ksqlite.types.SqliteStatementStatusCounter
import ksqlite.types.SqliteStatusOption
import ksqlite.types.SqliteTextEncoding
import ksqlite.types.SqliteTraceEventCode
import ksqlite.types.SqliteTransactionState
import ksqlite.types.internal.convertCompleteResult
import ksqlite.types.internal.convertConflictResolutionMode
import ksqlite.types.internal.convertDataType
import ksqlite.types.internal.convertDbReadonlyResult
import ksqlite.types.internal.convertExplainMode
import ksqlite.types.internal.convertResultCode
import ksqlite.types.internal.convertTextEncoding
import ksqlite.types.internal.convertTransactionState
import java.lang.foreign.ValueLayout
import ksqlite.foreign.sqlite3 as native

/**
 * Loads the native library.
 */
@Suppress("unused")
private val nativeInit = run(::ksqliteLoadLibrary)

public actual fun sqlite3_auto_extension(callback: SqliteAutoExtensionCallback): SqliteResultCode =
    autoExtensionRegister(callback) { native.ksqlite_auto_extension(AutoExtensionHandler) }

public actual fun <AppData> sqlite3_autovacuum_pages(
    db: sqlite3,
    appData: AppData,
    destroy: SqliteDestroyCallback<in AppData>?,
    callback: SqliteAutovacuumPagesCallback<in AppData>?
): SqliteResultCode = convertResultCode(db.withMemoryManager {
    native.sqlite3_autovacuum_pages(
        db.pointer,
        functionPointer(callback, ::AutovacuumPagesHandler),
        stableRefPointer(callback, appData, destroy),
        stableRefDisposer(callback, destroy)
    )
})

public actual fun sqlite3_backup_finish(backup: sqlite3_backup): SqliteResultCode =
    convertResultCode(native.sqlite3_backup_finish(backup.pointer))

public actual fun sqlite3_backup_init(
    destDb: sqlite3,
    destDbName: String,
    srcDb: sqlite3,
    srcDbName: String
): sqlite3_backup? = memScoped {
    native.sqlite3_backup_init(
        destDb.pointer,
        destDbName.allocateUtf8(),
        srcDb.pointer,
        srcDbName.allocateUtf8()
    )
}.wrapOrNull(::sqlite3_backup)

public actual fun sqlite3_backup_pagecount(backup: sqlite3_backup): Int =
    native.sqlite3_backup_pagecount(backup.pointer)

public actual fun sqlite3_backup_remaining(backup: sqlite3_backup): Int =
    native.sqlite3_backup_remaining(backup.pointer)

public actual fun sqlite3_backup_step(
    backup: sqlite3_backup,
    nPage: Int
): SqliteResultCode = convertResultCode(native.sqlite3_backup_step(backup.pointer, nPage))

public actual fun sqlite3_bind_blob(
    stmt: sqlite3_stmt,
    index: Int,
    bytes: ByteArray,
    size: Int,
    destroy: SqliteDestroyCallback<ByteArray>?
): SqliteResultCode = convertResultCode(
    native.sqlite3_bind_blob(
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
): SqliteResultCode = convertResultCode(
    native.sqlite3_bind_blob64(
        stmt.pointer,
        index,
        buffer.pointer,
        size,
        bufferDisposer(buffer, destroy)
    )
)

public actual fun sqlite3_bind_double(
    stmt: sqlite3_stmt,
    index: Int,
    value: Double
): SqliteResultCode = convertResultCode(native.sqlite3_bind_double(stmt.pointer, index, value))

public actual fun sqlite3_bind_int(
    stmt: sqlite3_stmt,
    index: Int,
    value: Int
): SqliteResultCode = convertResultCode(native.sqlite3_bind_int(stmt.pointer, index, value))

public actual fun sqlite3_bind_int64(
    stmt: sqlite3_stmt,
    index: Int,
    value: Long
): SqliteResultCode = convertResultCode(native.sqlite3_bind_int64(stmt.pointer, index, value))

public actual fun sqlite3_bind_null(
    stmt: sqlite3_stmt,
    index: Int
): SqliteResultCode = convertResultCode(native.sqlite3_bind_null(stmt.pointer, index))

public actual fun sqlite3_bind_parameter_count(stmt: sqlite3_stmt): Int =
    native.sqlite3_bind_parameter_count(stmt.pointer)

public actual fun sqlite3_bind_parameter_index(
    stmt: sqlite3_stmt,
    name: String
): Int = memScoped {
    native.sqlite3_bind_parameter_index(stmt.pointer, name.allocateUtf8())
}

public actual fun sqlite3_bind_parameter_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native.sqlite3_bind_parameter_name(stmt.pointer, index)
    .toKStringFromUtf8OrNull()

public actual fun <Data> sqlite3_bind_pointer(
    stmt: sqlite3_stmt,
    index: Int,
    data: Data,
    type: String?,
    destroy: SqliteDestroyCallback<Data>?
): SqliteResultCode = convertResultCode(with(globalMemory) {
    // Use globalMemory because of lack of information within sqlite3_value_pointer()
    allocateNamedPointer(type, destroy) { ptr, ptrDestroy ->
        native.sqlite3_bind_pointer(
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
): SqliteResultCode = convertResultCode(memScoped {
    val cText = value.allocateUtf8()

    native.sqlite3_bind_text(
        stmt.pointer,
        index,
        cText,
        cText.contentSize,
        native.SQLITE_TRANSIENT()
    )
})

public actual fun sqlite3_bind_text64(
    stmt: sqlite3_stmt,
    index: Int,
    buffer: Buffer,
    size: Long,
    encoding: SqliteTextEncoding.BindText,
    destroy: SqliteDestroyCallback<Buffer>?
): SqliteResultCode = convertResultCode(
    native.sqlite3_bind_text64(
        stmt.pointer,
        index,
        buffer.pointer,
        size,
        bufferDisposer(buffer, destroy),
        encoding.value.toByte()
    )
)

public actual fun sqlite3_bind_value(
    stmt: sqlite3_stmt,
    index: Int,
    value: sqlite3_value
): SqliteResultCode =
    convertResultCode(native.sqlite3_bind_value(stmt.pointer, index, value.pointer))

public actual fun sqlite3_bind_zeroblob(
    stmt: sqlite3_stmt,
    index: Int,
    size: Int
): SqliteResultCode = convertResultCode(native.sqlite3_bind_zeroblob(stmt.pointer, index, size))

public actual fun sqlite3_bind_zeroblob64(
    stmt: sqlite3_stmt,
    index: Int,
    size: ULong
): SqliteResultCode =
    convertResultCode(native.sqlite3_bind_zeroblob64(stmt.pointer, index, size.toLong()))

public actual fun sqlite3_blob_bytes(blob: sqlite3_blob): Int =
    native.sqlite3_blob_bytes(blob.pointer)

public actual fun sqlite3_blob_close(blob: sqlite3_blob): SqliteResultCode =
    convertResultCode(native.sqlite3_blob_close(blob.pointer))

public actual fun sqlite3_blob_open(
    db: sqlite3,
    database: String,
    tableName: String,
    columnName: String,
    rowid: Long,
    flags: SqliteBlobOpenFlag,
    outBlob: sqlite3_blob.OutputParam
): SqliteResultCode = convertResultCode(memScoped {
    useParam(outBlob) { blobPtr ->
        native.sqlite3_blob_open(
            db.pointer,
            database.allocateUtf8(),
            tableName.allocateUtf8(),
            columnName.allocateUtf8(),
            rowid,
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
): SqliteResultCode = convertResultCode(memScoped {
    output.reading { outPtr ->
        native.sqlite3_blob_read(blob.pointer, outPtr, size, offset)
    }
})

public actual fun sqlite3_blob_reopen(
    blob: sqlite3_blob,
    rowid: Long
): SqliteResultCode = convertResultCode(native.sqlite3_blob_reopen(blob.pointer, rowid))

public actual fun sqlite3_blob_write(
    blob: sqlite3_blob,
    input: ByteArray,
    size: Int,
    offset: Int
): SqliteResultCode = convertResultCode(memScoped {
    native.sqlite3_blob_write(blob.pointer, input.allocate(size), size, offset)
})

public actual fun <AppData> sqlite3_busy_handler(
    db: sqlite3,
    appData: AppData,
    callback: SqliteBusyHandlerCallback<AppData>?
): SqliteResultCode = convertResultCode(db.withMemoryManager {
    native.sqlite3_busy_handler(
        db.pointer,
        functionPointer(callback, ::BusyHandlerHandler),
        keyedStableRefPointer(KEY_BUSY_HANDLER, callback, appData)
    )
})

public actual fun sqlite3_busy_timeout(
    db: sqlite3,
    millis: Int
): SqliteResultCode = convertResultCode(native.sqlite3_busy_timeout(db.pointer, millis))

public actual fun sqlite3_cancel_auto_extension(callback: SqliteAutoExtensionCallback): Int =
    autoExtensionUnregister(callback) { native.ksqlite_cancel_auto_extension(AutoExtensionHandler) }

public actual fun sqlite3_changes(db: sqlite3): Int =
    native.sqlite3_changes(db.pointer)

public actual fun sqlite3_changes64(db: sqlite3): Long =
    native.sqlite3_changes64(db.pointer)

public actual fun sqlite3_clear_bindings(stmt: sqlite3_stmt): SqliteResultCode =
    commonClearBindings(stmt, native.sqlite3_clear_bindings(stmt.pointer))

public actual fun sqlite3_close(db: sqlite3): SqliteResultCode =
    db.deallocate { native.sqlite3_close(it.pointer) }

public actual fun sqlite3_close_v2(db: sqlite3): SqliteResultCode =
    db.deallocate { native.sqlite3_close_v2(it.pointer) }

public actual fun <AppData> sqlite3_collation_needed(
    db: sqlite3,
    appData: AppData,
    callback: SqliteCollationNeededCallback<AppData>?,
): SqliteResultCode = convertResultCode(db.withMemoryManager {
    native.sqlite3_collation_needed(
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
    pointer = native.sqlite3_column_blob(stmt.pointer, index),
    toByteArray = { pointer, size ->
        pointer
            .reinterpret(size.toLong())
            .toArray(ValueLayout.JAVA_BYTE)
    }
)

public actual fun sqlite3_column_bytes(
    stmt: sqlite3_stmt,
    index: Int
): Int = native.sqlite3_column_bytes(stmt.pointer, index)

public actual fun sqlite3_column_count(stmt: sqlite3_stmt): Int =
    native.sqlite3_column_count(stmt.pointer)

public actual fun sqlite3_column_database_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native.sqlite3_column_database_name(stmt.pointer, index)
    .toKStringFromUtf8OrNull()

public actual fun sqlite3_column_decltype(
    stmt: sqlite3_stmt,
    index: Int
): String? = native.sqlite3_column_decltype(stmt.pointer, index)
    .toKStringFromUtf8OrNull()

public actual fun sqlite3_column_double(
    stmt: sqlite3_stmt,
    index: Int
): Double = native.sqlite3_column_double(stmt.pointer, index)

public actual fun sqlite3_column_int(
    stmt: sqlite3_stmt,
    index: Int
): Int = native.sqlite3_column_int(stmt.pointer, index)

public actual fun sqlite3_column_int64(
    stmt: sqlite3_stmt,
    index: Int
): Long = native.sqlite3_column_int64(stmt.pointer, index)

public actual fun sqlite3_column_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native.sqlite3_column_name(stmt.pointer, index)
    .toKStringFromUtf8OrNull()

public actual fun sqlite3_column_origin_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native.sqlite3_column_origin_name(stmt.pointer, index)
    .toKStringFromUtf8OrNull()

public actual fun sqlite3_column_table_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native.sqlite3_column_table_name(stmt.pointer, index)
    .toKStringFromUtf8OrNull()

public actual fun sqlite3_column_text(
    stmt: sqlite3_stmt,
    index: Int
): String? = native.sqlite3_column_text(stmt.pointer, index)
    .toKStringFromUtf8OrNull()

public actual fun sqlite3_column_type(
    stmt: sqlite3_stmt,
    index: Int
): SqliteDataType = convertDataType(native.sqlite3_column_type(stmt.pointer, index))

public actual fun sqlite3_column_value(
    stmt: sqlite3_stmt,
    index: Int
): sqlite3_value? = native.sqlite3_column_value(stmt.pointer, index)
    .wrapOrNull(::sqlite3_value)

public actual fun <AppData> sqlite3_commit_hook(
    db: sqlite3,
    appData: AppData,
    callback: SqliteCommitHookCallback<AppData>?
): Unit = db.withMemoryManager {
    native.sqlite3_commit_hook(
        db.pointer,
        functionPointer(callback, ::CommitHookHandler),
        keyedStableRefPointer(KEY_COMMIT_HOOK, callback, appData)
    )
}

public actual fun sqlite3_compileoption_get(index: Int): String? =
    native.sqlite3_compileoption_get(index).toKStringFromUtf8OrNull()

public actual fun sqlite3_compileoption_used(optName: String): Int = memScoped {
    native.sqlite3_compileoption_used(optName.allocateUtf8())
}

public actual fun sqlite3_complete(sql: String): SqliteCompleteResult =
    convertCompleteResult(memScoped { native.sqlite3_complete(sql.allocateUtf8()) })

public actual fun sqlite3_config(option: SqliteConfigOption): SqliteResultCode = commonConfig(
    option = option,
    logFunctionPointer = { cb, _ -> globalMemory.functionPointer(cb, ::ConfigLogHandler) },
    sqllogFunctionPointer = { cb, _ -> globalMemory.functionPointer(cb, ::ConfigSqlLogHandler) },
    bufferPointer = OpaqueBuffer::pointer,
    keyedStableRefPointer = globalMemory::keyedStableRefPointer,
    outputParamConfig = {
        useParamMemScoped(state) { statePtr ->
            native.sqlite3_config
                .makeInvoker(ValueLayout.ADDRESS)
                .apply(id, statePtr)
        }
    },
    nativeConfig = { id, values ->
        invokeVariadic(values, ::globalMemory) { layouts, arguments ->
            native.sqlite3_config
                .makeInvoker(*layouts)
                .apply(id, *arguments)
        }
    }
)

public actual fun sqlite3_context_db_handle(context: sqlite3_context): sqlite3 =
    sqlite3(native.sqlite3_context_db_handle(context.pointer))

public actual fun <AppData> sqlite3_create_collation_v2(
    db: sqlite3,
    name: String,
    encoding: SqliteTextEncoding.CreateCollation,
    appData: AppData,
    destroy: SqliteDestroyCallback<in AppData>?,
    callback: SqliteCollationCallback<in AppData>?
): SqliteResultCode = memScoped {
    commonCreateCollation(
        db = db,
        getDisposable = MemoryManager::stableRefDisposable,
        execute = { setPointer ->
            native.sqlite3_create_collation_v2(
                db.pointer,
                name.allocateUtf8(),
                encoding.value,
                setPointer(stableRefPointer(callback, appData, destroy).orNull).notNull,
                functionPointer(callback, ::CollationHandler),
                stableRefDisposer(callback, destroy)
            )
        }
    )
}

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
): SqliteResultCode = convertResultCode(db.withMemoryManager {
    memScoped {
        createFunction(appData, func, step, final, destroy) { fn, fnDestroy ->
            native.sqlite3_create_function_v2(
                db.pointer,
                name.allocateUtf8(),
                nArg,
                encoding.value,
                stableRefPointer(fn, appData, fnDestroy),
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
): SqliteResultCode = convertResultCode(db.withMemoryManager {
    memScoped {
        createVtabModule(module?.callbacks, appData) { vTabModule ->
            native.sqlite3_create_module_v2(
                db.pointer,
                name.allocateUtf8(),
                module?.pointer.notNull,
                stableRefPointer(vTabModule, appData, destroy),
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
): SqliteResultCode = convertResultCode(db.withMemoryManager {
    memScoped {
        createWindowFunction(appData, step, final, value, inverse, destroy) { fn, fnDestroy ->
            native.sqlite3_create_window_function(
                db.pointer,
                name.allocateUtf8(),
                nArg,
                encoding.value,
                stableRefPointer(fn, appData, fnDestroy),
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
    native.sqlite3_data_count(stmt.pointer)

public actual fun sqlite3_db_cacheflush(db: sqlite3): SqliteResultCode =
    convertResultCode(native.sqlite3_db_cacheflush(db.pointer))

public actual fun sqlite3_db_config(
    db: sqlite3,
    option: SqliteDbConfigOption,
): SqliteResultCode = commonDbConfig(
    option = option,
    bufferPointer = OpaqueBuffer::pointer,
    outParamConfig = {
        useParamMemScoped(state) { statePtr ->
            native.sqlite3_db_config
                .makeInvoker(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
                .apply(db.pointer, id, value, statePtr)
        }
    },
    nativeConfig = { id, values ->
        invokeVariadic(values, db::memory) { layouts, arguments ->
            native.sqlite3_db_config
                .makeInvoker(*layouts)
                .apply(db.pointer, id, *arguments)
        }
    }
)

public actual fun sqlite3_db_filename(
    db: sqlite3,
    database: String
): sqlite3_filename? = memScoped {
    native.sqlite3_db_filename(db.pointer, database.allocateUtf8())
}.wrapOrNull(::sqlite3_filename)

public actual fun sqlite3_db_handle(stmt: sqlite3_stmt): sqlite3? =
    native.sqlite3_db_handle(stmt.pointer).wrapOrNull(::sqlite3)

public actual fun sqlite3_db_name(
    db: sqlite3,
    index: Int
): String? = native.sqlite3_db_name(db.pointer, index)
    .toKStringFromUtf8OrNull()

public actual fun sqlite3_db_readonly(
    db: sqlite3,
    database: String
): SqliteDbReadonlyResult = convertDbReadonlyResult(memScoped {
    native.sqlite3_db_readonly(db.pointer, database.allocateUtf8())
})

public actual fun sqlite3_db_release_memory(db: sqlite3): SqliteResultCode =
    convertResultCode(native.sqlite3_db_release_memory(db.pointer))

public actual fun sqlite3_db_status(
    db: sqlite3,
    option: SqliteDbStatusOption,
    outCurrent: Int32OutputParam?,
    outHighwater: Int32OutputParam?,
    resetFlag: Int
): SqliteResultCode =
    convertResultCode(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
        native.sqlite3_db_status(db.pointer, option.id, curPtr, highPtr, resetFlag)
    })

public actual fun sqlite3_db_status64(
    db: sqlite3,
    option: SqliteDbStatusOption,
    outCurrent: Int64OutputParam?,
    outHighwater: Int64OutputParam?,
    resetFlag: Int
): SqliteResultCode =
    convertResultCode(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
        native.sqlite3_db_status64(db.pointer, option.id, curPtr, highPtr, resetFlag)
    })

public actual fun sqlite3_declare_vtab(
    db: sqlite3,
    sql: String
): SqliteResultCode = convertResultCode(memScoped {
    native.sqlite3_declare_vtab(db.pointer, sql.allocateUtf8())
})

public actual fun sqlite3_deserialize(
    db: sqlite3,
    database: String?,
    buffer: Buffer,
    dbSize: Long,
    bufferSize: Long,
    flags: SqliteDeserializeFlag?
): SqliteResultCode = convertResultCode(memScoped {
    native.sqlite3_deserialize(
        db.pointer,
        database.allocateUtf8(),
        buffer.pointer,
        dbSize,
        bufferSize,
        flags?.value ?: 0
    )
})

public actual fun sqlite3_drop_modules(
    db: sqlite3,
    keep: Array<String>?
): SqliteResultCode = convertResultCode(memScoped {
    native.sqlite3_drop_modules(db.pointer, keep?.let { names ->
        listOf(*names, null).toCStringArray()
    }.notNull)
})

public actual fun sqlite3_errcode(db: sqlite3): SqliteResultCode =
    convertResultCode(native.sqlite3_errcode(db.pointer))

public actual fun sqlite3_errmsg(db: sqlite3): String? =
    native.sqlite3_errmsg(db.pointer).toKStringFromUtf8OrNull()

public actual fun sqlite3_error_offset(db: sqlite3): Int =
    native.sqlite3_error_offset(db.pointer)

public actual fun sqlite3_errstr(resultCode: SqliteResultCode): String? =
    native.sqlite3_errstr(resultCode.code).toKStringFromUtf8OrNull()

public actual fun <AppData> sqlite3_exec(
    db: sqlite3,
    sql: String,
    outErrorMessage: Utf8OutputParam?,
    appData: AppData,
    callback: SqliteExecCallback<AppData>?
): SqliteResultCode = convertResultCode(useMemoryManager {
    memScoped {
        outErrorMessage.overriding(freeOnRead = true) {
            useParam(outErrorMessage) { errorMessagePtr ->
                native.sqlite3_exec(
                    db.pointer,
                    sql.allocateUtf8(),
                    functionPointer(callback, ::ExecHandler),
                    stableRefPointer(callback, appData),
                    errorMessagePtr
                )
            }
        }
    }
})

public actual fun sqlite3_expanded_sql(stmt: sqlite3_stmt): String? {
    val pointer = native.sqlite3_expanded_sql(stmt.pointer).orNull ?: return null
    val expandedSql = pointer.toKStringFromUtf8()
    native.sqlite3_free(pointer)
    return expandedSql
}

public actual fun sqlite3_extended_errcode(db: sqlite3): SqliteResultCode =
    convertResultCode(native.sqlite3_extended_errcode(db.pointer))

public actual fun sqlite3_extended_result_codes(
    db: sqlite3,
    enabled: Int
): SqliteResultCode = convertResultCode(native.sqlite3_extended_result_codes(db.pointer, enabled))

public actual fun sqlite3_file_control(
    db: sqlite3,
    database: String?,
    opcode: SqliteFileControlOpcode
): SqliteResultCode = memScoped {
    val name = database.allocateUtf8()

    fun controlParam(param: OutputParamBase<*>) = useParam(param) { paramPtr ->
        native.sqlite3_file_control(db.pointer, name, opcode.code, paramPtr)
    }

    commonFileControl(
        opcode = opcode,
        control = {
            native.sqlite3_file_control(db.pointer, name, opcode.code, NullPtr)
        },
        controlBuffer = { buffer ->
            native.sqlite3_file_control(db.pointer, name, opcode.code, buffer?.pointer.notNull)
        },
        controlVfs = ::controlParam,
        controlInt32 = ::controlParam,
        controlInt64 = ::controlParam,
        controlString = { param, freeOnRead ->
            param.overriding(freeOnRead = freeOnRead) {
                useParam(param) { paramPtr ->
                    native.sqlite3_file_control(db.pointer, name, opcode.code, paramPtr)
                }
            }
        }
    )
}

public actual fun sqlite3_filename_database(fileName: sqlite3_filename): String? =
    native.sqlite3_filename_database(fileName.pointer).toKStringFromUtf8OrNull()

public actual fun sqlite3_filename_journal(fileName: sqlite3_filename): String? =
    native.sqlite3_filename_journal(fileName.pointer).toKStringFromUtf8OrNull()

public actual fun sqlite3_filename_wal(fileName: sqlite3_filename): String? =
    native.sqlite3_filename_wal(fileName.pointer).toKStringFromUtf8OrNull()

public actual fun sqlite3_finalize(stmt: sqlite3_stmt): SqliteResultCode =
    stmt.deallocate { native.sqlite3_finalize(stmt.pointer) }

public actual fun sqlite3_free(buffer: Buffer): Unit =
    native.sqlite3_free(buffer.pointer)

public actual fun sqlite3_get_autocommit(db: sqlite3): Int =
    native.sqlite3_get_autocommit(db.pointer)

public actual fun sqlite3_hard_heap_limit64(limit: Long): Long =
    native.sqlite3_hard_heap_limit64(limit)

public actual fun sqlite3_initialize(): SqliteResultCode =
    convertResultCode(native.sqlite3_initialize())

public actual fun sqlite3_interrupt(db: sqlite3): Unit =
    native.sqlite3_interrupt(db.pointer)

public actual fun sqlite3_is_interrupted(db: sqlite3): Int =
    native.sqlite3_is_interrupted(db.pointer)

public actual fun sqlite3_key(
    db: sqlite3,
    key: ByteArray,
    nKey: Int,
): SqliteResultCode = convertResultCode(memScoped {
    native.sqlite3_key(db.pointer, key.allocate(nKey), nKey)
})

public actual fun sqlite3_key_v2(
    db: sqlite3,
    database: String,
    key: ByteArray,
    nKey: Int,
): SqliteResultCode = convertResultCode(memScoped {
    native.sqlite3_key_v2(db.pointer, database.allocateUtf8(), key.allocate(nKey), nKey)
})

public actual fun sqlite3_keyword_check(
    word: String
): Int = memScoped {
    val cWord = word.allocateUtf8()
    native.sqlite3_keyword_check(cWord, cWord.contentSize)
}

public actual fun sqlite3_keyword_count(): Int =
    native.sqlite3_keyword_count()

public actual fun sqlite3_keyword_name(
    index: Int,
    outName: Utf8OutputParam,
): SqliteResultCode = convertResultCode(memScoped {
    val outSize = Int32OutputParam(0)

    outName.overriding(customSize = outSize) {
        useParams(outSize, outName) { sizePtr, namePtr ->
            native.sqlite3_keyword_name(index, namePtr, sizePtr)
        }
    }
})

public actual fun sqlite3_last_insert_rowid(db: sqlite3): Long =
    native.sqlite3_last_insert_rowid(db.pointer)

public actual fun sqlite3_libversion(): String =
    native.sqlite3_libversion().toKStringFromUtf8()

public actual fun sqlite3_libversion_number(): Int =
    native.sqlite3_libversion_number()

public actual fun sqlite3_limit(
    db: sqlite3,
    id: SqliteRuntimeLimit,
    newVal: Int
): Int = native.sqlite3_limit(db.pointer, id.id, newVal)

public actual fun sqlite3_log(
    errorCode: Int,
    message: String
): Unit = memScoped {
    native.sqlite3_log.makeInvoker().apply(errorCode, message.allocateUtf8())
}

public actual fun sqlite3_malloc(size: Int): Buffer? =
    native.sqlite3_malloc(size).orNull?.let { Buffer.from(it, size.toLong()) }

public actual fun sqlite3_malloc64(size: Long): Buffer? =
    native.sqlite3_malloc64(size).orNull?.let { Buffer.from(it, size) }

public actual fun sqlite3_memory_used(): Long =
    native.sqlite3_memory_used()

public actual fun sqlite3_memory_highwater(resetFlag: Int): Long =
    native.sqlite3_memory_highwater(resetFlag)

public actual fun sqlite3_msize(buffer: Buffer): ULong =
    native.sqlite3_msize(buffer.pointer).toULong()

public actual fun sqlite3_next_stmt(
    db: sqlite3,
    stmt: sqlite3_stmt?
): sqlite3_stmt? = native.sqlite3_next_stmt(db.pointer, stmt?.pointer.notNull)
    .wrapOrNull(::sqlite3_stmt)

public actual fun sqlite3_open(
    fileName: String,
    outDb: sqlite3.OutputParam
): SqliteResultCode = convertResultCode(memScoped {
    useParam(outDb) { dbPtr ->
        native.sqlite3_open(fileName.allocateUtf8(), dbPtr)
    }
})

public actual fun sqlite3_open_v2(
    fileName: String,
    outDb: sqlite3.OutputParam,
    flags: SqliteOpenFlag.Db,
    vfs: String?
): SqliteResultCode = convertResultCode(memScoped {
    useParam(outDb) { dbPtr ->
        native.sqlite3_open_v2(fileName.allocateUtf8(), dbPtr, flags.value, vfs.allocateUtf8())
    }
})

public actual fun sqlite3_overload_function(
    db: sqlite3,
    name: String,
    nArg: Int
): SqliteResultCode = convertResultCode(memScoped {
    native.sqlite3_overload_function(db.pointer, name.allocateUtf8(), nArg)
})

public actual fun sqlite3_prepare_v2(
    db: sqlite3,
    sql: ByteArray,
    maxBytes: Int,
    outStmt: sqlite3_stmt.OutputParam,
    outOffset: Int32OutputParam?
): SqliteResultCode = convertResultCode(memScoped {
    useParams(outStmt, outOffset) { stmtPtr, offsetPtr ->
        native.ksqlite_prepare_v2(db.pointer, sql.allocate(maxBytes), maxBytes, stmtPtr, offsetPtr)
    }
})

public actual fun sqlite3_prepare_v2(
    db: sqlite3,
    sql: String,
    outStmt: sqlite3_stmt.OutputParam
): SqliteResultCode = convertResultCode(memScoped {
    useParam(outStmt) { stmtPtr ->
        val cSql = sql.allocateUtf8()
        native.sqlite3_prepare_v2(db.pointer, cSql, cSql.contentSize, stmtPtr, NullPtr)
    }
})

public actual fun sqlite3_prepare_v3(
    db: sqlite3,
    sql: ByteArray,
    maxBytes: Int,
    flags: SqlitePrepareFlag?,
    outStmt: sqlite3_stmt.OutputParam,
    outOffset: Int32OutputParam?
): SqliteResultCode = convertResultCode(memScoped {
    useParams(outStmt, outOffset) { stmtPtr, offsetPtr ->
        val prepFlags = flags?.value ?: 0

        native.ksqlite_prepare_v3(
            db.pointer,
            sql.allocate(maxBytes),
            maxBytes,
            prepFlags,
            stmtPtr,
            offsetPtr
        )
    }
})

public actual fun sqlite3_prepare_v3(
    db: sqlite3,
    sql: String,
    flags: SqlitePrepareFlag?,
    outStmt: sqlite3_stmt.OutputParam
): SqliteResultCode = convertResultCode(memScoped {
    useParam(outStmt) { stmtPtr ->
        val cSql = sql.allocateUtf8()
        val prepFlags = flags?.value ?: 0
        native.sqlite3_prepare_v3(db.pointer, cSql, cSql.contentSize, prepFlags, stmtPtr, NullPtr)
    }
})

public actual fun sqlite3_preupdate_blobwrite(db: sqlite3): Int =
    native.sqlite3_preupdate_blobwrite(db.pointer)

public actual fun sqlite3_preupdate_count(db: sqlite3): Int =
    native.sqlite3_preupdate_count(db.pointer)

public actual fun sqlite3_preupdate_depth(db: sqlite3): Int =
    native.sqlite3_preupdate_depth(db.pointer)

public actual fun <AppData> sqlite3_preupdate_hook(
    db: sqlite3,
    appData: AppData,
    callback: SqlitePreupdateHookCallback<AppData>?
): Unit = db.withMemoryManager {
    native.sqlite3_preupdate_hook(
        db.pointer,
        functionPointer(callback, ::PreupdateHookHandler),
        keyedStableRefPointer(KEY_PREUPDATE_HOOK, callback, appData)
    )
}

public actual fun sqlite3_preupdate_new(
    db: sqlite3,
    index: Int,
    outValue: sqlite3_value.OutputParam
): SqliteResultCode = convertResultCode(useParamMemScoped(outValue) { valuePtr ->
    native.sqlite3_preupdate_new(db.pointer, index, valuePtr)
})

public actual fun sqlite3_preupdate_old(
    db: sqlite3,
    index: Int,
    outValue: sqlite3_value.OutputParam
): SqliteResultCode = convertResultCode(useParamMemScoped(outValue) { valuePtr ->
    native.sqlite3_preupdate_old(db.pointer, index, valuePtr)
})

public actual fun <AppData> sqlite3_progress_handler(
    db: sqlite3,
    nOps: Int,
    appData: AppData,
    callback: SqliteProgressHandlerCallback<AppData>?
): Unit = db.withMemoryManager {
    native.sqlite3_progress_handler(
        db.pointer,
        nOps,
        functionPointer(callback, ::ProgressHandlerHandler),
        keyedStableRefPointer(KEY_PROGRESS_HANDLER, callback, appData)
    )
}

public actual fun sqlite3_randomness(
    size: Int,
    buffer: Buffer
): Unit = native.sqlite3_randomness(size, buffer.pointer)

public actual fun sqlite3_realloc(
    buffer: Buffer,
    size: Int
): Buffer? = Buffer.from(
    pointer = native.sqlite3_realloc(buffer.pointer, size),
    size = size.toLong()
)

public actual fun sqlite3_realloc64(
    buffer: Buffer,
    size: Long
): Buffer? = Buffer.from(
    pointer = native.sqlite3_realloc64(buffer.pointer, size),
    size = size
)

public actual fun sqlite3_rekey(
    db: sqlite3,
    key: ByteArray,
    nKey: Int,
): SqliteResultCode = convertResultCode(memScoped {
    native.sqlite3_rekey(db.pointer, key.allocate(nKey), nKey)
})

public actual fun sqlite3_rekey_v2(
    db: sqlite3,
    dbName: String,
    key: ByteArray,
    nKey: Int,
): SqliteResultCode = convertResultCode(memScoped {
    native.sqlite3_rekey_v2(db.pointer, dbName.allocateUtf8(), key.allocate(nKey), nKey)
})

public actual fun sqlite3_release_memory(size: Int): Int =
    native.sqlite3_release_memory(size)

public actual fun sqlite3_reset(stmt: sqlite3_stmt): SqliteResultCode =
    convertResultCode(native.sqlite3_reset(stmt.pointer))

public actual fun sqlite3_reset_auto_extension(): Unit =
    autoExtensionReset { native.sqlite3_reset_auto_extension() }

public actual fun sqlite3_result_blob(
    context: sqlite3_context,
    bytes: ByteArray,
    size: Int,
    destroy: SqliteDestroyCallback<ByteArray>?
): Unit = native.sqlite3_result_blob(
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
): Unit = native.sqlite3_result_blob64(
    context.pointer,
    buffer.pointer,
    size,
    bufferDisposer(buffer, destroy)
)

public actual fun sqlite3_result_double(
    context: sqlite3_context,
    value: Double
): Unit = native.sqlite3_result_double(context.pointer, value)

public actual fun sqlite3_result_error(
    context: sqlite3_context,
    message: String
): Unit = memScoped {
    val cMessage = message.allocateUtf8()
    native.sqlite3_result_error(context.pointer, cMessage, cMessage.contentSize)
}

public actual fun sqlite3_result_error_code(
    context: sqlite3_context,
    result: SqliteResultCode.Failure
): Unit = native.sqlite3_result_error_code(context.pointer, result.code)

public actual fun sqlite3_result_error_nomem(context: sqlite3_context): Unit =
    native.sqlite3_result_error_nomem(context.pointer)

public actual fun sqlite3_result_error_toobig(context: sqlite3_context): Unit =
    native.sqlite3_result_error_toobig(context.pointer)

public actual fun sqlite3_result_int(
    context: sqlite3_context,
    value: Int
): Unit = native.sqlite3_result_int(context.pointer, value)

public actual fun sqlite3_result_int64(
    context: sqlite3_context,
    value: Long
): Unit = native.sqlite3_result_int64(context.pointer, value)

public actual fun sqlite3_result_null(context: sqlite3_context): Unit =
    native.sqlite3_result_null(context.pointer)

public actual fun <Data> sqlite3_result_pointer(
    context: sqlite3_context,
    data: Data,
    type: String?,
    destroy: SqliteDestroyCallback<Data>?
): Unit = with(globalMemory) {
    // Use globalMemory because of lack of information within sqlite3_value_pointer()
    allocateNamedPointer(type, destroy) { ptr, ptrDestroy ->
        native.sqlite3_result_pointer(
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
): Unit = native.sqlite3_result_subtype(context.pointer, subtype.toInt())

public actual fun sqlite3_result_text(
    context: sqlite3_context,
    value: String
): Unit = memScoped {
    val cText = value.allocateUtf8()
    native.sqlite3_result_text(context.pointer, cText, cText.contentSize, native.SQLITE_TRANSIENT())
}

public actual fun sqlite3_result_text64(
    context: sqlite3_context,
    buffer: Buffer,
    size: Long,
    encoding: SqliteTextEncoding.ResultText,
    destroy: SqliteDestroyCallback<Buffer>?
): Unit = native.sqlite3_result_text64(
    context.pointer,
    buffer.pointer,
    size,
    bufferDisposer(buffer, destroy),
    encoding.value.toByte()
)

public actual fun sqlite3_result_value(
    context: sqlite3_context,
    value: sqlite3_value,
): Unit = native.sqlite3_result_value(context.pointer, value.pointer)

public actual fun sqlite3_result_zeroblob(
    context: sqlite3_context,
    size: Int
): Unit = native.sqlite3_result_zeroblob(context.pointer, size)

public actual fun sqlite3_result_zeroblob64(
    context: sqlite3_context,
    size: ULong
): SqliteResultCode =
    convertResultCode(native.sqlite3_result_zeroblob64(context.pointer, size.toLong()))

public actual fun <AppData> sqlite3_rollback_hook(
    db: sqlite3,
    appData: AppData,
    callback: SqliteRollbackHookCallback<AppData>?
): Unit = db.withMemoryManager {
    native.sqlite3_rollback_hook(
        db.pointer,
        functionPointer(callback, ::RollbackHookHandler),
        keyedStableRefPointer(KEY_ROLLBACK_HOOK, callback, appData)
    )
}

public actual fun <AppData> sqlite3_set_authorizer(
    db: sqlite3,
    appData: AppData,
    callback: SqliteAuthorizerCallback<AppData>?
): SqliteResultCode = convertResultCode(db.withMemoryManager {
    native.sqlite3_set_authorizer(
        db.pointer,
        functionPointer(callback, ::AuthorizerHandler),
        keyedStableRefPointer(KEY_SET_AUTHORIZER, callback, appData)
    )
})

public actual fun sqlite3_set_errmsg(
    db: sqlite3,
    errorCode: SqliteResultCode,
    errorMessage: String?
): SqliteResultCode = convertResultCode(memScoped {
    native.sqlite3_set_errmsg(db.pointer, errorCode.code, errorMessage.allocateUtf8())
})

public actual fun sqlite3_set_last_insert_rowid(
    db: sqlite3,
    rowId: Long
): Unit = native.sqlite3_set_last_insert_rowid(db.pointer, rowId)

public actual fun sqlite3_shutdown(): SqliteResultCode =
    convertResultCode(native.sqlite3_shutdown())

public actual fun sqlite3_snapshot_cmp(
    snapshot1: sqlite3_snapshot,
    snapshot2: sqlite3_snapshot
): Int = native.sqlite3_snapshot_cmp(snapshot1.pointer, snapshot2.pointer)

public actual fun sqlite3_snapshot_free(snapshot: sqlite3_snapshot): Unit =
    native.sqlite3_snapshot_free(snapshot.pointer)

public actual fun sqlite3_snapshot_get(
    db: sqlite3,
    name: String?,
    outSnapshot: sqlite3_snapshot.OutputParam
): SqliteResultCode = convertResultCode(memScoped {
    useParam(outSnapshot) { snapshotPtr ->
        native.sqlite3_snapshot_get(db.pointer, name.allocateUtf8(), snapshotPtr)
    }
})

public actual fun sqlite3_snapshot_open(
    db: sqlite3,
    name: String?,
    snapshot: sqlite3_snapshot
): SqliteResultCode = convertResultCode(memScoped {
    native.sqlite3_snapshot_open(db.pointer, name.allocateUtf8(), snapshot.pointer)
})

public actual fun sqlite3_snapshot_recover(
    db: sqlite3,
    name: String?
): SqliteResultCode = convertResultCode(memScoped {
    native.sqlite3_snapshot_recover(db.pointer, name.allocateUtf8())
})

public actual fun sqlite3_soft_heap_limit64(limit: Long): Long =
    native.sqlite3_soft_heap_limit64(limit)

public actual fun sqlite3_sourceid(): String =
    native.sqlite3_sourceid().toKStringFromUtf8()

public actual fun sqlite3_sql(stmt: sqlite3_stmt): String =
    native.sqlite3_sql(stmt.pointer).toKStringFromUtf8()

public actual fun sqlite3_status(
    option: SqliteStatusOption,
    outCurrent: Int32OutputParam,
    outHighwater: Int32OutputParam,
    resetFlag: Int
): SqliteResultCode =
    convertResultCode(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
        native.sqlite3_status(option.id, curPtr, highPtr, resetFlag)
    })

public actual fun sqlite3_status64(
    option: SqliteStatusOption,
    outCurrent: Int64OutputParam,
    outHighwater: Int64OutputParam,
    resetFlag: Int
): SqliteResultCode =
    convertResultCode(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
        native.sqlite3_status64(option.id, curPtr, highPtr, resetFlag)
    })

public actual fun sqlite3_step(stmt: sqlite3_stmt): SqliteResultCode =
    convertResultCode(native.sqlite3_step(stmt.pointer))

public actual fun sqlite3_stmt_busy(stmt: sqlite3_stmt): Int =
    native.sqlite3_stmt_busy(stmt.pointer)

public actual fun sqlite3_stmt_explain(
    stmt: sqlite3_stmt,
    mode: SqliteExplainMode
): SqliteResultCode = convertResultCode(native.sqlite3_stmt_explain(stmt.pointer, mode.id))

public actual fun sqlite3_stmt_isexplain(stmt: sqlite3_stmt): SqliteExplainMode =
    convertExplainMode(native.sqlite3_stmt_isexplain(stmt.pointer))

public actual fun sqlite3_stmt_readonly(stmt: sqlite3_stmt): Int =
    native.sqlite3_stmt_readonly(stmt.pointer)

public actual fun sqlite3_stmt_status(
    stmt: sqlite3_stmt,
    counter: SqliteStatementStatusCounter,
    resetFlag: Int
): Int = native.sqlite3_stmt_status(stmt.pointer, counter.id, resetFlag)

public actual fun sqlite3_strglob(
    globPattern: String,
    input: String
): Int = memScoped {
    native.sqlite3_strglob(globPattern.allocateUtf8(), input.allocateUtf8())
}

public actual fun sqlite3_stricmp(
    first: String,
    second: String
): Int = memScoped {
    native.sqlite3_stricmp(first.allocateUtf8(), second.allocateUtf8())
}

public actual fun sqlite3_strlike(
    likePattern: String,
    input: String,
    escapeCharacter: Char
): Int = memScoped {
    native.sqlite3_strlike(likePattern.allocateUtf8(), input.allocateUtf8(), escapeCharacter.code)
}

public actual fun sqlite3_strnicmp(
    first: String,
    second: String,
    maxBytes: Int
): Int = memScoped {
    native.sqlite3_strnicmp(first.allocateUtf8(), second.allocateUtf8(), maxBytes)
}

public actual fun sqlite3_system_errno(db: sqlite3): Int =
    native.sqlite3_system_errno(db.pointer)

public actual fun sqlite3_table_column_metadata(
    db: sqlite3,
    dbName: String?,
    tableName: String,
    columnName: String,
    outDataType: Utf8OutputParam?,
    outCollationSequence: Utf8OutputParam?,
    outNotNull: Int32OutputParam?,
    outPrimaryKey: Int32OutputParam?,
    outAutoIncrement: Int32OutputParam?
): SqliteResultCode = convertResultCode(memScoped {
    val dataTypePtr = outDataType?.attach(this)
    val collationNamePtr = outCollationSequence?.attach(this)
    val notNullPtr = outNotNull?.attach(this)
    val primaryKeyPtr = outPrimaryKey?.attach(this)
    val autoIncrementPtr = outAutoIncrement?.attach(this)

    try {
        native.sqlite3_table_column_metadata(
            db.pointer,
            dbName.allocateUtf8(),
            tableName.allocateUtf8(),
            columnName.allocateUtf8(),
            dataTypePtr.notNull,
            collationNamePtr.notNull,
            notNullPtr.notNull,
            primaryKeyPtr.notNull,
            autoIncrementPtr.notNull
        )
    } finally {
        dataTypePtr?.let(outDataType::detach)
        collationNamePtr?.let(outCollationSequence::detach)
        notNullPtr?.let(outNotNull::detach)
        primaryKeyPtr?.let(outPrimaryKey::detach)
        autoIncrementPtr?.let(outAutoIncrement::detach)
    }
})

public actual fun sqlite3_threadsafe(): Int =
    native.sqlite3_threadsafe()

public actual fun sqlite3_total_changes(db: sqlite3): Int =
    native.sqlite3_total_changes(db.pointer)

public actual fun sqlite3_total_changes64(db: sqlite3): Long =
    native.sqlite3_total_changes64(db.pointer)

public actual fun <AppData> sqlite3_trace_v2(
    db: sqlite3,
    mask: SqliteTraceEventCode?,
    appData: AppData,
    callback: SqliteTraceCallback<AppData>?
): SqliteResultCode = convertResultCode(db.withMemoryManager {
    native.sqlite3_trace_v2(
        db.pointer,
        mask?.value ?: 0,
        functionPointer(callback, ::TraceHandler),
        keyedStableRefPointer(KEY_TRACE, callback, appData)
    )
})

public actual fun sqlite3_txn_state(
    db: sqlite3,
    database: String?
): SqliteTransactionState? = convertTransactionState(memScoped {
    native.sqlite3_txn_state(db.pointer, database.allocateUtf8())
})

public actual fun <AppData> sqlite3_update_hook(
    db: sqlite3,
    appData: AppData,
    callback: SqliteUpdateHookCallback<AppData>?
): Unit = db.withMemoryManager {
    native.sqlite3_update_hook(
        db.pointer,
        functionPointer(callback, ::UpdateHookHandler),
        keyedStableRefPointer(KEY_UPDATE_HOOK, callback, appData)
    )
}

public actual fun sqlite3_uri_boolean(
    fileName: sqlite3_filename,
    parameter: String,
    default: Int
): Int = memScoped {
    native.sqlite3_uri_boolean(fileName.pointer, parameter.allocateUtf8(), default)
}

public actual fun sqlite3_uri_int64(
    fileName: sqlite3_filename,
    parameter: String,
    default: Long
): Long = memScoped {
    native.sqlite3_uri_int64(fileName.pointer, parameter.allocateUtf8(), default)
}

public actual fun sqlite3_uri_key(
    fileName: sqlite3_filename,
    index: Int
): String? = memScoped {
    native.sqlite3_uri_key(fileName.pointer, index)
}.toKStringFromUtf8OrNull()

public actual fun sqlite3_uri_parameter(
    fileName: sqlite3_filename,
    parameter: String
): String? = memScoped {
    native.sqlite3_uri_parameter(fileName.pointer, parameter.allocateUtf8())
}.toKStringFromUtf8OrNull()

public actual fun sqlite3_value_blob(value: sqlite3_value): ByteArray? = commonValueByteArray(
    value = value,
    pointer = native.sqlite3_value_blob(value.pointer),
    toByteArray = { pointer, size ->
        pointer
            .reinterpret(size.toLong())
            .toArray(ValueLayout.JAVA_BYTE)
    }
)

public actual fun sqlite3_value_bytes(value: sqlite3_value): Int =
    native.sqlite3_value_bytes(value.pointer)

public actual fun sqlite3_value_double(value: sqlite3_value): Double =
    native.sqlite3_value_double(value.pointer)

public actual fun sqlite3_value_dup(value: sqlite3_value): sqlite3_value? =
    native.sqlite3_value_dup(value.pointer).wrapOrNull(::sqlite3_value)

public actual fun sqlite3_value_encoding(value: sqlite3_value): SqliteTextEncoding.ValueEncoding =
    convertTextEncoding(native.sqlite3_value_encoding(value.pointer))

public actual fun sqlite3_value_free(value: sqlite3_value): Unit =
    native.sqlite3_value_free(value.pointer)

public actual fun sqlite3_value_frombind(value: sqlite3_value): Int =
    native.sqlite3_value_frombind(value.pointer)

public actual fun sqlite3_value_int(value: sqlite3_value): Int =
    native.sqlite3_value_int(value.pointer)

public actual fun sqlite3_value_int64(value: sqlite3_value): Long =
    native.sqlite3_value_int64(value.pointer)

public actual fun sqlite3_value_nochange(value: sqlite3_value): Int =
    native.sqlite3_value_nochange(value.pointer)

public actual fun sqlite3_value_numeric_type(value: sqlite3_value): SqliteDataType =
    convertDataType(native.sqlite3_value_numeric_type(value.pointer))

public actual fun sqlite3_value_subtype(value: sqlite3_value): UInt =
    native.sqlite3_value_subtype(value.pointer).toUInt()

public actual fun sqlite3_value_text(value: sqlite3_value): String? =
    native.sqlite3_value_text(value.pointer).toKStringFromUtf8OrNull()

public actual fun sqlite3_value_type(value: sqlite3_value): SqliteDataType =
    convertDataType(native.sqlite3_value_type(value.pointer))

public actual fun sqlite3_vfs_find(name: String?): sqlite3_vfs? = memScoped {
    native.sqlite3_vfs_find(name.allocateUtf8())
}.wrapOrNull(::sqlite3_vfs)

public actual fun sqlite3_vfs_register(
    vfs: sqlite3_vfs,
    makeDefault: Int
): SqliteResultCode = convertResultCode(native.sqlite3_vfs_register(vfs.pointer, makeDefault))

public actual fun sqlite3_vfs_unregister(vfs: sqlite3_vfs): SqliteResultCode =
    convertResultCode(native.sqlite3_vfs_unregister(vfs.pointer))

public actual fun sqlite3_vtab_collation(
    info: sqlite3_index_info,
    index: Int
): String = native.sqlite3_vtab_collation(info.pointer, index)
    .toKStringFromUtf8()

public actual fun sqlite3_vtab_config(
    db: sqlite3,
    option: SqliteVtabConfigOption
): SqliteResultCode = commonVtabConfig(option) { id, values ->
    invokeVariadic(values, db::memory) { layouts, arguments ->
        native.sqlite3_vtab_config
            .makeInvoker(*layouts)
            .apply(db.pointer, id, *arguments)
    }
}

public actual fun sqlite3_vtab_distinct(info: sqlite3_index_info): Int =
    native.sqlite3_vtab_distinct(info.pointer)

public actual fun sqlite3_vtab_in(
    info: sqlite3_index_info,
    index: Int,
    handle: Int
): Int = native.sqlite3_vtab_in(info.pointer, index, handle)

public actual fun sqlite3_vtab_in_first(
    value: sqlite3_value,
    outValue: sqlite3_value.OutputParam?
): SqliteResultCode = convertResultCode(useParamMemScoped(outValue) { valuePtr ->
    native.sqlite3_vtab_in_first(value.pointer, valuePtr)
})

public actual fun sqlite3_vtab_in_next(
    value: sqlite3_value,
    outValue: sqlite3_value.OutputParam?
): SqliteResultCode = convertResultCode(useParamMemScoped(outValue) { valuePtr ->
    native.sqlite3_vtab_in_next(value.pointer, valuePtr)
})

public actual fun sqlite3_vtab_nochange(context: sqlite3_context): Int =
    native.sqlite3_vtab_nochange(context.pointer)

public actual fun sqlite3_vtab_on_conflict(db: sqlite3): SqliteConflictResolutionMode =
    convertConflictResolutionMode(native.sqlite3_vtab_on_conflict(db.pointer))

public actual fun sqlite3_vtab_rhs_value(
    info: sqlite3_index_info,
    index: Int,
    outValue: sqlite3_value.OutputParam?
): SqliteResultCode = convertResultCode(useParamMemScoped(outValue) { valuePtr ->
    native.sqlite3_vtab_rhs_value(info.pointer, index, valuePtr)
})

public actual fun sqlite3_wal_autocheckpoint(
    db: sqlite3,
    nFrame: Int
): SqliteResultCode = convertResultCode(native.sqlite3_wal_autocheckpoint(db.pointer, nFrame))

public actual fun sqlite3_wal_checkpoint(
    db: sqlite3,
    database: String?
): SqliteResultCode = convertResultCode(memScoped {
    native.sqlite3_wal_checkpoint(db.pointer, database.allocateUtf8())
})

public actual fun sqlite3_wal_checkpoint_v2(
    db: sqlite3,
    database: String?,
    mode: SqliteCheckpointMode,
    outNLog: Int32OutputParam?,
    outNCkpt: Int32OutputParam?
): SqliteResultCode = convertResultCode(memScoped {
    useParams(outNLog, outNCkpt) { nLogPtr, nCkptPtr ->
        native.sqlite3_wal_checkpoint_v2(
            db.pointer,
            database.allocateUtf8(),
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
    native.sqlite3_wal_hook(
        db.pointer,
        functionPointer(callback, ::WalHookHandler),
        keyedStableRefPointer(KEY_WAL_HOOK, callback, appData)
    )
}