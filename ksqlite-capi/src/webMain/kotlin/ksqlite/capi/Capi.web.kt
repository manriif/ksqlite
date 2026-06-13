@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi

import ksqlite.capi.callbacks.Sqlite3AuthorizerCallback
import ksqlite.capi.callbacks.Sqlite3AutoExtensionCallback
import ksqlite.capi.callbacks.Sqlite3AutovacuumPagesCallback
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
import ksqlite.capi.callbacks.Sqlite3TraceCallback
import ksqlite.capi.callbacks.Sqlite3UpdateHookCallback
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
import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.HeapAllocatorScope
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.NullPtr
import ksqlite.capi.memory.allocateUtf8
import ksqlite.capi.memory.allocateUtf8Array
import ksqlite.capi.memory.allocateUtf8Pointer
import ksqlite.capi.memory.bufferDisposer
import ksqlite.capi.memory.bufferScoped
import ksqlite.capi.memory.globalDisposer
import ksqlite.capi.memory.globalMemory
import ksqlite.capi.memory.heapScoped
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
import ksqlite.capi.types.Sqlite3BlobOpenFlag
import ksqlite.capi.types.Sqlite3BlobOutputParam
import ksqlite.capi.types.Sqlite3CompleteResult
import ksqlite.capi.types.Sqlite3ConfigOption
import ksqlite.capi.types.Sqlite3ConflictResolutionMode
import ksqlite.capi.types.Sqlite3DataType
import ksqlite.capi.types.Sqlite3DbConfigOption
import ksqlite.capi.types.Sqlite3DbStatusOption
import ksqlite.capi.types.Sqlite3DeserializeFlag
import ksqlite.capi.types.Sqlite3ExplainMode
import ksqlite.capi.types.Sqlite3FileControlOpcode
import ksqlite.capi.types.Sqlite3Limit
import ksqlite.capi.types.Sqlite3OpenFlag
import ksqlite.capi.types.Sqlite3OutputParam
import ksqlite.capi.types.Sqlite3PrepareFlag
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.Sqlite3SerializeFlag
import ksqlite.capi.types.Sqlite3StatementStatusCounter
import ksqlite.capi.types.Sqlite3StatusOption
import ksqlite.capi.types.Sqlite3StmtOutputParam
import ksqlite.capi.types.Sqlite3TextEncoding
import ksqlite.capi.types.Sqlite3TraceCode
import ksqlite.capi.types.Sqlite3TransactionState
import ksqlite.capi.types.Sqlite3ValueOutputParam
import ksqlite.capi.types.Utf8OutputParam
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_backup
import ksqlite.capi.types.sqlite3_blob
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_filename
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.capi.types.sqlite3_value
import ksqlite.capi.types.sqlite3_vfs
import ksqlite.capi.types.useParam
import ksqlite.capi.types.useParamStackScoped
import ksqlite.capi.types.useParamsStackScoped
import ksqlite.capi.types.vtab.Sqlite3VTabConfigOption
import ksqlite.capi.vtab.createVTabModule
import ksqlite.capi.vtab.sqlite3_index_info
import ksqlite.capi.vtab.sqlite3_module
import ksqlite.js.arrayForEachIndexed
import ksqlite.js.arraySize
import ksqlite.js.copyTo
import ksqlite.js.plus
import ksqlite.wasm.IR
import ksqlite.wasm.WasmPointer
import ksqlite.wasm.size
import ksqlite.wasm.sizeofIR
import kotlin.js.toJsBigInt
import kotlin.js.toLong

///////////////////////////////////////////////////////////////////////////
// Helpers
///////////////////////////////////////////////////////////////////////////

/**
 * Invokes a function accepting a variadic parameter.
 */
private inline fun <Result> HeapAllocatorScope.invokeVariadic(
    values: Array<out VariadicValue<WasmPointer>?>,
    noinline manager: () -> MemoryManager,
    invoke: HeapAllocatorScope.(vaList: WasmPointer) -> Result
): Result {
    val pointerSize = memory.sizeofIR(IR.Ptr)
    val argCount = arraySize(values)
    val vaArgsSize = pointerSize * argCount
    val vaArgsPointer = allocate(vaArgsSize)

    arrayForEachIndexed(values) { index, value ->
        val vaArgPointer = vaArgsPointer + (index * pointerSize)

        when (value) {
            null -> memory.pokePtr(vaArgPointer, NullPtr)
            is OfInt -> memory.poke32(vaArgPointer, value.value)
            is OfUInt -> memory.poke32(vaArgPointer, value.value.toInt())
            is OfLong -> memory.poke64(vaArgPointer, value.value.toJsBigInt())
            is OfPointer -> memory.pokePtr(vaArgPointer, value.value)

            // String value is allocated on MemoryManager rather than current scope as the String is
            // tied to the lifecycle of the caller MemoryScope
            is OfString -> memory.pokePtr(
                address = vaArgPointer,
                value = manager().keyedStringPointer(
                    key = value.key,
                    value = value.value
                )
            )
        }
    }

    return invoke(vaArgsPointer)
}

/**
 * Invokes a function accepting a variadic parameter.
 */
private inline fun <Result> invokeVariadic(
    values: Array<out VariadicValue<WasmPointer>?>,
    noinline manager: () -> MemoryManager,
    invoke: HeapAllocatorScope.(vaList: WasmPointer) -> Result
): Result = heapScoped {
    invokeVariadic(values, manager, invoke)
}

/**
 * Invokes a function accepting a variadic parameter.
 */
context(scope: HeapAllocatorScope)
private inline fun <Result> invokeVariadic(
    noinline manager: () -> MemoryManager,
    vararg values: VariadicValue<WasmPointer>?,
    invoke: HeapAllocatorScope.(vaList: WasmPointer) -> Result
): Result = scope.invokeVariadic(values, manager, invoke)

///////////////////////////////////////////////////////////////////////////
// Functions
///////////////////////////////////////////////////////////////////////////

public actual fun sqlite3_auto_extension(callback: Sqlite3AutoExtensionCallback): Sqlite3Result =
    autoExtensionRegister(callback) { exports.ksqlite_auto_extension(SharedAutoExtensionHandler) }

public actual fun <AppData> sqlite3_autovacuum_pages(
    db: sqlite3,
    appData: AppData,
    destroy: Sqlite3DestroyCallback<in AppData>?,
    callback: Sqlite3AutovacuumPagesCallback<in AppData>?
): Sqlite3Result = convertResult(db.withMemoryManager {
    exports.sqlite3_autovacuum_pages(
        db.pointer,
        functionPointer(callback, ::AutovacuumPagesHandler),
        keyedStableRefPointer(KEY_AUTOVACUUM_PAGES, callback, appData, destroy),
        stableRefDisposer(callback, destroy)
    )
})

public actual fun sqlite3_backup_finish(backup: sqlite3_backup): Sqlite3Result = convertResult(
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
): Sqlite3Result = convertResult(exports.sqlite3_backup_step(backup.pointer, nPage))

public actual fun sqlite3_bind_blob(
    stmt: sqlite3_stmt,
    index: Int,
    bytes: ByteArray,
    size: Int,
    destroy: Sqlite3DestroyCallback<ByteArray>?
): Sqlite3Result = convertResult(
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
    destroy: Sqlite3DestroyCallback<Buffer>?
): Sqlite3Result = convertResult(
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
): Sqlite3Result = convertResult(exports.sqlite3_bind_double(stmt.pointer, index, value))

public actual fun sqlite3_bind_int(
    stmt: sqlite3_stmt,
    index: Int,
    value: Int
): Sqlite3Result = convertResult(exports.sqlite3_bind_int(stmt.pointer, index, value))

public actual fun sqlite3_bind_int64(
    stmt: sqlite3_stmt,
    index: Int,
    value: Long
): Sqlite3Result =
    convertResult(exports.sqlite3_bind_int64(stmt.pointer, index, value.toJsBigInt()))

public actual fun sqlite3_bind_null(
    stmt: sqlite3_stmt,
    index: Int
): Sqlite3Result = convertResult(exports.sqlite3_bind_null(stmt.pointer, index))

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
    destroy: Sqlite3DestroyCallback<Data>?
): Sqlite3Result = convertResult(with(globalMemory) {
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
): Sqlite3Result = convertResult(heapScoped {
    val cText = value.allocateUtf8()
    exports.sqlite3_bind_text(stmt.pointer, index, cText.pointer, cText.size, SqliteTransient)
})

public actual fun sqlite3_bind_text64(
    stmt: sqlite3_stmt,
    index: Int,
    buffer: Buffer,
    size: Long,
    encoding: Sqlite3TextEncoding.Set1,
    destroy: Sqlite3DestroyCallback<Buffer>?
): Sqlite3Result = convertResult(
    exports.sqlite3_bind_text64(
        stmt.pointer,
        index,
        buffer.pointer,
        size.toJsBigInt(),
        bufferDisposer(buffer, destroy),
        encoding.utf8OrThrow().value
    )
)

public actual fun sqlite3_bind_value(
    stmt: sqlite3_stmt,
    index: Int,
    value: sqlite3_value
): Sqlite3Result = convertResult(exports.sqlite3_bind_value(stmt.pointer, index, value.pointer))

public actual fun sqlite3_bind_zeroblob(
    stmt: sqlite3_stmt,
    index: Int,
    size: Int
): Sqlite3Result = convertResult(exports.sqlite3_bind_zeroblob(stmt.pointer, index, size))

public actual fun sqlite3_bind_zeroblob64(
    stmt: sqlite3_stmt,
    index: Int,
    size: ULong
): Sqlite3Result =
    convertResult(exports.sqlite3_bind_zeroblob64(stmt.pointer, index, size.toLong().toJsBigInt()))

public actual fun sqlite3_blob_bytes(blob: sqlite3_blob): Int =
    exports.sqlite3_blob_bytes(blob.pointer)

public actual fun sqlite3_blob_close(blob: sqlite3_blob): Sqlite3Result =
    convertResult(exports.sqlite3_blob_close(blob.pointer))

public actual fun sqlite3_blob_open(
    db: sqlite3,
    databaseName: String,
    tableName: String,
    columnName: String,
    rowIndex: Long,
    flags: Sqlite3BlobOpenFlag,
    outBlob: Sqlite3BlobOutputParam
): Sqlite3Result = convertResult(heapScoped {
    useParam(outBlob) { blobPtr ->
        exports.sqlite3_blob_open(
            db.pointer,
            databaseName.allocateUtf8Pointer(),
            tableName.allocateUtf8Pointer(),
            columnName.allocateUtf8Pointer(),
            rowIndex.toJsBigInt(),
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
): Sqlite3Result {
    val memory = wasm
    val pointer = memory.alloc(size)
    val result = convertResult(exports.sqlite3_blob_read(blob.pointer, pointer, size, offset))

    try {
        if (result == Sqlite3Result.OK) {
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
): Sqlite3Result = convertResult(exports.sqlite3_blob_reopen(blob.pointer, rowid.toJsBigInt()))

public actual fun sqlite3_blob_write(
    blob: sqlite3_blob,
    input: ByteArray,
    size: Int,
    offset: Int
): Sqlite3Result = convertResult(bufferScoped(input) { bufferPtr ->
    exports.sqlite3_blob_write(blob.pointer, bufferPtr, size, offset)
})

public actual fun <AppData> sqlite3_busy_handler(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3BusyHandlerCallback<AppData>?
): Sqlite3Result = convertResult(db.withMemoryManager {
    exports.sqlite3_busy_handler(
        db.pointer,
        functionPointer(callback, ::BusyHandlerHandler),
        keyedStableRefPointer(KEY_BUSY_HANDLER, callback, appData)
    )
})

public actual fun sqlite3_busy_timeout(
    db: sqlite3,
    millis: Int
): Sqlite3Result = convertResult(exports.sqlite3_busy_timeout(db.pointer, millis))

public actual fun sqlite3_cancel_auto_extension(callback: Sqlite3AutoExtensionCallback): Int =
    autoExtensionUnregister(callback) {
        exports.ksqlite_cancel_auto_extension(SharedAutoExtensionHandler)
    }

public actual fun sqlite3_changes(db: sqlite3): Int =
    exports.sqlite3_changes(db.pointer)

public actual fun sqlite3_changes64(db: sqlite3): Long =
    exports.sqlite3_changes64(db.pointer).toLong()

public actual fun sqlite3_clear_bindings(stmt: sqlite3_stmt): Sqlite3Result =
    commonClearBindings(stmt, exports.sqlite3_clear_bindings(stmt.pointer))

public actual fun sqlite3_close(db: sqlite3): Sqlite3Result =
    db.deallocate { exports.sqlite3_close(it.pointer) }

public actual fun sqlite3_close_v2(db: sqlite3): Sqlite3Result =
    db.deallocate { exports.sqlite3_close_v2(it.pointer) }

public actual fun <AppData> sqlite3_collation_needed(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3CollationNeededCallback<AppData>?,
): Sqlite3Result = convertResult(db.withMemoryManager {
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
): Sqlite3DataType = convertDataType(exports.sqlite3_column_type(stmt.pointer, index))

public actual fun sqlite3_column_value(
    stmt: sqlite3_stmt,
    index: Int
): sqlite3_value? = exports.sqlite3_column_value(stmt.pointer, index)
    .orNull?.let(::sqlite3_value)

public actual fun <AppData> sqlite3_commit_hook(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3CommitHookCallback<AppData>?
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

public actual fun sqlite3_complete(sql: String): Sqlite3CompleteResult =
    convertCompleteResult(heapScoped { exports.sqlite3_complete(sql.allocateUtf8Pointer()) })

public actual fun sqlite3_config(option: Sqlite3ConfigOption): Sqlite3Result = commonConfig(
    option = option,
    logFunctionPointer = { cb, _ -> globalMemory.functionPointer(cb, ::ConfigLogHandler) },
    sqllogFunctionPointer = { cb, _ -> globalMemory.functionPointer(cb, ::ConfigSqlLogHandler) },
    bufferPointer = Buffer::pointer,
    keyedStableRefPointer = globalMemory::keyedStableRefPointer,
    rowidInView = {
        heapScoped {
            useParam(param) { paramPtr ->
                invokeVariadic(::globalMemory, VariadicValue.OfPointer(paramPtr)) { vaListPtr ->
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
    encoding: Sqlite3TextEncoding.Set0,
    appData: AppData,
    destroy: Sqlite3DestroyCallback<in AppData>?,
    callback: Sqlite3CollationCompareCallback<in AppData>?
): Sqlite3Result = convertResult(db.withMemoryManager {
    heapScoped {
        exports.sqlite3_create_collation_v2(
            db.pointer,
            name.allocateUtf8Pointer(),
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
    func: Sqlite3FunctionFuncCallback<in AppData>?,
    step: Sqlite3FunctionStepCallback<in AppData>?,
    final: Sqlite3FunctionFinalCallback<in AppData>?,
    destroy: Sqlite3DestroyCallback<in AppData>?
): Sqlite3Result = convertResult(db.withMemoryManager {
    heapScoped {
        createFunction(appData, func, step, final, destroy) { fn, fnDestroy ->
            exports.sqlite3_create_function_v2(
                db.pointer,
                name.allocateUtf8Pointer(),
                nArg,
                encoding.utf8OrThrow().value,
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
    destroy: Sqlite3DestroyCallback<in AppData>?
): Sqlite3Result = convertResult(db.withMemoryManager {
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
    encoding: Sqlite3TextEncoding,
    appData: AppData,
    step: Sqlite3FunctionStepCallback<in AppData>?,
    final: Sqlite3FunctionFinalCallback<in AppData>?,
    value: Sqlite3FunctionValueCallback<in AppData>?,
    inverse: Sqlite3FunctionInverseCallback<in AppData>?,
    destroy: Sqlite3DestroyCallback<in AppData>?
): Sqlite3Result = convertResult(db.withMemoryManager {
    heapScoped {
        createWindowFunction(appData, step, final, value, inverse, destroy) { fn, fnDestroy ->
            exports.sqlite3_create_window_function(
                db.pointer,
                name.allocateUtf8Pointer(),
                nArg,
                encoding.utf8OrThrow().value,
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

public actual fun sqlite3_db_cacheflush(db: sqlite3): Sqlite3Result =
    convertResult(exports.sqlite3_db_cacheflush(db.pointer))

public actual fun sqlite3_db_config(
    db: sqlite3,
    option: Sqlite3DbConfigOption,
): Sqlite3Result = commonDbConfig(
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
    name: String
): sqlite3_filename? = heapScoped {
    exports.sqlite3_db_filename(db.pointer, name.allocateUtf8Pointer())
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
    name: String
): Int = heapScoped {
    exports.sqlite3_db_readonly(db.pointer, name.allocateUtf8Pointer())
}

public actual fun sqlite3_db_release_memory(db: sqlite3): Sqlite3Result =
    convertResult(exports.sqlite3_db_release_memory(db.pointer))

public actual fun sqlite3_db_status(
    db: sqlite3,
    option: Sqlite3DbStatusOption,
    outCurrent: Int32OutputParam?,
    outHighwater: Int32OutputParam?,
    resetFlag: Int
): Sqlite3Result = convertResult(useParamsStackScoped(outCurrent, outHighwater) { curPtr, highPtr ->
    exports.sqlite3_db_status(db.pointer, option.id, curPtr, highPtr, resetFlag)
})

public actual fun sqlite3_db_status64(
    db: sqlite3,
    option: Sqlite3DbStatusOption,
    outCurrent: Int64OutputParam?,
    outHighwater: Int64OutputParam?,
    resetFlag: Int
): Sqlite3Result = convertResult(useParamsStackScoped(outCurrent, outHighwater) { curPtr, highPtr ->
    exports.sqlite3_db_status64(db.pointer, option.id, curPtr, highPtr, resetFlag)
})

public actual fun sqlite3_declare_vtab(
    db: sqlite3,
    sql: String
): Sqlite3Result = convertResult(heapScoped {
    exports.sqlite3_declare_vtab(db.pointer, sql.allocateUtf8Pointer())
})

public actual fun sqlite3_deserialize(
    db: sqlite3,
    schema: String?,
    buffer: Buffer,
    dbSize: Long,
    bufferSize: Long,
    flags: Sqlite3DeserializeFlag?
): Sqlite3Result = convertResult(heapScoped {
    exports.sqlite3_deserialize(
        db.pointer,
        schema.allocateUtf8Pointer(),
        buffer.pointer,
        dbSize.toJsBigInt(),
        bufferSize.toJsBigInt(),
        flags?.value ?: 0
    )
})

public actual fun sqlite3_drop_modules(
    db: sqlite3,
    keep: Array<String>?
): Sqlite3Result = convertResult(heapScoped {
    exports.sqlite3_drop_modules(db.pointer, allocateUtf8Array(keep))
})

public actual fun sqlite3_errcode(db: sqlite3): Sqlite3Result =
    convertResult(exports.sqlite3_errcode(db.pointer))

public actual fun sqlite3_errmsg(db: sqlite3): String? =
    exports.sqlite3_errmsg(db.pointer).toKStringFromUtf8OrNull()

public actual fun sqlite3_error_offset(db: sqlite3): Int =
    exports.sqlite3_error_offset(db.pointer)

public actual fun sqlite3_errstr(resultCode: Int): String? =
    exports.sqlite3_errstr(resultCode).toKStringFromUtf8OrNull()

public actual fun <AppData> sqlite3_exec(
    db: sqlite3,
    sql: String,
    outErrorMessage: Utf8OutputParam?,
    appData: AppData,
    callback: Sqlite3ExecCallback<AppData>?
): Sqlite3Result = convertResult(useMemoryManager {
    heapScoped {
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
})

public actual fun sqlite3_expanded_sql(stmt: sqlite3_stmt): String? {
    val pointer = exports.sqlite3_expanded_sql(stmt.pointer).orNull ?: return null
    val expandedSql = pointer.toKStringFromUtf8()
    exports.sqlite3_free(pointer)
    return expandedSql
}

public actual fun sqlite3_extended_errcode(db: sqlite3): Int =
    exports.sqlite3_extended_errcode(db.pointer)

public actual fun sqlite3_extended_result_codes(
    db: sqlite3,
    enabled: Int
): Sqlite3Result = convertResult(exports.sqlite3_extended_result_codes(db.pointer, enabled))

public actual fun sqlite3_file_control(
    db: sqlite3,
    name: String?,
    opcode: Sqlite3FileControlOpcode
): Sqlite3Result = convertResult(heapScoped {
    exports.sqlite3_file_control(
        db.pointer,
        name.allocateUtf8Pointer(),
        opcode.code,
        NullPtr
    )
})

public actual fun sqlite3_finalize(stmt: sqlite3_stmt): Sqlite3Result =
    stmt.deallocate { exports.sqlite3_finalize(stmt.pointer) }

public actual fun sqlite3_free(buffer: Buffer): Unit =
    exports.sqlite3_free(buffer.pointer)

public actual fun sqlite3_get_autocommit(db: sqlite3): Int =
    exports.sqlite3_get_autocommit(db.pointer)

public actual fun sqlite3_initialize(): Sqlite3Result =
    convertResult(exports.sqlite3_initialize())

public actual fun sqlite3_interrupt(db: sqlite3): Unit =
    exports.sqlite3_interrupt(db.pointer)

public actual fun sqlite3_is_interrupted(db: sqlite3): Int =
    exports.sqlite3_is_interrupted(db.pointer)

public actual fun sqlite3_key(
    db: sqlite3,
    key: ByteArray,
    nKey: Int,
): Sqlite3Result = convertResult(bufferScoped(key) { keyPtr ->
    exports.sqlite3_key(db.pointer, keyPtr, nKey)
})

public actual fun sqlite3_key_v2(
    db: sqlite3,
    dbName: String,
    key: ByteArray,
    nKey: Int,
): Sqlite3Result = convertResult(heapScoped {
    bufferScoped(key, memory) { keyPtr ->
        exports.sqlite3_key_v2(db.pointer, dbName.allocateUtf8Pointer(), keyPtr, nKey)
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
): Sqlite3Result = convertResult(stackScoped {
    useParam(outName) { namePtr ->
        val size = Int32OutputParam(0)

        useParam(size) { sizePtr ->
            exports.sqlite3_keyword_name(index, namePtr, sizePtr)
        }.also {
            outName.size = size.value
        }
    }
})

public actual fun sqlite3_last_insert_rowid(db: sqlite3): Long =
    exports.sqlite3_last_insert_rowid(db.pointer).toLong()

public actual fun sqlite3_libversion(): String =
    exports.sqlite3_libversion().toKStringFromUtf8()

public actual fun sqlite3_libversion_number(db: sqlite3): Int =
    exports.sqlite3_libversion_number()

public actual fun sqlite3_limit(
    db: sqlite3,
    id: Sqlite3Limit,
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
    outDb: Sqlite3OutputParam
): Sqlite3Result = convertResult(heapScoped {
    useParam(outDb) { dbPtr ->
        exports.sqlite3_open(fileName.allocateUtf8Pointer(), dbPtr)
    }
})

public actual fun sqlite3_open_v2(
    fileName: String,
    outDb: Sqlite3OutputParam,
    flags: Sqlite3OpenFlag.Db,
    vfs: String?
): Sqlite3Result = convertResult(heapScoped {
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
): Sqlite3Result = convertResult(heapScoped {
    exports.sqlite3_overload_function(db.pointer, name.allocateUtf8Pointer(), nArg)
})

public actual fun sqlite3_prepare_v2(
    db: sqlite3,
    sql: ByteArray,
    maxBytes: Int,
    outStmt: Sqlite3StmtOutputParam,
    outOffset: Int32OutputParam?
): Sqlite3Result = convertResult(useParamsStackScoped(outStmt, outOffset) { stmtPtr, offsetPtr ->
    bufferScoped(sql, size = maxBytes) { sqlPtr ->
        exports.ksqlite_prepare_v2(db.pointer, sqlPtr, maxBytes, stmtPtr, offsetPtr)
    }
})

public actual fun sqlite3_prepare_v2(
    db: sqlite3,
    sql: String,
    outStmt: Sqlite3StmtOutputParam
): Sqlite3Result = convertResult(heapScoped {
    useParam(outStmt) { stmtPtr ->
        val cSql = sql.allocateUtf8()
        exports.sqlite3_prepare_v2(db.pointer, cSql.pointer, cSql.size, stmtPtr, NullPtr)
    }
})

public actual fun sqlite3_prepare_v3(
    db: sqlite3,
    sql: ByteArray,
    maxBytes: Int,
    flags: Sqlite3PrepareFlag?,
    outStmt: Sqlite3StmtOutputParam,
    outOffset: Int32OutputParam?
): Sqlite3Result = convertResult(useParamsStackScoped(outStmt, outOffset) { stmtPtr, offsetPtr ->
    bufferScoped(sql, size = maxBytes) { sqlPtr ->
        val prepFlags = flags?.value ?: 0
        exports.ksqlite_prepare_v3(db.pointer, sqlPtr, maxBytes, prepFlags, stmtPtr, offsetPtr)
    }
})

public actual fun sqlite3_prepare_v3(
    db: sqlite3,
    sql: String,
    flags: Sqlite3PrepareFlag?,
    outStmt: Sqlite3StmtOutputParam
): Sqlite3Result = convertResult(heapScoped {
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
    callback: Sqlite3PreupdateHookCallback<AppData>?
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
    outValue: Sqlite3ValueOutputParam
): Sqlite3Result = convertResult(useParamStackScoped(outValue) { valuePtr ->
    exports.sqlite3_preupdate_new(db.pointer, index, valuePtr)
})

public actual fun sqlite3_preupdate_old(
    db: sqlite3,
    index: Int,
    outValue: Sqlite3ValueOutputParam
): Sqlite3Result = convertResult(useParamStackScoped(outValue) { valuePtr ->
    exports.sqlite3_preupdate_old(db.pointer, index, valuePtr)
})

public actual fun <AppData> sqlite3_progress_handler(
    db: sqlite3,
    nOps: Int,
    appData: AppData,
    callback: Sqlite3ProgressHandlerCallback<AppData>?
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
): Sqlite3Result = convertResult(bufferScoped(key) { keyPtr ->
    exports.sqlite3_rekey(db.pointer, keyPtr, nKey)
})

public actual fun sqlite3_rekey_v2(
    db: sqlite3,
    dbName: String,
    key: ByteArray,
    nKey: Int,
): Sqlite3Result = convertResult(heapScoped {
    bufferScoped(key, memory) { keyPtr ->
        exports.sqlite3_rekey_v2(db.pointer, dbName.allocateUtf8Pointer(), keyPtr, nKey)
    }
})

public actual fun sqlite3_release_memory(size: Int): Int =
    exports.sqlite3_release_memory(size)

public actual fun sqlite3_reset(stmt: sqlite3_stmt): Sqlite3Result =
    convertResult(exports.sqlite3_reset(stmt.pointer))

public actual fun sqlite3_reset_auto_extension(): Unit =
    autoExtensionReset { exports.sqlite3_reset_auto_extension() }

public actual fun sqlite3_result_blob(
    context: sqlite3_context,
    bytes: ByteArray,
    size: Int,
    destroy: Sqlite3DestroyCallback<ByteArray>?
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
    destroy: Sqlite3DestroyCallback<Buffer>?
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
    result: Sqlite3Result.Failure
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
    destroy: Sqlite3DestroyCallback<Data>?
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
    encoding: Sqlite3TextEncoding.Set1,
    destroy: Sqlite3DestroyCallback<Buffer>?
): Unit = exports.sqlite3_result_text64(
    context.pointer,
    buffer.pointer,
    size.toJsBigInt(),
    bufferDisposer(buffer, destroy),
    encoding.utf8OrThrow().value
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
): Sqlite3Result =
    convertResult(exports.sqlite3_result_zeroblob64(context.pointer, size.toLong().toJsBigInt()))

public actual fun <AppData> sqlite3_rollback_hook(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3RollbackHookCallback<AppData>?
): Unit = db.withMemoryManager {
    val _ = exports.sqlite3_rollback_hook(
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

    val pointer = heapScoped {
        useParam(size) { sizePtr ->
            val mFlags = flags?.value ?: 0
            exports.sqlite3_serialize(db.pointer, schema.allocateUtf8Pointer(), sizePtr, mFlags)
        }
    }

    return Buffer.from(pointer, size.value)
}

public actual fun <AppData> sqlite3_set_authorizer(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3AuthorizerCallback<AppData>?
): Sqlite3Result = convertResult(db.withMemoryManager {
    exports.sqlite3_set_authorizer(
        db.pointer,
        functionPointer(callback, ::AuthorizerHandler),
        keyedStableRefPointer(KEY_SET_AUTHORIZER, callback, appData)
    )
})

public actual fun sqlite3_set_errmsg(
    db: sqlite3,
    errorCode: Sqlite3Result.Failure,
    message: String?
): Sqlite3Result = convertResult(heapScoped {
    exports.sqlite3_set_errmsg(db.pointer, errorCode.code, message.allocateUtf8Pointer())
})

public actual fun sqlite3_set_last_insert_rowid(
    db: sqlite3,
    rowId: Long
): Unit = exports.sqlite3_set_last_insert_rowid(db.pointer, rowId.toJsBigInt())

public actual fun sqlite3_shutdown(): Sqlite3Result =
    convertResult(exports.sqlite3_shutdown())

public actual fun sqlite3_sourceid(): String =
    exports.sqlite3_sourceid().toKStringFromUtf8()

public actual fun sqlite3_sql(stmt: sqlite3_stmt): String =
    exports.sqlite3_sql(stmt.pointer).toKStringFromUtf8()

public actual fun sqlite3_status(
    option: Sqlite3StatusOption,
    outCurrent: Int32OutputParam,
    outHighwater: Int32OutputParam,
    resetFlag: Int
): Sqlite3Result = convertResult(useParamsStackScoped(outCurrent, outHighwater) { curPtr, highPtr ->
    exports.sqlite3_status(option.id, curPtr, highPtr, resetFlag)
})

public actual fun sqlite3_status64(
    option: Sqlite3StatusOption,
    outCurrent: Int64OutputParam,
    outHighwater: Int64OutputParam,
    resetFlag: Int
): Sqlite3Result = convertResult(useParamsStackScoped(outCurrent, outHighwater) { curPtr, highPtr ->
    exports.sqlite3_status64(option.id, curPtr, highPtr, resetFlag)
})

public actual fun sqlite3_step(stmt: sqlite3_stmt): Sqlite3Result =
    convertResult(exports.sqlite3_step(stmt.pointer))

public actual fun sqlite3_stmt_busy(stmt: sqlite3_stmt): Int =
    exports.sqlite3_stmt_busy(stmt.pointer)

public actual fun sqlite3_stmt_explain(
    stmt: sqlite3_stmt,
    mode: Sqlite3ExplainMode
): Sqlite3Result = convertResult(exports.sqlite3_stmt_explain(stmt.pointer, mode.id))

public actual fun sqlite3_stmt_isexplain(stmt: sqlite3_stmt): Sqlite3ExplainMode =
    convertExplainMode(exports.sqlite3_stmt_isexplain(stmt.pointer))

public actual fun sqlite3_stmt_readonly(stmt: sqlite3_stmt): Int =
    exports.sqlite3_stmt_readonly(stmt.pointer)

public actual fun sqlite3_stmt_status(
    stmt: sqlite3_stmt,
    counter: Sqlite3StatementStatusCounter,
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
): Sqlite3Result = convertResult(heapScoped {
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
    mask: Sqlite3TraceCode?,
    appData: AppData,
    callback: Sqlite3TraceCallback<AppData>?
): Sqlite3Result = convertResult(db.withMemoryManager {
    exports.sqlite3_trace_v2(
        db.pointer,
        mask?.value ?: 0,
        functionPointer(callback, ::TraceHandler),
        keyedStableRefPointer(KEY_TRACE, callback, appData)
    )
})

public actual fun sqlite3_txn_state(
    db: sqlite3,
    schema: String?
): Sqlite3TransactionState? = convertTransactionState(heapScoped {
    exports.sqlite3_txn_state(db.pointer, schema.allocateUtf8Pointer())
})

public actual fun <AppData> sqlite3_update_hook(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3UpdateHookCallback<AppData>?
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

public actual fun sqlite3_value_encoding(value: sqlite3_value): Sqlite3TextEncoding.Set2 =
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

public actual fun sqlite3_value_numeric_type(value: sqlite3_value): Sqlite3DataType =
    convertDataType(exports.sqlite3_value_numeric_type(value.pointer))

public actual fun sqlite3_value_subtype(value: sqlite3_value): UInt =
    exports.sqlite3_value_subtype(value.pointer).toUInt()

public actual fun sqlite3_value_text(value: sqlite3_value): String? =
    exports.sqlite3_value_text(value.pointer).toKStringFromUtf8OrNull()

public actual fun sqlite3_value_type(value: sqlite3_value): Sqlite3DataType =
    convertDataType(exports.sqlite3_value_type(value.pointer))

public actual fun sqlite3_vfs_find(name: String?): sqlite3_vfs? = heapScoped {
    exports.sqlite3_vfs_find(name.allocateUtf8Pointer()).orNull?.let(::sqlite3_vfs)
}

public actual fun sqlite3_vfs_register(
    vfs: sqlite3_vfs,
    makeDefault: Int
): Sqlite3Result = convertResult(exports.sqlite3_vfs_register(vfs.pointer, makeDefault))

public actual fun sqlite3_vfs_unregister(vfs: sqlite3_vfs): Sqlite3Result =
    convertResult(exports.sqlite3_vfs_unregister(vfs.pointer))

public actual fun sqlite3_vtab_collation(
    info: sqlite3_index_info,
    index: Int
): String = exports.sqlite3_vtab_collation(info.pointer, index)
    .toKStringFromUtf8()

public actual fun sqlite3_vtab_config(
    db: sqlite3,
    option: Sqlite3VTabConfigOption
): Sqlite3Result = commonVtabConfig(option) { id, values ->
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
    outValue: Sqlite3ValueOutputParam?
): Sqlite3Result = convertResult(useParamStackScoped(outValue) { valuePtr ->
    exports.sqlite3_vtab_in_first(value.pointer, valuePtr)
})

public actual fun sqlite3_vtab_in_next(
    value: sqlite3_value,
    outValue: Sqlite3ValueOutputParam?
): Sqlite3Result = convertResult(useParamStackScoped(outValue) { valuePtr ->
    exports.sqlite3_vtab_in_next(value.pointer, valuePtr)
})

public actual fun sqlite3_vtab_nochange(context: sqlite3_context): Int =
    exports.sqlite3_vtab_nochange(context.pointer)

public actual fun sqlite3_vtab_on_conflict(db: sqlite3): Sqlite3ConflictResolutionMode =
    convertConflictResolutionMode(exports.sqlite3_vtab_on_conflict(db.pointer))

public actual fun sqlite3_vtab_rhs_value(
    info: sqlite3_index_info,
    index: Int,
    outValue: Sqlite3ValueOutputParam?
): Sqlite3Result = convertResult(useParamStackScoped(outValue) { valuePtr ->
    exports.sqlite3_vtab_rhs_value(info.pointer, index, valuePtr)
})