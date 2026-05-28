@file:JvmName("Ksqlite")
@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi

import ksqlite.capi.VariadicValue.OfPointer
import ksqlite.capi.callbacks.Sqlite3AutoExtensionCallback
import ksqlite.capi.callbacks.Sqlite3AutoVacuumPagesCallback
import ksqlite.capi.callbacks.Sqlite3BusyHandlerCallback
import ksqlite.capi.callbacks.Sqlite3CollationCompareCallback
import ksqlite.capi.callbacks.Sqlite3CollationNeededCallback
import ksqlite.capi.callbacks.Sqlite3CommitHookCallback
import ksqlite.capi.callbacks.Sqlite3DestroyCallback
import ksqlite.capi.callbacks.Sqlite3ExecCallback
import ksqlite.capi.callbacks.Sqlite3FunctionFinalCallback
import ksqlite.capi.callbacks.Sqlite3FunctionFuncCallback
import ksqlite.capi.callbacks.Sqlite3FunctionInverseCallback
import ksqlite.capi.callbacks.Sqlite3FunctionStepCallback
import ksqlite.capi.callbacks.Sqlite3FunctionValueCallback
import ksqlite.capi.callbacks.Sqlite3PreupdateHookCallback
import ksqlite.capi.callbacks.Sqlite3ProgressHandlerCallback
import ksqlite.capi.callbacks.Sqlite3RollbackHookCallback
import ksqlite.capi.callbacks.Sqlite3SetAuthorizerCallback
import ksqlite.capi.callbacks.Sqlite3TraceCallback
import ksqlite.capi.callbacks.Sqlite3UpdateHookCallback
import ksqlite.capi.callbacks.Sqlite3WalHookCallback
import ksqlite.capi.handlers.AutoVacuumPagesHandler
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
import ksqlite.capi.handlers.SetAuthorizerHandler
import ksqlite.capi.handlers.SharedAutoExtensionHandler
import ksqlite.capi.handlers.TraceHandler
import ksqlite.capi.handlers.UpdateHookHandler
import ksqlite.capi.handlers.WalHookHandler
import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.allocateUtf8
import ksqlite.capi.memory.backing
import ksqlite.capi.memory.bufferDisposer
import ksqlite.capi.memory.deallocateNullable
import ksqlite.capi.memory.globalDisposer
import ksqlite.capi.memory.globalMemory
import ksqlite.capi.memory.memScoped
import ksqlite.capi.memory.memory
import ksqlite.capi.memory.notNull
import ksqlite.capi.memory.orNull
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.memory.useMemoryManager
import ksqlite.capi.memory.withMemoryManager
import ksqlite.capi.types.Int32OutputParam
import ksqlite.capi.types.Int64OutputParam
import ksqlite.capi.types.Sqlite3BlobOpenFlag
import ksqlite.capi.types.Sqlite3BlobOutputParam
import ksqlite.capi.types.Sqlite3CheckpointMode
import ksqlite.capi.types.Sqlite3CompleteResult
import ksqlite.capi.types.Sqlite3ConfigOption
import ksqlite.capi.types.Sqlite3DataType
import ksqlite.capi.types.Sqlite3DbConfigOption
import ksqlite.capi.types.Sqlite3DbStatusOption
import ksqlite.capi.types.Sqlite3DeserializeFlag
import ksqlite.capi.types.Sqlite3ExplainMode
import ksqlite.capi.types.Sqlite3FileControlOpcode
import ksqlite.capi.types.Sqlite3OpenFlag
import ksqlite.capi.types.Sqlite3Limit
import ksqlite.capi.types.Sqlite3OutputParam
import ksqlite.capi.types.Sqlite3PrepareFlag
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.Sqlite3SerializeFlag
import ksqlite.capi.types.Sqlite3SnapshotOutputParam
import ksqlite.capi.types.Sqlite3StatementStatusCounter
import ksqlite.capi.types.Sqlite3StatusOption
import ksqlite.capi.types.Sqlite3StmtOutputParam
import ksqlite.capi.types.Sqlite3TextEncoding
import ksqlite.capi.types.Sqlite3TraceCode
import ksqlite.capi.types.Sqlite3TransactionState
import ksqlite.capi.types.Sqlite3ValueOutputParam
import ksqlite.capi.types.Sqlite3VirtualTableConfigOption
import ksqlite.capi.types.Utf8OutputParam
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_backup
import ksqlite.capi.types.sqlite3_blob
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_filename
import ksqlite.capi.types.sqlite3_index_info
import ksqlite.capi.types.sqlite3_snapshot
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.capi.types.sqlite3_value
import ksqlite.capi.types.sqlite3_vfs
import ksqlite.capi.types.useParam
import ksqlite.capi.types.useParamMemScoped
import ksqlite.capi.types.useParams
import ksqlite.capi.types.useParamsMemScoped
import ksqlite.ksqliteLoadLibrary
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import ksqlite.sqlite3 as native

///////////////////////////////////////////////////////////////////////////
// Library
///////////////////////////////////////////////////////////////////////////

/**
 * Workaround to load the native library at file level.
 */
@Suppress("unused")
private val nativeInit = run {
    ksqliteLoadLibrary()
}

///////////////////////////////////////////////////////////////////////////
// Helpers
///////////////////////////////////////////////////////////////////////////

/**
 * Invokes a function accepting a variadic parameter.
 * String parameter are allocated within [manager].
 */
private inline fun <Result> invokeVariadic(
    values: Array<out VariadicValue<MemorySegment>?>,
    manager: () -> MemoryManager,
    invoke: (layouts: Array<out MemoryLayout>, arguments: Array<out Any>) -> Result
): Result {
    val layouts = Array(values.size) { index ->
        when (values[index]) {
            is OfInt, is OfUInt -> ValueLayout.JAVA_INT
            is OfLong -> ValueLayout.JAVA_LONG
            is OfPointer, is OfString, null -> ValueLayout.ADDRESS
        }
    }

    val arguments = Array(values.size) { index ->
        when (val value = values[index]) {
            null -> MemorySegment.NULL
            !is OfString -> value.value
            else -> manager().keyedStringPointer(value.key, value.value)
        }
    }

    return invoke(layouts, arguments)
}

///////////////////////////////////////////////////////////////////////////
// Functions
///////////////////////////////////////////////////////////////////////////

public actual fun sqlite3_auto_extension(callback: Sqlite3AutoExtensionCallback): Sqlite3Result =
    autoExtensionRegister(callback) { native.ksqlite_auto_extension(SharedAutoExtensionHandler) }

public actual fun <AppData> sqlite3_autovacuum_pages(
    db: sqlite3,
    appData: AppData,
    destroy: Sqlite3DestroyCallback<AppData>?,
    callback: Sqlite3AutoVacuumPagesCallback<AppData>?
): Sqlite3Result = convertResult(db.withMemoryManager {
    native.sqlite3_autovacuum_pages(
        db.pointer,
        functionPointer(callback, ::AutoVacuumPagesHandler),
        keyedStableRefPointer(KEY_AUTOVACUUM_PAGES, callback, appData, destroy),
        stableRefDisposer(callback, destroy)
    )
})

public actual fun sqlite3_backup_finish(backup: sqlite3_backup): Sqlite3Result =
    convertResult(native.sqlite3_backup_finish(backup.pointer))

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
    ).orNull?.let(::sqlite3_backup)
}

public actual fun sqlite3_backup_pagecount(backup: sqlite3_backup): Int =
    native.sqlite3_backup_pagecount(backup.pointer)

public actual fun sqlite3_backup_remaining(backup: sqlite3_backup): Int =
    native.sqlite3_backup_remaining(backup.pointer)

public actual fun sqlite3_backup_step(
    backup: sqlite3_backup,
    nPage: Int
): Sqlite3Result = convertResult(native.sqlite3_backup_step(backup.pointer, nPage))

public actual fun sqlite3_bind_blob(
    stmt: sqlite3_stmt,
    index: Int,
    bytes: ByteArray,
    size: Int,
    destroy: Sqlite3DestroyCallback<ByteArray>?
): Sqlite3Result = convertResult(
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
    destroy: Sqlite3DestroyCallback<Buffer>?
): Sqlite3Result = convertResult(
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
): Sqlite3Result = convertResult(native.sqlite3_bind_double(stmt.pointer, index, value))

public actual fun sqlite3_bind_int(
    stmt: sqlite3_stmt,
    index: Int,
    value: Int
): Sqlite3Result = convertResult(native.sqlite3_bind_int(stmt.pointer, index, value))

public actual fun sqlite3_bind_int64(
    stmt: sqlite3_stmt,
    index: Int,
    value: Long
): Sqlite3Result = convertResult(native.sqlite3_bind_int64(stmt.pointer, index, value))

public actual fun sqlite3_bind_null(
    stmt: sqlite3_stmt,
    index: Int
): Sqlite3Result = convertResult(native.sqlite3_bind_null(stmt.pointer, index))

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
    destroy: Sqlite3DestroyCallback<Data>?
): Sqlite3Result = convertResult(with(globalMemory) {
    // Use globalMemory because of lack of information within sqlite3_value_pointer()
    allocateNamedPointer(type, destroy) { ptr, ptrDestroy ->
        native.sqlite3_bind_pointer(
            stmt.pointer,
            index,
            stableRefPointer(ptr, data, ptrDestroy),
            ptr.name.notNull,
            stableRefDisposer(ptr, ptrDestroy)
        )
    }
})

public actual fun sqlite3_bind_text(
    stmt: sqlite3_stmt,
    index: Int,
    text: String
): Sqlite3Result = convertResult(memScoped {
    val cText = text.allocateUtf8()
    val nByte = cText.byteSize().toInt()
    native.sqlite3_bind_text(stmt.pointer, index, cText, nByte, SqliteTransient)
})

public actual fun sqlite3_bind_text64(
    stmt: sqlite3_stmt,
    index: Int,
    buffer: Buffer,
    size: Long,
    encoding: Sqlite3TextEncoding.Set1,
    destroy: Sqlite3DestroyCallback<Buffer>?
): Sqlite3Result = convertResult(
    native.sqlite3_bind_text64(
        stmt.pointer,
        index,
        buffer.pointer,
        size,
        bufferDisposer(buffer, destroy),
        encoding.utf8OrThrow().value.toByte()
    )
)

public actual fun sqlite3_bind_value(
    stmt: sqlite3_stmt,
    index: Int,
    value: sqlite3_value
): Sqlite3Result = convertResult(native.sqlite3_bind_value(stmt.pointer, index, value.pointer))

public actual fun sqlite3_bind_zeroblob(
    stmt: sqlite3_stmt,
    index: Int,
    size: Int
): Sqlite3Result = convertResult(native.sqlite3_bind_zeroblob(stmt.pointer, index, size))

public actual fun sqlite3_bind_zeroblob64(
    stmt: sqlite3_stmt,
    index: Int,
    size: ULong
): Sqlite3Result = convertResult(native.sqlite3_bind_zeroblob64(stmt.pointer, index, size.toLong()))

public actual fun sqlite3_blob_bytes(blob: sqlite3_blob): Int =
    native.sqlite3_blob_bytes(blob.pointer)

public actual fun sqlite3_blob_close(blob: sqlite3_blob): Sqlite3Result =
    convertResult(native.sqlite3_blob_close(blob.pointer))

public actual fun sqlite3_blob_open(
    db: sqlite3,
    databaseName: String,
    tableName: String,
    columnName: String,
    rowIndex: Long,
    flags: Sqlite3BlobOpenFlag,
    outBlob: Sqlite3BlobOutputParam
): Sqlite3Result = convertResult(memScoped {
    useParam(outBlob) { blobPtr ->
        native.sqlite3_blob_open(
            db.pointer,
            databaseName.allocateUtf8(),
            tableName.allocateUtf8(),
            columnName.allocateUtf8(),
            rowIndex,
            flags.value,
            blobPtr
        )
    }
})

public actual fun sqlite3_blob_read(
    blob: sqlite3_blob,
    bytes: ByteArray,
    size: Int,
    offset: Int
): Sqlite3Result =
    convertResult(native.sqlite3_blob_read(blob.pointer, bytes.backing(), size, offset))

public actual fun sqlite3_blob_reopen(
    blob: sqlite3_blob,
    rowIndex: Long
): Sqlite3Result = convertResult(native.sqlite3_blob_reopen(blob.pointer, rowIndex))

public actual fun sqlite3_blob_write(
    blob: sqlite3_blob,
    bytes: ByteArray,
    size: Int,
    offset: Int
): Sqlite3Result =
    convertResult(native.sqlite3_blob_write(blob.pointer, bytes.backing(), size, offset))

public actual fun <AppData> sqlite3_busy_handler(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3BusyHandlerCallback<AppData>?
): Sqlite3Result = convertResult(db.withMemoryManager {
    native.sqlite3_busy_handler(
        db.pointer,
        functionPointer(callback, ::BusyHandlerHandler),
        keyedStableRefPointer(KEY_BUSY_HANDLER, callback, appData)
    )
})

public actual fun sqlite3_busy_timeout(
    db: sqlite3,
    millis: Int
): Sqlite3Result = convertResult(native.sqlite3_busy_timeout(db.pointer, millis))

public actual fun sqlite3_cancel_auto_extension(callback: Sqlite3AutoExtensionCallback): Int =
    autoExtensionUnregister(callback) {
        native.ksqlite_cancel_auto_extension(SharedAutoExtensionHandler)
    }

public actual fun sqlite3_changes(db: sqlite3): Int =
    native.sqlite3_changes(db.pointer)

public actual fun sqlite3_changes64(db: sqlite3): Long =
    native.sqlite3_changes64(db.pointer)

public actual fun sqlite3_clear_bindings(stmt: sqlite3_stmt): Sqlite3Result =
    commonClearBindings(stmt, native.sqlite3_clear_bindings(stmt.pointer))

public actual fun sqlite3_close(db: sqlite3?): Sqlite3Result =
    db.deallocateNullable { native.sqlite3_close(it?.pointer.notNull) }

public actual fun sqlite3_close_v2(db: sqlite3?): Sqlite3Result =
    db.deallocateNullable { native.sqlite3_close_v2(it?.pointer.notNull) }

public actual fun <AppData> sqlite3_collation_needed(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3CollationNeededCallback<AppData>?,
): Sqlite3Result = convertResult(db.withMemoryManager {
    native.sqlite3_collation_needed(
        db.pointer,
        keyedStableRefPointer(KEY_COLLATION_NEEDED, callback, appData),
        functionPointer(callback, ::CollationNeededHandler)
    )
})

public actual fun sqlite3_column_blob(
    stmt: sqlite3_stmt,
    index: Int
): ByteArray? = commonColumnBlob(
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
): Sqlite3DataType = convertDataType(native.sqlite3_column_type(stmt.pointer, index))

public actual fun sqlite3_column_value(
    stmt: sqlite3_stmt,
    index: Int
): sqlite3_value? = native.sqlite3_column_value(stmt.pointer, index)
    .orNull?.let(::sqlite3_value)

public actual fun <AppData> sqlite3_commit_hook(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3CommitHookCallback<AppData>?
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

public actual fun sqlite3_complete(sql: String): Sqlite3CompleteResult =
    convertCompleteResult(memScoped { native.sqlite3_complete(sql.allocateUtf8()) })

public actual fun sqlite3_config(option: Sqlite3ConfigOption): Sqlite3Result = commonConfig(
    option = option,
    logFunctionPointer = { cb, _ -> globalMemory.functionPointer(cb, ::ConfigLogHandler) },
    sqllogFunctionPointer = { cb, _ -> globalMemory.functionPointer(cb, ::ConfigSqlLogHandler) },
    bufferPointer = Buffer::pointer,
    keyedStableRefPointer = MemoryManager::keyedStableRefPointer,
    rowidInView = {
        useParamMemScoped(param) { paramPtr ->
            native.sqlite3_config
                .makeInvoker(ValueLayout.ADDRESS)
                .apply(id, paramPtr)
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

public actual fun sqlite3_context_db_handle(context: sqlite3_context): sqlite3? =
    native.sqlite3_context_db_handle(context.pointer).orNull?.let(::sqlite3)

public actual fun <AppData> sqlite3_create_collation_v2(
    db: sqlite3,
    name: String,
    encoding: Sqlite3TextEncoding.Set0,
    appData: AppData,
    destroy: Sqlite3DestroyCallback<AppData>?,
    callback: Sqlite3CollationCompareCallback<AppData>?
): Sqlite3Result = convertResult(db.withMemoryManager {
    memScoped {
        native.sqlite3_create_collation_v2(
            db.pointer,
            name.allocateUtf8(),
            encoding.utf8OrThrow().value,
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
    encoding: Sqlite3TextEncoding,
    appData: AppData,
    func: Sqlite3FunctionFuncCallback<AppData>?,
    step: Sqlite3FunctionStepCallback<AppData>?,
    final: Sqlite3FunctionFinalCallback<AppData>?,
    destroy: Sqlite3DestroyCallback<AppData>?
): Sqlite3Result = convertResult(db.withMemoryManager {
    memScoped {
        appFunction(appData, func, step, final, destroy) { fn, fnDestroy ->
            native.sqlite3_create_function_v2(
                db.pointer,
                name.allocateUtf8(),
                nArg,
                encoding.utf8OrThrow().value,
                keyedStableRefPointer(
                    key = appFunctionKey(name, nArg, encoding),
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

/*
public actual fun <AppData> sqlite3_create_module_v2(
    db: sqlite3,
    name: String,
    module: sqlite3_module<AppData>?,
    appData: AppData,
    destroy: Sqlite3DestroyCallback<AppData>?
): Sqlite3Result = convertResult(db.withMemoryManager {
    memScoped {
        native.sqlite3_create_module_v2(
            db.pointer,
            name.allocateUtf8(),
            module?.pointer.notNull,
            keyedStableRefPointer(moduleKey(name), appData, appData, destroy),
            stableRefDisposer(appData, destroy)
        )
    }
})
*/
public actual fun <AppData> sqlite3_create_window_function(
    db: sqlite3,
    name: String,
    nArg: Int,
    encoding: Sqlite3TextEncoding,
    appData: AppData,
    step: Sqlite3FunctionStepCallback<AppData>?,
    final: Sqlite3FunctionFinalCallback<AppData>?,
    value: Sqlite3FunctionValueCallback<AppData>?,
    inverse: Sqlite3FunctionInverseCallback<AppData>?,
    destroy: Sqlite3DestroyCallback<AppData>?
): Sqlite3Result = convertResult(db.withMemoryManager {
    memScoped {
        appWindowFunction(appData, step, final, value, inverse, destroy) { fn, fnDestroy ->
            native.sqlite3_create_window_function(
                db.pointer,
                name.allocateUtf8(),
                nArg,
                encoding.utf8OrThrow().value,
                keyedStableRefPointer(
                    key = appWindowFunctionKey(name, nArg, encoding),
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
    native.sqlite3_data_count(stmt.pointer)

public actual fun sqlite3_db_cacheflush(db: sqlite3): Sqlite3Result =
    convertResult(native.sqlite3_db_cacheflush(db.pointer))

public actual fun sqlite3_db_config(
    db: sqlite3,
    option: Sqlite3DbConfigOption,
): Sqlite3Result = commonDbConfig(
    option = option,
    bufferPointer = Buffer::pointer,
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
    name: String
): sqlite3_filename? = memScoped {
    native.sqlite3_db_filename(db.pointer, name.allocateUtf8())
}.toKStringFromUtf8OrNull()

public actual fun sqlite3_db_handle(stmt: sqlite3_stmt): sqlite3? =
    native.sqlite3_db_handle(stmt.pointer).orNull?.let(::sqlite3)

public actual fun sqlite3_db_name(
    db: sqlite3,
    index: Int
): String? = native.sqlite3_db_name(db.pointer, index)
    .toKStringFromUtf8OrNull()

public actual fun sqlite3_db_readonly(
    db: sqlite3,
    name: String
): Int = memScoped {
    native.sqlite3_db_readonly(db.pointer, name.allocateUtf8())
}

public actual fun sqlite3_db_release_memory(db: sqlite3): Sqlite3Result =
    convertResult(native.sqlite3_db_release_memory(db.pointer))

public actual fun sqlite3_db_status(
    db: sqlite3,
    option: Sqlite3DbStatusOption,
    outCurrent: Int32OutputParam?,
    outHighwater: Int32OutputParam?,
    resetFlag: Int
): Sqlite3Result = convertResult(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
    native.sqlite3_db_status(db.pointer, option.id, curPtr, highPtr, resetFlag)
})

public actual fun sqlite3_db_status64(
    db: sqlite3,
    option: Sqlite3DbStatusOption,
    outCurrent: Int64OutputParam?,
    outHighwater: Int64OutputParam?,
    resetFlag: Int
): Sqlite3Result = convertResult(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
    native.sqlite3_db_status64(db.pointer, option.id, curPtr, highPtr, resetFlag)
})

public actual fun sqlite3_declare_vtab(
    db: sqlite3,
    sql: String
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_declare_vtab(db.pointer, sql.allocateUtf8())
})

public actual fun sqlite3_deserialize(
    db: sqlite3,
    schema: String?,
    buffer: Buffer,
    dbSize: Long,
    bufferSize: Long,
    flags: Sqlite3DeserializeFlag?
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_deserialize(
        db.pointer,
        schema.allocateUtf8(),
        buffer.pointer,
        dbSize,
        bufferSize,
        flags?.value ?: 0
    )
})

/*
public actual fun sqlite3_drop_modules(
    db: sqlite3,
    keep: Array<String>?
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_drop_modules(db.pointer, keep.allocateUtf8Array())
})
*/
public actual fun sqlite3_errcode(db: sqlite3): Int =
    native.sqlite3_errcode(db.pointer)

public actual fun sqlite3_errmsg(db: sqlite3): String? =
    native.sqlite3_errmsg(db.pointer).toKStringFromUtf8OrNull()

public actual fun sqlite3_error_offset(db: sqlite3): Int =
    native.sqlite3_error_offset(db.pointer)

public actual fun sqlite3_errstr(resultCode: Int): String? =
    native.sqlite3_errstr(resultCode).toKStringFromUtf8OrNull()

public actual fun <AppData> sqlite3_exec(
    db: sqlite3,
    sql: String,
    outErrorMessage: Utf8OutputParam?,
    appData: AppData,
    callback: Sqlite3ExecCallback<AppData>?
): Sqlite3Result = convertResult(useMemoryManager {
    memScoped {
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
})

public actual fun sqlite3_expanded_sql(stmt: sqlite3_stmt): String? {
    val pointer = native.sqlite3_expanded_sql(stmt.pointer).orNull ?: return null
    val expandedSql = pointer.toKStringFromUtf8()
    native.sqlite3_free(pointer)
    return expandedSql
}

public actual fun sqlite3_extended_errcode(db: sqlite3): Int =
    native.sqlite3_extended_errcode(db.pointer)

public actual fun sqlite3_extended_result_codes(
    db: sqlite3,
    enabled: Int
): Sqlite3Result = convertResult(native.sqlite3_extended_result_codes(db.pointer, enabled))

public actual fun sqlite3_file_control(
    db: sqlite3,
    name: String?,
    opcode: Sqlite3FileControlOpcode
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_file_control(
        db.pointer,
        name.allocateUtf8(),
        opcode.code,
        MemorySegment.NULL
    )
})

public actual fun sqlite3_finalize(stmt: sqlite3_stmt?): Sqlite3Result =
    stmt.deallocateNullable { native.sqlite3_finalize(stmt?.pointer.notNull) }

public actual fun sqlite3_free(buffer: Buffer?): Unit =
    native.sqlite3_free(buffer?.pointer.notNull)

public actual fun sqlite3_get_autocommit(db: sqlite3): Int =
    native.sqlite3_get_autocommit(db.pointer)

public actual fun sqlite3_hard_heap_limit64(limit: Long): Long =
    native.sqlite3_hard_heap_limit64(limit)

public actual fun sqlite3_initialize(): Sqlite3Result =
    convertResult(native.sqlite3_initialize())

public actual fun sqlite3_interrupt(db: sqlite3): Unit =
    native.sqlite3_interrupt(db.pointer)

public actual fun sqlite3_is_interrupted(db: sqlite3): Int =
    native.sqlite3_is_interrupted(db.pointer)

public actual fun sqlite3_key(
    db: sqlite3,
    key: ByteArray,
    nKey: Int?,
): Sqlite3Result = convertResult(native.sqlite3_key(db.pointer, key.backing(), nKey ?: key.size))

public actual fun sqlite3_key_v2(
    db: sqlite3,
    dbName: String,
    key: ByteArray,
    nKey: Int?,
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_key_v2(db.pointer, dbName.allocateUtf8(), key.backing(), nKey ?: key.size)
})

public actual fun sqlite3_keyword_check(
    word: String
): Int = memScoped {
    val cWord = word.allocateUtf8()
    val nByte = cWord.byteSize().toInt()
    native.sqlite3_keyword_check(cWord, nByte)
}

public actual fun sqlite3_keyword_count(): Int =
    native.sqlite3_keyword_count()

public actual fun sqlite3_keyword_name(
    index: Int,
    outName: Utf8OutputParam,
): Sqlite3Result = convertResult(memScoped {
    useParam(outName) { namePtr ->
        val size = Int32OutputParam(0)

        useParam(size) { sizePtr ->
            native.sqlite3_keyword_name(index, namePtr, sizePtr)
        }.also {
            outName.size = size.value
        }
    }
})

public actual fun sqlite3_last_insert_rowid(db: sqlite3): Long =
    native.sqlite3_last_insert_rowid(db.pointer)

public actual fun sqlite3_libversion(): String =
    native.sqlite3_libversion().toKStringFromUtf8()

public actual fun sqlite3_libversion_number(db: sqlite3): Int =
    native.sqlite3_libversion_number()

public actual fun sqlite3_limit(
    db: sqlite3,
    id: Sqlite3Limit,
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

public actual fun sqlite3_msize(buffer: Buffer?): ULong =
    native.sqlite3_msize(buffer?.pointer.notNull).toULong()

public actual fun sqlite3_next_stmt(
    db: sqlite3,
    stmt: sqlite3_stmt?
): sqlite3_stmt? = native.sqlite3_next_stmt(db.pointer, stmt?.pointer.notNull)
    .orNull?.let(::sqlite3_stmt)

public actual fun sqlite3_open(
    fileName: String,
    outDb: Sqlite3OutputParam
): Sqlite3Result = convertResult(memScoped {
    useParam(outDb) { dbPtr ->
        native.sqlite3_open(fileName.allocateUtf8(), dbPtr)
    }
})

public actual fun sqlite3_open_v2(
    fileName: String,
    outDb: Sqlite3OutputParam,
    flags: Sqlite3OpenFlag.Valid,
    vfs: String?
): Sqlite3Result = convertResult(memScoped {
    useParam(outDb) { dbPtr ->
        native.sqlite3_open_v2(fileName.allocateUtf8(), dbPtr, flags.value, vfs.allocateUtf8())
    }
})

public actual fun sqlite3_overload_function(
    db: sqlite3,
    name: String,
    nArg: Int
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_overload_function(db.pointer, name.allocateUtf8(), nArg)
})

public actual fun sqlite3_prepare_v2(
    db: sqlite3,
    sql: ByteArray,
    maxBytes: Int,
    outStmt: Sqlite3StmtOutputParam,
    outOffset: Int32OutputParam?
): Sqlite3Result = convertResult(useParamsMemScoped(outStmt, outOffset) { stmtPtr, offsetPtr ->
    native.ksqlite_prepare_v2(db.pointer, sql.backing(), maxBytes, stmtPtr, offsetPtr)
})

public actual fun sqlite3_prepare_v2(
    db: sqlite3,
    sql: String,
    outStmt: Sqlite3StmtOutputParam
): Sqlite3Result = convertResult(memScoped {
    useParam(outStmt) { stmtPtr ->
        val cSql = sql.allocateUtf8()
        val nByte = cSql.byteSize().toInt()
        native.sqlite3_prepare_v2(db.pointer, cSql, nByte, stmtPtr, MemorySegment.NULL)
    }
})

public actual fun sqlite3_prepare_v3(
    db: sqlite3,
    sql: ByteArray,
    maxBytes: Int,
    flags: Sqlite3PrepareFlag?,
    outStmt: Sqlite3StmtOutputParam,
    outOffset: Int32OutputParam?
): Sqlite3Result = convertResult(useParamsMemScoped(outStmt, outOffset) { stmtPtr, offsetPtr ->
    val prepFlags = flags?.value ?: 0
    native.ksqlite_prepare_v3(db.pointer, sql.backing(), maxBytes, prepFlags, stmtPtr, offsetPtr)
})

public actual fun sqlite3_prepare_v3(
    db: sqlite3,
    sql: String,
    flags: Sqlite3PrepareFlag?,
    outStmt: Sqlite3StmtOutputParam
): Sqlite3Result = convertResult(memScoped {
    useParam(outStmt) { stmtPtr ->
        val cSql = sql.allocateUtf8()
        val nByte = cSql.byteSize().toInt()
        val prepFlags = flags?.value ?: 0
        native.sqlite3_prepare_v3(db.pointer, cSql, nByte, prepFlags, stmtPtr, MemorySegment.NULL)
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
    callback: Sqlite3PreupdateHookCallback<AppData>?
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
    outValue: Sqlite3ValueOutputParam
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    native.sqlite3_preupdate_new(db.pointer, index, valuePtr)
})

public actual fun sqlite3_preupdate_old(
    db: sqlite3,
    index: Int,
    outValue: Sqlite3ValueOutputParam
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    native.sqlite3_preupdate_old(db.pointer, index, valuePtr)
})

public actual fun <AppData> sqlite3_progress_handler(
    db: sqlite3,
    nOps: Int,
    appData: AppData,
    callback: Sqlite3ProgressHandlerCallback<AppData>?
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
    buffer: Buffer?
): Unit = native.sqlite3_randomness(size, buffer?.pointer.notNull)

public actual fun sqlite3_realloc(
    buffer: Buffer?,
    size: Int
): Buffer? = Buffer.from(
    pointer = native.sqlite3_realloc(buffer?.pointer.notNull, size),
    size = size.toLong()
)

public actual fun sqlite3_realloc64(
    buffer: Buffer?,
    size: Long
): Buffer? = Buffer.from(
    pointer = native.sqlite3_realloc64(buffer?.pointer.notNull, size),
    size = size
)

public actual fun sqlite3_rekey(
    db: sqlite3,
    key: ByteArray,
    nKey: Int?,
): Sqlite3Result = convertResult(native.sqlite3_rekey(db.pointer, key.backing(), nKey ?: key.size))

public actual fun sqlite3_rekey_v2(
    db: sqlite3,
    dbName: String,
    key: ByteArray,
    nKey: Int?,
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_rekey_v2(db.pointer, dbName.allocateUtf8(), key.backing(), nKey ?: key.size)
})

public actual fun sqlite3_release_memory(size: Int): Int =
    native.sqlite3_release_memory(size)

public actual fun sqlite3_reset(stmt: sqlite3_stmt): Sqlite3Result =
    convertResult(native.sqlite3_reset(stmt.pointer))

public actual fun sqlite3_reset_auto_extension(): Unit =
    autoExtensionReset { native.sqlite3_reset_auto_extension() }

public actual fun sqlite3_result_blob(
    context: sqlite3_context,
    bytes: ByteArray,
    size: Int,
    destroy: Sqlite3DestroyCallback<ByteArray>?
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
    destroy: Sqlite3DestroyCallback<Buffer>?
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
    val nByte = cMessage.byteSize().toInt()
    native.sqlite3_result_error(context.pointer, cMessage, nByte)
}

public actual fun sqlite3_result_error_code(
    context: sqlite3_context,
    code: Int
): Unit = native.sqlite3_result_error_code(context.pointer, code)

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
    destroy: Sqlite3DestroyCallback<Data>?
): Unit = with(globalMemory) {
    // Use globalMemory because of lack of information within sqlite3_value_pointer()
    allocateNamedPointer(type, destroy) { ptr, ptrDestroy ->
        native.sqlite3_result_pointer(
            context.pointer,
            stableRefPointer(ptr, data, ptrDestroy),
            ptr.name.notNull,
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
    text: String
): Unit = memScoped {
    val cText = text.allocateUtf8()
    val nByte = cText.byteSize().toInt()
    native.sqlite3_result_text(context.pointer, cText.notNull, nByte, SqliteTransient)
}

public actual fun sqlite3_result_text64(
    context: sqlite3_context,
    buffer: Buffer,
    size: Long,
    encoding: Sqlite3TextEncoding.Set1,
    destroy: Sqlite3DestroyCallback<Buffer>?
): Unit = native.sqlite3_result_text64(
    context.pointer,
    buffer.pointer,
    size,
    bufferDisposer(buffer, destroy),
    encoding.utf8OrThrow().value.toByte()
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
): Int = native.sqlite3_result_zeroblob64(context.pointer, size.toLong())

public actual fun <AppData> sqlite3_rollback_hook(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3RollbackHookCallback<AppData>?
): Unit = db.withMemoryManager {
    native.sqlite3_rollback_hook(
        db.pointer,
        functionPointer(callback, ::RollbackHookHandler),
        keyedStableRefPointer(KEY_ROLLBACK_HOOK, callback, appData)
    )
}

public actual fun sqlite3_serialize(
    db: sqlite3,
    schema: String?,
    flags: Sqlite3SerializeFlag?
): Buffer? {
    val size = Int64OutputParam(0)

    val pointer = memScoped {
        useParam(size) { sizePtr ->
            val mFlags = flags?.value ?: 0
            native.sqlite3_serialize(db.pointer, schema.allocateUtf8(), sizePtr, mFlags)
        }
    }

    return Buffer.from(pointer, size.value)
}

public actual fun <AppData> sqlite3_set_authorizer(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3SetAuthorizerCallback<AppData>?
): Sqlite3Result = convertResult(db.withMemoryManager {
    native.sqlite3_set_authorizer(
        db.pointer,
        functionPointer(callback, ::SetAuthorizerHandler),
        keyedStableRefPointer(KEY_SET_AUTHORIZER, callback, appData)
    )
})

public actual fun sqlite3_set_errmsg(
    db: sqlite3,
    errorCode: Sqlite3Result.Failure,
    message: String?
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_set_errmsg(db.pointer, errorCode.code, message.allocateUtf8())
})

public actual fun sqlite3_set_last_insert_rowid(
    db: sqlite3,
    rowId: Long
): Unit = native.sqlite3_set_last_insert_rowid(db.pointer, rowId)

public actual fun sqlite3_shutdown(): Sqlite3Result =
    convertResult(native.sqlite3_shutdown())

public actual fun sqlite3_snapshot_cmp(
    snapshot1: sqlite3_snapshot,
    snapshot2: sqlite3_snapshot
): Int = native.sqlite3_snapshot_cmp(snapshot1.pointer, snapshot2.pointer)

public actual fun sqlite3_snapshot_free(snapshot: sqlite3_snapshot): Unit =
    native.sqlite3_snapshot_free(snapshot.pointer)

public actual fun sqlite3_snapshot_get(
    db: sqlite3,
    name: String?,
    outSnapshot: Sqlite3SnapshotOutputParam
): Sqlite3Result = convertResult(memScoped {
    useParam(outSnapshot) { snapshotPtr ->
        native.sqlite3_snapshot_get(db.pointer, name.allocateUtf8(), snapshotPtr)
    }
})

public actual fun sqlite3_snapshot_open(
    db: sqlite3,
    name: String?,
    snapshot: sqlite3_snapshot
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_snapshot_open(db.pointer, name.allocateUtf8(), snapshot.pointer)
})

public actual fun sqlite3_snapshot_recover(
    db: sqlite3,
    name: String?
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_snapshot_recover(db.pointer, name.allocateUtf8())
})

public actual fun sqlite3_soft_heap_limit64(limit: Long): Long =
    native.sqlite3_soft_heap_limit64(limit)

public actual fun sqlite3_sourceid(): String =
    native.sqlite3_sourceid().toKStringFromUtf8()

public actual fun sqlite3_sql(stmt: sqlite3_stmt): String =
    native.sqlite3_sql(stmt.pointer).toKStringFromUtf8()

public actual fun sqlite3_status(
    option: Sqlite3StatusOption,
    outCurrent: Int32OutputParam,
    outHighwater: Int32OutputParam,
    resetFlag: Int
): Sqlite3Result = convertResult(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
    native.sqlite3_status(option.id, curPtr, highPtr, resetFlag)
})

public actual fun sqlite3_status64(
    option: Sqlite3StatusOption,
    outCurrent: Int64OutputParam,
    outHighwater: Int64OutputParam,
    resetFlag: Int
): Sqlite3Result = convertResult(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
    native.sqlite3_status64(option.id, curPtr, highPtr, resetFlag)
})

public actual fun sqlite3_step(stmt: sqlite3_stmt): Sqlite3Result =
    convertResult(native.sqlite3_step(stmt.pointer))

public actual fun sqlite3_stmt_busy(stmt: sqlite3_stmt): Int =
    native.sqlite3_stmt_busy(stmt.pointer)

public actual fun sqlite3_stmt_explain(
    stmt: sqlite3_stmt,
    mode: Sqlite3ExplainMode
): Sqlite3Result = convertResult(native.sqlite3_stmt_explain(stmt.pointer, mode.id))

public actual fun sqlite3_stmt_isexplain(stmt: sqlite3_stmt): Sqlite3ExplainMode =
    convertExplainMode(native.sqlite3_stmt_isexplain(stmt.pointer))

public actual fun sqlite3_stmt_readonly(stmt: sqlite3_stmt): Int =
    native.sqlite3_stmt_readonly(stmt.pointer)

public actual fun sqlite3_stmt_status(
    stmt: sqlite3_stmt,
    counter: Sqlite3StatementStatusCounter,
    resetFlag: Int
): Int = native.sqlite3_stmt_status(stmt.pointer, counter.id, resetFlag)

public actual fun sqlite3_strglob(
    pattern: String,
    string: String
): Int = memScoped {
    native.sqlite3_strglob(pattern.allocateUtf8(), string.allocateUtf8())
}

public actual fun sqlite3_stricmp(
    left: String,
    right: String
): Int = memScoped {
    native.sqlite3_stricmp(left.allocateUtf8(), right.allocateUtf8())
}

public actual fun sqlite3_strlike(
    pattern: String,
    string: String,
    escape: Char
): Int = memScoped {
    native.sqlite3_strlike(pattern.allocateUtf8(), string.allocateUtf8(), escape.code)
}

public actual fun sqlite3_strnicmp(
    left: String,
    right: String,
    size: Int
): Int = memScoped {
    native.sqlite3_strnicmp(left.allocateUtf8(), right.allocateUtf8(), size)
}

public actual fun sqlite3_system_errno(db: sqlite3): Int =
    native.sqlite3_system_errno(db.pointer)

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
): Sqlite3Result = convertResult(memScoped {
    val dataTypePtr = outDataType?.attach(this)
    val collationNamePtr = outCollationName?.attach(this)
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
        collationNamePtr?.let(outCollationName::detach)
        notNullPtr?.let(outNotNull::detach)
        primaryKeyPtr?.let(outPrimaryKey::detach)
        autoIncrementPtr?.let(outAutoIncrement::detach)
    }
})

public actual fun sqlite3_total_changes(db: sqlite3): Int =
    native.sqlite3_total_changes(db.pointer)

public actual fun sqlite3_total_changes64(db: sqlite3): Long =
    native.sqlite3_total_changes64(db.pointer)

public actual fun <AppData> sqlite3_trace_v2(
    db: sqlite3,
    mask: Sqlite3TraceCode?,
    appData: AppData,
    callback: Sqlite3TraceCallback<AppData>?
): Sqlite3Result = convertResult(db.withMemoryManager {
    native.sqlite3_trace_v2(
        db.pointer,
        mask?.value ?: 0,
        functionPointer(callback, ::TraceHandler),
        keyedStableRefPointer(KEY_TRACE, callback, appData)
    )
})

public actual fun sqlite3_txn_state(
    db: sqlite3,
    schema: String?
): Sqlite3TransactionState? = convertTransactionState(memScoped {
    native.sqlite3_txn_state(db.pointer, schema.allocateUtf8())
})

public actual fun <AppData> sqlite3_update_hook(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3UpdateHookCallback<AppData>?
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
    native.sqlite3_uri_boolean(fileName.allocateUtf8(), parameter.allocateUtf8(), default)
}

public actual fun sqlite3_uri_int64(
    fileName: sqlite3_filename,
    parameter: String,
    default: Long
): Long = memScoped {
    native.sqlite3_uri_int64(fileName.allocateUtf8(), parameter.allocateUtf8(), default)
}

public actual fun sqlite3_uri_key(
    fileName: sqlite3_filename,
    index: Int
): String? = memScoped {
    native.sqlite3_uri_key(fileName.allocateUtf8(), index)
}.toKStringFromUtf8OrNull()

public actual fun sqlite3_uri_parameter(
    fileName: sqlite3_filename,
    parameter: String
): String? = memScoped {
    native.sqlite3_uri_parameter(fileName.allocateUtf8(), parameter.allocateUtf8())
}.toKStringFromUtf8OrNull()

public actual fun sqlite3_value_bytes(value: sqlite3_value): Int =
    native.sqlite3_value_bytes(value.pointer)

public actual fun sqlite3_value_double(value: sqlite3_value): Double =
    native.sqlite3_value_double(value.pointer)

public actual fun sqlite3_value_dup(value: sqlite3_value): sqlite3_value? =
    native.sqlite3_value_dup(value.pointer).orNull?.let(::sqlite3_value)

public actual fun sqlite3_value_encoding(value: sqlite3_value): Sqlite3TextEncoding.Set2? =
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

public actual fun sqlite3_value_numeric_type(value: sqlite3_value): Sqlite3DataType =
    convertDataType(native.sqlite3_value_numeric_type(value.pointer))

public actual fun sqlite3_value_subtype(value: sqlite3_value): UInt =
    native.sqlite3_value_subtype(value.pointer).toUInt()

public actual fun sqlite3_value_text(value: sqlite3_value): String? =
    native.sqlite3_value_text(value.pointer).toKStringFromUtf8OrNull()

public actual fun sqlite3_value_type(value: sqlite3_value): Sqlite3DataType =
    convertDataType(native.sqlite3_value_type(value.pointer))

public actual fun sqlite3_vfs_find(name: String?): sqlite3_vfs? = memScoped {
    native.sqlite3_vfs_find(name.allocateUtf8()).orNull?.let(::sqlite3_vfs)
}

public actual fun sqlite3_vfs_register(
    vfs: sqlite3_vfs,
    makeDefault: Int
): Sqlite3Result = convertResult(native.sqlite3_vfs_register(vfs.pointer, makeDefault))

public actual fun sqlite3_vfs_unregister(vfs: sqlite3_vfs): Sqlite3Result =
    convertResult(native.sqlite3_vfs_unregister(vfs.pointer))

public actual fun sqlite3_vtab_collation(
    info: sqlite3_index_info,
    index: Int
): String? = native.sqlite3_vtab_collation(info.pointer, index)
    .toKStringFromUtf8OrNull()

public actual fun sqlite3_vtab_config(
    db: sqlite3,
    option: Sqlite3VirtualTableConfigOption
): Sqlite3Result = commonVtabConfig(option) { id, values ->
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
    outValue: Sqlite3ValueOutputParam?
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    native.sqlite3_vtab_in_first(value.pointer, valuePtr)
})

public actual fun sqlite3_vtab_in_next(
    value: sqlite3_value,
    outValue: Sqlite3ValueOutputParam?
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    native.sqlite3_vtab_in_next(value.pointer, valuePtr)
})

public actual fun sqlite3_vtab_nochange(context: sqlite3_context): Int =
    native.sqlite3_vtab_nochange(context.pointer)

public actual fun sqlite3_vtab_on_conflict(db: sqlite3): Sqlite3Result =
    convertResult(native.sqlite3_vtab_on_conflict(db.pointer))

public actual fun sqlite3_vtab_rhs_value(
    info: sqlite3_index_info,
    index: Int,
    outValue: Sqlite3ValueOutputParam?
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    native.sqlite3_vtab_rhs_value(info.pointer, index, valuePtr)
})

public actual fun sqlite3_wal_autocheckpoint(
    db: sqlite3,
    nFrame: Int
): Sqlite3Result = convertResult(native.sqlite3_wal_autocheckpoint(db.pointer, nFrame))

public actual fun sqlite3_wal_checkpoint(
    db: sqlite3,
    name: String?
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_wal_checkpoint(db.pointer, name.allocateUtf8())
})

public actual fun sqlite3_wal_checkpoint_v2(
    db: sqlite3,
    name: String?,
    mode: Sqlite3CheckpointMode,
    outNLog: Int32OutputParam?,
    outNCkpt: Int32OutputParam?
): Sqlite3Result = convertResult(memScoped {
    useParams(outNLog, outNCkpt) { nLogPtr, nCkptPtr ->
        native.sqlite3_wal_checkpoint_v2(
            db.pointer,
            name.allocateUtf8(),
            mode.id,
            nLogPtr,
            nCkptPtr
        )
    }
})

public actual fun <AppData> sqlite3_wal_hook(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3WalHookCallback<AppData>?
): Unit = db.withMemoryManager {
    native.sqlite3_wal_hook(
        db.pointer,
        functionPointer(callback, ::WalHookHandler),
        keyedStableRefPointer(KEY_WAL_HOOK, callback, appData)
    )
}