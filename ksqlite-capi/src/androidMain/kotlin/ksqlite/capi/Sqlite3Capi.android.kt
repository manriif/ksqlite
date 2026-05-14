@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi
/*
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.deallocateNullable
import ksqlite.capi.memory.globalMemory
import ksqlite.capi.memory.memory
import ksqlite.capi.memory.useMemoryManager
import ksqlite.capi.memory.withMemoryManager
import ksqlite.capi.memory.wrapOrNull
import ksqlite.capi.types.Int32OutputParam
import ksqlite.capi.types.Int64OutputParam
import ksqlite.capi.types.Sqlite3AutoExtensionCallback
import ksqlite.capi.types.Sqlite3AutoVacuumPagesCallback
import ksqlite.capi.types.Sqlite3BlobOpenFlag
import ksqlite.capi.types.Sqlite3BlobOutputParam
import ksqlite.capi.types.Sqlite3BusyHandlerCallback
import ksqlite.capi.types.Sqlite3CheckpointMode
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
import ksqlite.capi.types.Sqlite3DbConfigOption
import ksqlite.capi.types.Sqlite3DbStatusOption
import ksqlite.capi.types.Sqlite3DeserializeFlag
import ksqlite.capi.types.Sqlite3DestructorCallback
import ksqlite.capi.types.Sqlite3ExecCallback
import ksqlite.capi.types.Sqlite3ExplainMode
import ksqlite.capi.types.Sqlite3FileControlOpcode
import ksqlite.capi.types.Sqlite3FileOpenFlag
import ksqlite.capi.types.Sqlite3Limit
import ksqlite.capi.types.Sqlite3OutputParam
import ksqlite.capi.types.Sqlite3PrepareFlag
import ksqlite.capi.types.Sqlite3PreupdateHookCallback
import ksqlite.capi.types.Sqlite3ProgressHandlerCallback
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.Sqlite3RollbackHookCallback
import ksqlite.capi.types.Sqlite3SerializeFlag
import ksqlite.capi.types.Sqlite3SetAuthorizerCallback
import ksqlite.capi.types.Sqlite3SnapshotOutputParam
import ksqlite.capi.types.Sqlite3StatementStatusCounter
import ksqlite.capi.types.Sqlite3StatusOption
import ksqlite.capi.types.Sqlite3StmtOutputParam
import ksqlite.capi.types.Sqlite3TextEncoding
import ksqlite.capi.types.Sqlite3TraceCallback
import ksqlite.capi.types.Sqlite3TraceCode
import ksqlite.capi.types.Sqlite3TransactionState
import ksqlite.capi.types.Sqlite3UpdateHookCallback
import ksqlite.capi.types.Sqlite3ValueOutputParam
import ksqlite.capi.types.Sqlite3VirtualTableConfigOption
import ksqlite.capi.types.Sqlite3WalHookCallback
import ksqlite.capi.types.Utf8OutputParam
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_backup
import ksqlite.capi.types.sqlite3_blob
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_filename
import ksqlite.capi.types.sqlite3_index_info
import ksqlite.capi.types.sqlite3_module
import ksqlite.capi.types.sqlite3_mutable_pointer
import ksqlite.capi.types.sqlite3_pointer
import ksqlite.capi.types.sqlite3_snapshot
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.capi.types.sqlite3_value
import ksqlite.capi.types.sqlite3_vfs
import ksqlite.capi.types.useParam
import ksqlite.capi.types.useParams
 */
import ksqlite.ksqliteHello
import ksqlite.ksqliteLoadLibrary
//import org.sqlite.jni.capi.CApi as native

public actual fun sqlite3_libversion(): String {
    return ksqliteHello()
}

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
/*
///////////////////////////////////////////////////////////////////////////
// Helpers
///////////////////////////////////////////////////////////////////////////

/**
 * Converts `this` [Boolean] into an [Int].
 */
private fun Boolean.toInt(): Int {
    return if (this) 1 else 0
}

/**
 * Converts `this` [Int] into an [Boolean].
 */
private fun Int.toBoolean(): Boolean {
    return this > 0
}

///////////////////////////////////////////////////////////////////////////
// Functions
///////////////////////////////////////////////////////////////////////////

public actual fun sqlite3_aggregate_context(
    context: sqlite3_context,
    nBytes: Int
): sqlite3_mutable_pointer? = sqlite3_mutable_pointer.from(
    pointer = native.sqlite3_aggregate_context(context.holder, nBytes.toBoolean()),
    size = nBytes.toLong()
)

public actual fun sqlite3_auto_extension(callback: Sqlite3AutoExtensionCallback): Sqlite3Result =
    autoExtensionRegister(callback) { native.ksqlite_auto_extension(SharedAutoExtensionHandler) }

public actual fun sqlite3_autovacuum_pages(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    destructor: Sqlite3DestructorCallback?,
    callback: Sqlite3AutoVacuumPagesCallback?
): Sqlite3Result = convertResult(db.withMemoryManager {
    native.sqlite3_autovacuum_pages(
        db.pointer,
        functionPointer(callback, ::AutoVacuumPagesHandler),
        keyedStableRefPointer(KEY_AUTOVACUUM_PAGES, callback, userData, destructor),
        stableRefDisposer(callback, destructor)
    )
})

public actual fun sqlite3_backup_finish(backup: sqlite3_backup): Sqlite3Result = convertResult(
    native.sqlite3_backup_finish(backup.holder)
)

public actual fun sqlite3_backup_init(
    destDb: sqlite3,
    destDbName: String,
    srcDb: sqlite3,
    srcDbName: String
): sqlite3_backup? = native.sqlite3_backup_init(
    destDb.holder,
    destDbName,
    srcDb.holder,
    srcDbName
).wrapOrNull(::sqlite3_backup)

public actual fun sqlite3_backup_pagecount(backup: sqlite3_backup): Int =
    native.sqlite3_backup_pagecount(backup.holder)

public actual fun sqlite3_backup_remaining(backup: sqlite3_backup): Int =
    native.sqlite3_backup_remaining(backup.holder)

public actual fun sqlite3_backup_step(
    backup: sqlite3_backup,
    nPage: Int
): Sqlite3Result = convertResult(native.sqlite3_backup_step(backup.holder, nPage))

public actual fun sqlite3_bind_blob(
    stmt: sqlite3_stmt,
    index: Int,
    data: ByteArray?,
    size: Int,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(
    native.sqlite3_bind_blob(
        stmt.holder,
        index,
        stmt.memory.byteArrayPointer(data, destructor),
        size,
        globalDisposer(data)
    )
)

public actual fun sqlite3_bind_blob64(
    stmt: sqlite3_stmt,
    index: Int,
    data: sqlite3_mutable_pointer?,
    size: Long,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(
    native.sqlite3_bind_blob64(
        stmt.pointer,
        index,
        data?.block?.pointer.notNull,
        size,
        userDataDisposer(data, destructor)
    )
)

public actual fun sqlite3_bind_double(
    stmt: sqlite3_stmt,
    index: Int,
    value: Double
): Sqlite3Result = convertResult(native.sqlite3_bind_double(stmt.holder, index, value))

public actual fun sqlite3_bind_int(
    stmt: sqlite3_stmt,
    index: Int,
    value: Int
): Sqlite3Result = convertResult(native.sqlite3_bind_int(stmt.holder, index, value))

public actual fun sqlite3_bind_int64(
    stmt: sqlite3_stmt,
    index: Int,
    value: Long
): Sqlite3Result = convertResult(native.sqlite3_bind_int64(stmt.holder, index, value))

public actual fun sqlite3_bind_null(
    stmt: sqlite3_stmt,
    index: Int
): Sqlite3Result = convertResult(native.sqlite3_bind_null(stmt.holder, index))

public actual fun sqlite3_bind_parameter_count(stmt: sqlite3_stmt): Int =
    native.sqlite3_bind_parameter_count(stmt.holder)

public actual fun sqlite3_bind_parameter_index(
    stmt: sqlite3_stmt,
    name: String
): Int = native.sqlite3_bind_parameter_index(stmt.holder, name)

public actual fun sqlite3_bind_parameter_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native.sqlite3_bind_parameter_name(stmt.holder, index)

public actual fun sqlite3_bind_pointer(
    stmt: sqlite3_stmt,
    index: Int,
    data: sqlite3_mutable_pointer?,
    type: String?,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(with(globalMemory) {
    // Use globalMemory because of lack of information within sqlite3_value_pointer()
    allocateNamedPointer(type, destructor) {
        native.sqlite3_bind_java_object(
            stmt.pointer,
            index,
            stableRefPointer(this, data, disposer),
            typePointer.notNull,
            stableRefDisposer(this, disposer)
        )
    }
})

public actual fun sqlite3_bind_text(
    stmt: sqlite3_stmt,
    index: Int,
    text: String?,
    size: Int?
): Sqlite3Result = convertResult(memScoped {
    val cText = text?.allocateUtf8()
    val nByte = size ?: cText?.byteSize()?.toInt() ?: 0
    native.sqlite3_bind_text(stmt.holder, index, cText.notNull, nByte, SqliteTransient)
})

public actual fun sqlite3_bind_text64(
    stmt: sqlite3_stmt,
    index: Int,
    data: sqlite3_mutable_pointer?,
    size: Long,
    encoding: Sqlite3TextEncoding.Set1,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(
    native.sqlite3_bind_text64(
        stmt.pointer,
        index,
        data?.block?.pointer.notNull,
        size,
        userDataDisposer(data, destructor),
        encoding.utf8OrThrow().value.toByte()
    )
)

public actual fun sqlite3_bind_value(
    stmt: sqlite3_stmt,
    index: Int,
    value: sqlite3_value
): Sqlite3Result = convertResult(native.sqlite3_bind_value(stmt.holder, index, value.holder))

public actual fun sqlite3_bind_zeroblob(
    stmt: sqlite3_stmt,
    index: Int,
    size: Int
): Sqlite3Result = convertResult(native.sqlite3_bind_zeroblob(stmt.holder, index, size))

public actual fun sqlite3_bind_zeroblob64(
    stmt: sqlite3_stmt,
    index: Int,
    size: ULong
): Sqlite3Result = convertResult(native.sqlite3_bind_zeroblob64(stmt.holder, index, size.toLong()))

public actual fun sqlite3_blob_bytes(blob: sqlite3_blob): Int =
    native.sqlite3_blob_bytes(blob.holder)

public actual fun sqlite3_blob_close(blob: sqlite3_blob): Sqlite3Result =
    convertResult(native.sqlite3_blob_close(blob.holder))

public actual fun sqlite3_blob_open(
    db: sqlite3,
    databaseName: String,
    tableName: String,
    columnName: String,
    rowIndex: Long,
    flags: Sqlite3BlobOpenFlag,
    outBlob: Sqlite3BlobOutputParam
): Sqlite3Result = convertResult(
    useParam(outBlob) { blobPtr ->
        native.sqlite3_blob_open(
            db.holder,
            databaseName,
            tableName,
            columnName,
            rowIndex,
            flags.value,
            blobPtr
        )
    }
)

public actual fun sqlite3_blob_read(
    blob: sqlite3_blob,
    buffer: ByteArray,
    size: Int,
    offset: Int
): Sqlite3Result =
    convertResult(native.sqlite3_blob_read(blob.holder, buffer.backing(), size, offset))

public actual fun sqlite3_blob_reopen(
    blob: sqlite3_blob,
    rowIndex: Long
): Sqlite3Result = convertResult(native.sqlite3_blob_reopen(blob.holder, rowIndex))

public actual fun sqlite3_blob_write(
    blob: sqlite3_blob,
    buffer: ByteArray,
    size: Int?,
    offset: Int
): Sqlite3Result = convertResult(
    native.sqlite3_blob_write(blob.pointer, buffer.backing(), size ?: buffer.size, offset)
)

public actual fun sqlite3_busy_handler(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3BusyHandlerCallback?
): Sqlite3Result = convertResult(db.withMemoryManager {
    native.sqlite3_busy_handler(
        db.holder,
        functionPointer(callback, ::BusyHandlerHandler),
        keyedStableRefPointer(KEY_BUSY_HANDLER, callback, userData)
    )
})

public actual fun sqlite3_busy_timeout(
    db: sqlite3,
    millis: Int
): Sqlite3Result = convertResult(native.sqlite3_busy_timeout(db.holder, millis))

public actual fun sqlite3_cancel_auto_extension(callback: Sqlite3AutoExtensionCallback): Int =
    autoExtensionUnregister(callback) {
        native.ksqlite_cancel_auto_extension(SharedAutoExtensionHandler)
    }

public actual fun sqlite3_changes(db: sqlite3): Int =
    native.sqlite3_changes(db.holder)

public actual fun sqlite3_changes64(db: sqlite3): Long =
    native.sqlite3_changes64(db.holder)

public actual fun sqlite3_clear_bindings(stmt: sqlite3_stmt): Sqlite3Result =
    commonSqlite3ClearBindings(stmt, native.sqlite3_clear_bindings(stmt.holder))

public actual fun sqlite3_close(db: sqlite3?): Sqlite3Result =
    db.deallocateNullable { native.sqlite3_close(it?.holder) }

public actual fun sqlite3_close_v2(db: sqlite3?): Sqlite3Result =
    db.deallocateNullable { native.sqlite3_close_v2(it?.holder) }

public actual fun sqlite3_collation_needed(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3CollationNeededCallback?,
): Sqlite3Result = convertResult(db.withMemoryManager {
    native.sqlite3_collation_needed(
        db.pointer,
        keyedStableRefPointer(KEY_COLLATION_NEEDED, callback, userData),
        functionPointer(callback, ::CollationNeededHandler)
    )
})

public actual fun sqlite3_column_blob(
    stmt: sqlite3_stmt,
    index: Int
): sqlite3_pointer? = sqlite3_pointer.from(
    pointer = native.sqlite3_column_blob(stmt.holder, index),
    size = native.sqlite3_column_bytes(stmt.holder, index).toLong()
)

public actual fun sqlite3_column_bytes(
    stmt: sqlite3_stmt,
    index: Int
): Int = native.sqlite3_column_bytes(stmt.holder, index)

public actual fun sqlite3_column_count(stmt: sqlite3_stmt): Int =
    native.sqlite3_column_count(stmt.holder)

public actual fun sqlite3_column_database_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native.sqlite3_column_database_name(stmt.holder, index)

public actual fun sqlite3_column_decltype(
    stmt: sqlite3_stmt,
    index: Int
): String? = native.sqlite3_column_decltype(stmt.holder, index)

public actual fun sqlite3_column_double(
    stmt: sqlite3_stmt,
    index: Int
): Double = native.sqlite3_column_double(stmt.holder, index)

public actual fun sqlite3_column_int(
    stmt: sqlite3_stmt,
    index: Int
): Int = native.sqlite3_column_int(stmt.holder, index)

public actual fun sqlite3_column_int64(
    stmt: sqlite3_stmt,
    index: Int
): Long = native.sqlite3_column_int64(stmt.holder, index)

public actual fun sqlite3_column_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native.sqlite3_column_name(stmt.holder, index)

public actual fun sqlite3_column_origin_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native.sqlite3_column_origin_name(stmt.holder, index)

public actual fun sqlite3_column_table_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native.sqlite3_column_table_name(stmt.holder, index)

public actual fun sqlite3_column_text(
    stmt: sqlite3_stmt,
    index: Int
): String? = native.sqlite3_column_text(stmt.holder, index)
    .toString(Charsets.UTF_8)

public actual fun sqlite3_column_type(
    stmt: sqlite3_stmt,
    index: Int
): Sqlite3DataType = convertDataType(native.sqlite3_column_type(stmt.holder, index))

public actual fun sqlite3_column_value(
    stmt: sqlite3_stmt,
    index: Int
): sqlite3_value? = native.sqlite3_column_value(stmt.holder, index)
    .wrapOrNull(::sqlite3_value)

public actual fun sqlite3_commit_hook(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3CommitHookCallback?
): sqlite3_mutable_pointer? = db.withMemoryManager {
    sqlite3_mutable_pointer.fromStableRef(
        native.sqlite3_commit_hook(
            db.holder,
            functionPointer(callback, ::CommitHookHandler),
            keyedStableRefPointer(KEY_COMMIT_HOOK, callback, userData)
        )
    )
}

public actual fun sqlite3_compileoption_get(index: Int): String? =
    native.sqlite3_compileoption_get(index)

public actual fun sqlite3_compileoption_used(optName: String): Int =
    native.sqlite3_compileoption_used(optName).toInt()

public actual fun sqlite3_complete(sql: String): Sqlite3CompleteResult =
    convertCompleteResult(native.sqlite3_complete(sql))

public actual fun sqlite3_config(option: Sqlite3ConfigOption): Sqlite3Result = commonSqlite3Config(
    option = option,
    logFunctionPointer = { globalMemory.functionPointer(it, ::ConfigLogHandler) },
    sqllogFunctionPointer = { globalMemory.functionPointer(it, ::ConfigSqlLogHandler) },
    memoryPointer = { it.block.pointer },
    keyedStableRefPointer = MemoryManager::keyedStableRefPointer,
    rowidInView = {
        useParamMemScoped(param) { paramPtr ->
            native.sqlite3_config
                .makeInvoker(ValueLayout.ADDRESS)
                .apply(id, paramPtr)
        }
    },
    nativeConfig = { id, values ->
        invokeVariadic(values) { layouts, arguments ->
            native.sqlite3_config
                .makeInvoker(*layouts)
                .apply(id, *arguments)
        }
    }
)

public actual fun sqlite3_context_db_handle(context: sqlite3_context): sqlite3? =
    native.sqlite3_context_db_handle(context.holder).wrapOrNull(::sqlite3)

public actual fun sqlite3_create_collation(
    db: sqlite3,
    name: String,
    encoding: Sqlite3TextEncoding.Set0,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3CreateCollationCallback?
): Sqlite3Result = convertResult(db.withMemoryManager {
    memScoped {
        native.sqlite3_create_collation(
            db.pointer,
            name.allocateUtf8(),
            encoding.utf8OrThrow().value,
            keyedStableRefPointer(KEY_CREATE_COLLATION, callback, userData),
            functionPointer(callback, ::CreateCollationHandler),
        )
    }
})

public actual fun sqlite3_create_collation_v2(
    db: sqlite3,
    name: String,
    encoding: Sqlite3TextEncoding.Set0,
    userData: sqlite3_mutable_pointer?,
    destructor: Sqlite3DestructorCallback?,
    callback: Sqlite3CreateCollationCallback?
): Sqlite3Result = convertResult(db.withMemoryManager {
    memScoped {
        native.sqlite3_create_collation_v2(
            db.pointer,
            name.allocateUtf8(),
            encoding.utf8OrThrow().value,
            keyedStableRefPointer(KEY_CREATE_COLLATION, callback, userData, destructor),
            functionPointer(callback, ::CreateCollationHandler),
            stableRefDisposer(callback, destructor)
        )
    }
})

public actual fun sqlite3_create_function(
    db: sqlite3,
    name: String,
    nArg: Int,
    encoding: Sqlite3TextEncoding,
    userData: sqlite3_mutable_pointer?,
    func: Sqlite3CreateFunctionFuncCallback?,
    step: Sqlite3CreateFunctionStepCallback?,
    final: Sqlite3CreateFunctionFinalCallback?
): Sqlite3Result = convertResult(db.withMemoryManager {
    memScoped {
        native.sqlite3_create_function(
            db.pointer,
            name.allocateUtf8(),
            nArg,
            encoding.utf8OrThrow().value,
            keyedStableRefPointer(
                key = uniqueFunctionKey(name, nArg, encoding),
                data = CreateFunction(func = func, step = step, final = final),
                userData = userData
            ),
            functionPointer(func, ::CreateFunctionFuncHandler),
            functionPointer(step, ::CreateFunctionStepHandler),
            functionPointer(final, ::CreateFunctionFinalHandler)
        )
    }
})

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
): Sqlite3Result = convertResult(db.withMemoryManager {
    memScoped {
        native.sqlite3_create_function_v2(
            db.pointer,
            name.allocateUtf8(),
            nArg,
            encoding.utf8OrThrow().value,
            keyedStableRefPointer(
                key = uniqueFunctionKey(name, nArg, encoding),
                data = CreateFunction(func = func, step = step, final = final),
                userData = userData
            ),
            functionPointer(func, ::CreateFunctionFuncHandler),
            functionPointer(step, ::CreateFunctionStepHandler),
            functionPointer(final, ::CreateFunctionFinalHandler),
            stableRefDisposer(0, destructor)
        )
    }
})

public actual fun sqlite3_create_module(
    db: sqlite3,
    name: String,
    module: sqlite3_module?,
    userData: sqlite3_mutable_pointer?
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_create_module(
        db.pointer,
        name.allocateUtf8(),
        module?.pointer.notNull,
        userData?.block?.pointer.notNull
    )
})

public actual fun sqlite3_create_module_v2(
    db: sqlite3,
    name: String,
    module: sqlite3_module?,
    userData: sqlite3_mutable_pointer?,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_create_module_v2(
        db.pointer,
        name.allocateUtf8(),
        module?.pointer.notNull,
        userData?.block?.pointer.notNull,
        userDataDisposer(userData, destructor)
    )
})

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
): Sqlite3Result = convertResult(db.withMemoryManager {
    memScoped {
        native.sqlite3_create_window_function(
            db.pointer,
            name.allocateUtf8(),
            nArg,
            encoding.utf8OrThrow().value,
            keyedStableRefPointer(
                key = uniqueFunctionKey(name, nArg, encoding),
                data = CreateFunction(step = step, final = final, value = value, inverse = inverse),
                userData = userData
            ),
            functionPointer(step, ::CreateFunctionStepHandler),
            functionPointer(final, ::CreateFunctionFinalHandler),
            functionPointer(value, ::CreateFunctionValueHandler),
            functionPointer(inverse, ::CreateFunctionInverseHandler),
            stableRefDisposer(0, destructor)
        )
    }
})

public actual fun sqlite3_data_count(stmt: sqlite3_stmt): Int =
    native.sqlite3_data_count(stmt.pointer)

public actual fun sqlite3_db_cacheflush(db: sqlite3): Sqlite3Result =
    convertResult(native.sqlite3_db_cacheflush(db.pointer))

public actual fun sqlite3_db_config(
    db: sqlite3,
    option: Sqlite3DbConfigOption,
): Sqlite3Result = commonSqlite3DbConfig(
    option = option,
    memoryPointer = { it.block.pointer },
    outParamConfig = {
        useParamMemScoped(state) { statePtr ->
            native.sqlite3_db_config
                .makeInvoker(ValueLayout.ADDRESS)
                .apply(db.pointer, id, value, statePtr)
        }
    },
    nativeConfig = { id, values ->
        invokeVariadic(values) { layouts, arguments ->
            native.sqlite3_db_config
                .makeInvoker(*layouts)
                .apply(db.pointer, id, *arguments)
        }
    }
)

public actual fun sqlite3_db_filename(
    db: sqlite3,
    name: String
): sqlite3_filename? = native.sqlite3_db_filename(db.holder, name)

public actual fun sqlite3_db_handle(stmt: sqlite3_stmt): sqlite3? =
    native.sqlite3_db_handle(stmt.holder).wrapOrNull(::sqlite3)

public actual fun sqlite3_db_name(
    db: sqlite3,
    index: Int
): String? = native.sqlite3_db_name(db.holder, index)

public actual fun sqlite3_db_readonly(
    db: sqlite3,
    name: String
): Int = native.sqlite3_db_readonly(db.holder, name)

public actual fun sqlite3_db_release_memory(db: sqlite3): Sqlite3Result =
    convertResult(native.sqlite3_db_release_memory(db.holder))

public actual fun sqlite3_db_status(
    db: sqlite3,
    option: Sqlite3DbStatusOption,
    outCurrent: Int32OutputParam?,
    outHighwater: Int32OutputParam?,
    resetFlag: Int
): Sqlite3Result = convertResult(useParams(outCurrent, outHighwater) { curPtr, highPtr ->
    native.sqlite3_db_status(db.holder, option.id, curPtr, highPtr, resetFlag > 0)
})

public actual fun sqlite3_db_status64(
    db: sqlite3,
    option: Sqlite3DbStatusOption,
    outCurrent: Int64OutputParam?,
    outHighwater: Int64OutputParam?,
    resetFlag: Int
): Sqlite3Result = convertResult(useParams(outCurrent, outHighwater) { curPtr, highPtr ->
    native.sqlite3_db_status64(db.pointer, option.id, curPtr, highPtr, resetFlag > 0)
})

public actual fun sqlite3_declare_vtab(
    db: sqlite3,
    sql: String
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_declare_vtab(db.holder, sql)
})

public actual fun sqlite3_deserialize(
    db: sqlite3,
    schema: String?,
    data: sqlite3_mutable_pointer?,
    dbSize: Long,
    dataSize: Long,
    flags: Sqlite3DeserializeFlag?
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_deserialize(
        db.pointer,
        schema.allocateUtf8(),
        data?.block?.pointer.notNull,
        dbSize,
        dataSize,
        flags?.value ?: 0
    )
})

public actual fun sqlite3_drop_modules(
    db: sqlite3,
    keep: Array<String>?
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_drop_modules(db.holder, keep.allocateUtf8Array())
})

public actual fun sqlite3_errcode(db: sqlite3): Int =
    native.sqlite3_errcode(db.holder)

public actual fun sqlite3_errmsg(db: sqlite3): String? =
    native.sqlite3_errmsg(db.holder)

public actual fun sqlite3_errstr(resultCode: Int): String? =
    native.sqlite3_errstr(resultCode)

public actual fun sqlite3_error_offset(db: sqlite3): Int =
    native.sqlite3_error_offset(db.holder)

public actual fun sqlite3_exec(
    db: sqlite3,
    sql: String,
    outErrorMessage: Utf8OutputParam?,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3ExecCallback?
): Sqlite3Result = convertResult(useMemoryManager {
    memScoped {
        useParam(outErrorMessage) { errorMessagePtr ->
            native.sqlite3_exec(
                db.pointer,
                sql.allocateUtf8(),
                functionPointer(callback, ::ExecHandler),
                stableRefPointer(callback, userData),
                errorMessagePtr
            )
        }
    }
})

public actual fun sqlite3_expanded_sql(stmt: sqlite3_stmt): String? {
    val pointer = native.sqlite3_expanded_sql(stmt.holder) ?: return null
    val expandedSql = pointer.toKStringFromUtf8()
    native.sqlite3_free(pointer)
    return expandedSql
}

public actual fun sqlite3_extended_errcode(db: sqlite3): Int =
    native.sqlite3_extended_errcode(db.holder)

public actual fun sqlite3_extended_result_codes(
    db: sqlite3,
    enabled: Int
): Sqlite3Result = convertResult(native.sqlite3_extended_result_codes(db.holder, enabled > 0))

public actual fun sqlite3_file_control(
    db: sqlite3,
    name: String?,
    opcode: Sqlite3FileControlOpcode,
    userData: sqlite3_mutable_pointer?
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_file_control(
        db.pointer,
        name.allocateUtf8(),
        opcode.code,
        userData?.block?.pointer.notNull
    )
})

public actual fun sqlite3_finalize(stmt: sqlite3_stmt?): Sqlite3Result =
    stmt.deallocateNullable { native.sqlite3_finalize(stmt?.holder) }

public actual fun sqlite3_free(data: sqlite3_mutable_pointer?): Unit =
    native.sqlite3_free(data?.block?.pointer.notNull)

public actual fun sqlite3_get_autocommit(db: sqlite3): Int =
    native.sqlite3_get_autocommit(db.holder).toInt()

public actual fun sqlite3_get_auxdata(
    context: sqlite3_context,
    index: Int
): sqlite3_mutable_pointer? = native.sqlite3_get_auxdata(context.holder, index).orNull
    ?.let { context.db.memory.stableRefData<sqlite3_mutable_pointer>(it).first }

public actual fun sqlite3_hard_heap_limit64(limit: Long): Long =
    native.sqlite3_hard_heap_limit64(limit)

public actual fun sqlite3_initialize(): Sqlite3Result =
    convertResult(native.sqlite3_initialize())

public actual fun sqlite3_interrupt(db: sqlite3): Unit =
    native.sqlite3_interrupt(db.holder)

public actual fun sqlite3_is_interrupted(db: sqlite3): Int =
    native.sqlite3_is_interrupted(db.holder).toInt()

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

public actual fun sqlite3_keyword_count(): Int =
    native.sqlite3_keyword_count()

public actual fun sqlite3_keyword_name(
    index: Int,
    outName: Utf8OutputParam,
): Sqlite3Result = useParam(outName) { namePtr ->
    val result = native.sqlite3_keyword_name(index)
    namePtr?.set(result)

    if (result == null) {
        Sqlite3Result.ERROR
    } else {
        Sqlite3Result.OK
    }
}

public actual fun sqlite3_keyword_check(
    word: String,
    size: Int?
): Int {
    val nByte = size ?: cWord.byteSize().toInt()
    return native.sqlite3_keyword_check(word, nByte).toInt()
}

public actual fun sqlite3_last_insert_rowid(db: sqlite3): Long =
    native.sqlite3_last_insert_rowid(db.pointer)

public actual fun sqlite3_libversion(): String =
    native.sqlite3_libversion()

public actual fun sqlite3_libversion_number(db: sqlite3): Int =
    native.sqlite3_libversion_number()

public actual fun sqlite3_limit(
    db: sqlite3,
    id: Sqlite3Limit,
    newVal: Int
): Int = native.sqlite3_limit(db.holder, id.id, newVal)

public actual fun sqlite3_log(
    errCode: Int,
    message: String
): Unit = memScoped {
    native.sqlite3_log(errCode, message.allocateUtf8())
}

public actual fun sqlite3_malloc(size: Int): sqlite3_mutable_pointer? =
    native.sqlite3_malloc(size).orNull?.let { sqlite3_mutable_pointer.from(it, size.toLong()) }

public actual fun sqlite3_malloc64(size: Long): sqlite3_mutable_pointer? =
    native.sqlite3_malloc64(size).orNull?.let { sqlite3_mutable_pointer.from(it, size) }

public actual fun sqlite3_memory_used(): Long =
    native.sqlite3_memory_used()

public actual fun sqlite3_memory_highwater(resetFlag: Int): Long =
    native.sqlite3_memory_highwater(resetFlag)

public actual fun sqlite3_msize(data: sqlite3_mutable_pointer?): ULong =
    native.sqlite3_msize(data?.block?.pointer.notNull).toULong()

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
    flags: Sqlite3FileOpenFlag.Valid,
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
    sql: String,
    size: Int?,
    outStmt: Sqlite3StmtOutputParam,
    outTail: Utf8OutputParam?
): Sqlite3Result = convertResult(memScoped {
    useParams(outStmt, outTail) { stmtPtr, tailPtr ->
        val cSql = sql.allocateUtf8()
        val nByte = size ?: cSql.byteSize().toInt()
        native.sqlite3_prepare_v2(db.pointer, cSql, nByte, stmtPtr, tailPtr)
    }
})

public actual fun sqlite3_prepare_v3(
    db: sqlite3,
    sql: String,
    size: Int?,
    flags: Sqlite3PrepareFlag?,
    outStmt: Sqlite3StmtOutputParam,
    outTail: Utf8OutputParam?
): Sqlite3Result = convertResult(memScoped {
    useParams(outStmt, outTail) { stmtPtr, tailPtr ->
        val cSql = sql.allocateUtf8()
        val nByte = size ?: cSql.byteSize().toInt()
        val prepFlags = flags?.value ?: 0
        native.sqlite3_prepare_v3(db.pointer, cSql, nByte, prepFlags, stmtPtr, tailPtr)
    }
})

public actual fun sqlite3_preupdate_blobwrite(db: sqlite3): Int =
    native.sqlite3_preupdate_blobwrite(db.holder)

public actual fun sqlite3_preupdate_count(db: sqlite3): Int =
    native.sqlite3_preupdate_count(db.holder)

public actual fun sqlite3_preupdate_depth(db: sqlite3): Int =
    native.sqlite3_preupdate_depth(db.holder)

public actual fun sqlite3_preupdate_hook(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3PreupdateHookCallback?
): sqlite3_mutable_pointer? = db.withMemoryManager {
    sqlite3_mutable_pointer.fromStableRef(
        native.sqlite3_preupdate_hook(
            db.pointer,
            functionPointer(callback, ::PreupdateHookHandler),
            keyedStableRefPointer(KEY_PREUPDATE_HOOK, callback, userData)
        )
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

public actual fun sqlite3_progress_handler(
    db: sqlite3,
    nOps: Int,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3ProgressHandlerCallback?
): Unit = db.withMemoryManager {
    native.sqlite3_progress_handler(
        db.pointer,
        nOps,
        functionPointer(callback, ::ProgressHandlerHandler),
        keyedStableRefPointer(KEY_PROGRESS_HANDLER, callback, userData)
    )
}

public actual fun sqlite3_randomness(
    size: Int,
    data: sqlite3_mutable_pointer?
): Unit = native.sqlite3_randomness(size, data?.block?.pointer.notNull)

public actual fun sqlite3_realloc(
    data: sqlite3_mutable_pointer?,
    size: Int
): sqlite3_mutable_pointer? = sqlite3_mutable_pointer.from(
    pointer = native.sqlite3_realloc(data?.block?.pointer.notNull, size),
    size = size.toLong()
)

public actual fun sqlite3_realloc64(
    data: sqlite3_mutable_pointer?,
    size: Long
): sqlite3_mutable_pointer? = sqlite3_mutable_pointer.from(
    pointer = native.sqlite3_realloc64(data?.block?.pointer.notNull, size),
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
    convertResult(native.sqlite3_reset(stmt.holder))

public actual fun sqlite3_reset_auto_extension(): Unit =
    autoExtensionReset { native.sqlite3_reset_auto_extension() }

public actual fun sqlite3_result_blob(
    context: sqlite3_context,
    data: ByteArray?,
    size: Int,
    destructor: Sqlite3DestructorCallback?
): Unit = native.sqlite3_result_blob(
    context.pointer,
    context.db.memory.byteArrayPointer(data, destructor),
    size,
    globalDisposer(data)
)

public actual fun sqlite3_result_blob64(
    context: sqlite3_context,
    data: sqlite3_mutable_pointer?,
    size: Long,
    destructor: Sqlite3DestructorCallback?
): Unit = native.sqlite3_result_blob64(
    context.pointer,
    data?.block?.pointer.notNull,
    size,
    userDataDisposer(data, destructor)
)

public actual fun sqlite3_result_double(
    context: sqlite3_context,
    value: Double
): Unit = native.sqlite3_result_double(context.holder, value)

public actual fun sqlite3_result_error(
    context: sqlite3_context,
    message: String?,
    size: Int?
): Unit = memScoped {
    val cMessage = message?.allocateUtf8()
    val nByte = size ?: cMessage?.byteSize()?.toInt() ?: 0
    native.sqlite3_result_error(context.holder, cMessage.notNull, nByte)
}

public actual fun sqlite3_result_error_code(
    context: sqlite3_context,
    code: Int
): Unit = native.sqlite3_result_error_code(context.holder, code)

public actual fun sqlite3_result_error_nomem(context: sqlite3_context): Unit =
    native.sqlite3_result_error_nomem(context.holder)

public actual fun sqlite3_result_error_toobig(context: sqlite3_context): Unit =
    native.sqlite3_result_error_toobig(context.holder)

public actual fun sqlite3_result_int(
    context: sqlite3_context,
    value: Int
): Unit = native.sqlite3_result_int(context.holder, value)

public actual fun sqlite3_result_int64(
    context: sqlite3_context,
    value: Long
): Unit = native.sqlite3_result_int64(context.holder, value)

public actual fun sqlite3_result_null(context: sqlite3_context): Unit =
    native.sqlite3_result_null(context.holder)

public actual fun sqlite3_result_pointer(
    context: sqlite3_context,
    data: sqlite3_mutable_pointer?,
    type: String?,
    destructor: Sqlite3DestructorCallback?
): Unit = with(globalMemory) {
    // Use globalMemory because of lack of information within sqlite3_value_pointer()
    allocateNamedPointer(type, destructor) {
        native.sqlite3_result_pointer(
            context.pointer,
            stableRefPointer(this, data, this.disposer),
            typePointer.notNull,
            stableRefDisposer(0, this.disposer)
        )
    }
}

public actual fun sqlite3_result_subtype(
    context: sqlite3_context,
    subtype: UInt
): Unit = native.sqlite3_result_subtype(context.holder, subtype.toInt())

public actual fun sqlite3_result_text(
    context: sqlite3_context,
    text: String?,
    size: Int?
): Unit = memScoped {
    val cText = text?.allocateUtf8()
    val nByte = size ?: cText?.byteSize()?.toInt() ?: 0
    native.sqlite3_result_text(context.pointer, cText.notNull, nByte, SqliteTransient)
}

public actual fun sqlite3_result_text64(
    context: sqlite3_context,
    data: sqlite3_mutable_pointer?,
    size: Long,
    encoding: Sqlite3TextEncoding.Set1,
    destructor: Sqlite3DestructorCallback?
): Unit = native.sqlite3_result_text64(
    context.pointer,
    data?.block?.pointer.notNull,
    size,
    userDataDisposer(data, destructor),
    encoding.utf8OrThrow().value.toByte()
)

public actual fun sqlite3_result_value(
    context: sqlite3_context,
    value: sqlite3_value,
): Unit = native.sqlite3_result_value(context.holder, value.holder)

public actual fun sqlite3_result_zeroblob(
    context: sqlite3_context,
    size: Int
): Unit = native.sqlite3_result_zeroblob(context.holder, size)

public actual fun sqlite3_result_zeroblob64(
    context: sqlite3_context,
    size: ULong
): Int = native.sqlite3_result_zeroblob64(context.holder, size.toLong())

public actual fun sqlite3_rollback_hook(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3RollbackHookCallback?
): sqlite3_mutable_pointer? = db.withMemoryManager {
    sqlite3_mutable_pointer.fromStableRef(
        native.sqlite3_rollback_hook(
            db.pointer,
            functionPointer(callback, ::RollbackHookHandler),
            keyedStableRefPointer(KEY_ROLLBACK_HOOK, callback, userData)
        )
    )
}

public actual fun sqlite3_serialize(
    db: sqlite3,
    schema: String?,
    flags: Sqlite3SerializeFlag?
): sqlite3_mutable_pointer? {
    val size = Int64OutputParam(0)

    val pointer = memScoped {
        useParam(size) { sizePtr ->
            val mFlags = flags?.value ?: 0
            native.sqlite3_serialize(db.pointer, schema.allocateUtf8(), sizePtr, mFlags)
        }
    }

    return sqlite3_mutable_pointer.from(pointer, size.value)
}

public actual fun sqlite3_set_authorizer(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3SetAuthorizerCallback?
): Sqlite3Result = convertResult(db.withMemoryManager {
    native.sqlite3_set_authorizer(
        db.pointer,
        functionPointer(callback, ::SetAuthorizerHandler),
        keyedStableRefPointer(KEY_SET_AUTHORIZER, callback, userData)
    )
})

public actual fun sqlite3_set_auxdata(
    context: sqlite3_context,
    index: Int,
    data: sqlite3_mutable_pointer?,
    destructor: Sqlite3DestructorCallback?
): Unit = context.db.withMemoryManager {
    native.sqlite3_set_auxdata(
        context.pointer,
        index,
        keyedStableRefPointer(auxDataKey(index), data, data, destructor),
        stableRefDisposer(data, destructor)
    )
}

public actual fun sqlite3_set_errmsg(
    db: sqlite3,
    errorCode: Sqlite3Result.Failure,
    message: String?
): Sqlite3Result = convertResult(native.sqlite3_set_errmsg(db.holder, errorCode.code, message))

public actual fun sqlite3_set_last_insert_rowid(
    db: sqlite3,
    rowId: Long
): Unit = native.sqlite3_set_last_insert_rowid(db.holder, rowId)

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
    native.sqlite3_sourceid()

public actual fun sqlite3_sql(stmt: sqlite3_stmt): String =
    native.sqlite3_sql(stmt.holder)

public actual fun sqlite3_status(
    option: Sqlite3StatusOption,
    outCurrent: Int32OutputParam,
    outHighwater: Int32OutputParam,
    resetFlag: Int
): Sqlite3Result = convertResult(useParams(outCurrent, outHighwater) { curPtr, highPtr ->
    native.sqlite3_status(option.id, curPtr, highPtr, resetFlag.toBoolean())
})

public actual fun sqlite3_status64(
    option: Sqlite3StatusOption,
    outCurrent: Int64OutputParam,
    outHighwater: Int64OutputParam,
    resetFlag: Int
): Sqlite3Result = convertResult(useParams(outCurrent, outHighwater) { curPtr, highPtr ->
    native.sqlite3_status64(option.id, curPtr, highPtr, resetFlag.toBoolean())
})

public actual fun sqlite3_step(stmt: sqlite3_stmt): Sqlite3Result =
    convertResult(native.sqlite3_step(stmt.holder))

public actual fun sqlite3_stmt_busy(stmt: sqlite3_stmt): Int =
    native.sqlite3_stmt_busy(stmt.holder).toInt()

public actual fun sqlite3_stmt_explain(
    stmt: sqlite3_stmt,
    mode: Sqlite3ExplainMode
): Sqlite3Result = convertResult(native.sqlite3_stmt_explain(stmt.holder, mode.id))

public actual fun sqlite3_stmt_isexplain(stmt: sqlite3_stmt): Sqlite3ExplainMode =
    convertExplainMode(native.sqlite3_stmt_isexplain(stmt.holder))

public actual fun sqlite3_stmt_readonly(stmt: sqlite3_stmt): Int =
    native.sqlite3_stmt_readonly(stmt.holder).toInt()

public actual fun sqlite3_stmt_status(
    stmt: sqlite3_stmt,
    counter: Sqlite3StatementStatusCounter,
    resetFlag: Int
): Int = native.sqlite3_stmt_status(stmt.holder, counter.id, resetFlag.toBoolean())

public actual fun sqlite3_strglob(
    pattern: String,
    string: String
): Int = native.sqlite3_strglob(pattern, string)

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
): Int = native.sqlite3_strlike(pattern, string, escape)

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

public actual fun sqlite3_trace_v2(
    db: sqlite3,
    mask: Sqlite3TraceCode?,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3TraceCallback?
): Sqlite3Result = convertResult(db.withMemoryManager {
    native.sqlite3_trace_v2(
        db.pointer,
        mask?.value ?: 0,
        functionPointer(callback, ::TraceHandler),
        keyedStableRefPointer(KEY_TRACE, callback, userData)
    )
})

public actual fun sqlite3_txn_state(
    db: sqlite3,
    schema: String?
): Sqlite3TransactionState? = convertTransactionState(memScoped {
    native.sqlite3_txn_state(db.pointer, schema.allocateUtf8())
})

public actual fun sqlite3_update_hook(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3UpdateHookCallback?
): sqlite3_mutable_pointer? = db.withMemoryManager {
    sqlite3_mutable_pointer.fromStableRef(
        native.sqlite3_update_hook(
            db.pointer,
            functionPointer(callback, ::UpdateHookHandler),
            keyedStableRefPointer(KEY_UPDATE_HOOK, callback, userData)
        )
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

public actual fun sqlite3_user_data(context: sqlite3_context): sqlite3_mutable_pointer? =
    context.db.memory
        .stableRefData<CreateFunction>(
            native.sqlite3_user_data(context.pointer).orNull ?: return null
        )
        .second

public actual fun sqlite3_value_bytes(value: sqlite3_value): Int =
    native.sqlite3_value_bytes(value.holder)

public actual fun sqlite3_value_double(value: sqlite3_value): Double =
    native.sqlite3_value_double(value.holder)

public actual fun sqlite3_value_dup(value: sqlite3_value): sqlite3_value? =
    native.sqlite3_value_dup(value.holder).wrapOrNull(::sqlite3_value)

public actual fun sqlite3_value_encoding(value: sqlite3_value): Sqlite3TextEncoding.Set2? =
    convertTextEncoding(native.sqlite3_value_encoding(value.holder))

public actual fun sqlite3_value_free(value: sqlite3_value): Unit =
    native.sqlite3_value_free(value.holder)

public actual fun sqlite3_value_frombind(value: sqlite3_value): Int =
    native.sqlite3_value_frombind(value.holder).toInt()

public actual fun sqlite3_value_int(value: sqlite3_value): Int =
    native.sqlite3_value_int(value.holder)

public actual fun sqlite3_value_int64(value: sqlite3_value): Long =
    native.sqlite3_value_int64(value.holder)

public actual fun sqlite3_value_nochange(value: sqlite3_value): Int =
    native.sqlite3_value_nochange(value.holder)

public actual fun sqlite3_value_numeric_type(value: sqlite3_value): Sqlite3DataType =
    convertDataType(native.sqlite3_value_numeric_type(value.holder))

public actual fun sqlite3_value_pointer(
    value: sqlite3_value,
    type: String?
): sqlite3_mutable_pointer? {
    globalMemory.stableRefData<NamedPointer>(
        native.sqlite3_value_pointer(value.pointer, type.allocateUtf8()) ?: return null
    ).second
}

public actual fun sqlite3_value_subtype(value: sqlite3_value): UInt =
    native.sqlite3_value_subtype(value.holder).toUInt()

public actual fun sqlite3_value_text(value: sqlite3_value): String? =
    native.sqlite3_value_text(value.holder).toString(Charsets.UTF_8)

public actual fun sqlite3_value_type(value: sqlite3_value): Sqlite3DataType =
    convertDataType(native.sqlite3_value_type(value.holder))

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
): Sqlite3Result = commonSqlite3VtabConfig(option) { id, values ->
    invokeVariadic(values) { layouts, arguments ->
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

public actual fun sqlite3_wal_hook(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3WalHookCallback?
): sqlite3_mutable_pointer? = db.withMemoryManager {
    sqlite3_mutable_pointer.fromStableRef(
        native.sqlite3_wal_hook(
            db.pointer,
            functionPointer(callback, ::WalHookHandler),
            keyedStableRefPointer(KEY_WAL_HOOK, callback, userData)
        )
    )
}*/