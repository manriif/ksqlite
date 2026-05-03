@file:JvmName("Ksqlite")
@file:Suppress("FunctionName")

package ksqlite.capi

import ksqlite.capi.VariadicValue.OfPointer
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
import ksqlite.capi.handlers.PreupdateHookHandler
import ksqlite.capi.handlers.ProgressHandlerHandler
import ksqlite.capi.handlers.RollbackHookHandler
import ksqlite.capi.handlers.SetAuthorizerHandler
import ksqlite.capi.handlers.SharedAutoExtensionHandler
import ksqlite.capi.handlers.TraceHandler
import ksqlite.capi.handlers.UpdateHookHandler
import ksqlite.capi.handlers.WalHookHandler
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.deallocateNullable
import ksqlite.capi.memory.globalDisposer
import ksqlite.capi.memory.globalMemory
import ksqlite.capi.memory.keyedStableRefPointer
import ksqlite.capi.memory.memScoped
import ksqlite.capi.memory.memory
import ksqlite.capi.memory.stableRefData
import ksqlite.capi.memory.useMemoryManager
import ksqlite.capi.memory.userDataDisposer
import ksqlite.capi.memory.withMemoryManager
import ksqlite.capi.types.Sqlite3AutoExtensionCallback
import ksqlite.capi.types.Sqlite3AutoVacuumPagesCallback
import ksqlite.capi.types.Sqlite3BlobOpenFlag
import ksqlite.capi.types.Sqlite3BlobOutParam
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
import ksqlite.capi.types.Sqlite3DatabaseConnectionOutParam
import ksqlite.capi.types.Sqlite3DbConfigOption
import ksqlite.capi.types.Sqlite3DbStatusOption
import ksqlite.capi.types.Sqlite3DeserializeFlag
import ksqlite.capi.types.Sqlite3DestructorCallback
import ksqlite.capi.types.Sqlite3ExecCallback
import ksqlite.capi.types.Sqlite3ExplainMode
import ksqlite.capi.types.Sqlite3FileControlOpcode
import ksqlite.capi.types.Sqlite3FileOpenFlag
import ksqlite.capi.types.Sqlite3IntOutParam
import ksqlite.capi.types.Sqlite3Limit
import ksqlite.capi.types.Sqlite3LongOutParam
import ksqlite.capi.types.Sqlite3PrepareFlag
import ksqlite.capi.types.Sqlite3PreupdateHookCallback
import ksqlite.capi.types.Sqlite3ProgressHandlerCallback
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.Sqlite3RollbackHookCallback
import ksqlite.capi.types.Sqlite3SerializeFlag
import ksqlite.capi.types.Sqlite3SetAuthorizerCallback
import ksqlite.capi.types.Sqlite3SnapshotOutParam
import ksqlite.capi.types.Sqlite3StatementOutParam
import ksqlite.capi.types.Sqlite3StatementStatusCounter
import ksqlite.capi.types.Sqlite3StatusOption
import ksqlite.capi.types.Sqlite3TextEncoding
import ksqlite.capi.types.Sqlite3TraceCallback
import ksqlite.capi.types.Sqlite3TraceCode
import ksqlite.capi.types.Sqlite3TransactionState
import ksqlite.capi.types.Sqlite3UpdateHookCallback
import ksqlite.capi.types.Sqlite3Utf8OutParam
import ksqlite.capi.types.Sqlite3ValueOutParam
import ksqlite.capi.types.Sqlite3VirtualTableConfigOption
import ksqlite.capi.types.Sqlite3WalHookCallback
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
import ksqlite.capi.types.useParamMemScoped
import ksqlite.capi.types.useParams
import ksqlite.capi.types.useParamsMemScoped
import ksqlite.capi.utils.allocateUtf8
import ksqlite.capi.utils.allocateUtf8Array
import ksqlite.capi.utils.backing
import ksqlite.capi.utils.getStringUtf8
import ksqlite.capi.utils.getStringUtf8OrNull
import ksqlite.capi.utils.notNull
import ksqlite.capi.utils.orNull
import ksqlite.sqliteLoadLibrary
import java.lang.foreign.Arena
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
    sqliteLoadLibrary()
}

///////////////////////////////////////////////////////////////////////////
// Helpers
///////////////////////////////////////////////////////////////////////////

/**
 * Invokes a function accepting a variadic parameter.
 */
private inline fun <Result> invokeVariadic(
    values: Array<out VariadicValue<MemorySegment>?>,
    invoke: (layouts: Array<out MemoryLayout>, arguments: Array<out Any>) -> Result
): Result {
    val layouts = Array(values.size) { index ->
        when (values[index]) {
            is OfInt, is OfUInt -> ValueLayout.JAVA_INT
            is OfLong -> ValueLayout.JAVA_LONG
            is OfPointer, is OfString, null -> ValueLayout.ADDRESS
        }
    }

    var arena: Arena? = null

    val arguments = Array(values.size) { index ->
        when (val value = values[index]) {
            null -> MemorySegment.NULL
            !is OfString -> value.value

            else -> {
                if (arena == null) {
                    arena = Arena.ofConfined()
                }

                arena.allocateFrom(value.value, Charsets.UTF_8)
            }
        }
    }

    return invoke(layouts, arguments).also {
        arena?.close()
    }
}

///////////////////////////////////////////////////////////////////////////
// Functions
///////////////////////////////////////////////////////////////////////////

public actual fun sqlite3_aggregate_context(
    context: sqlite3_context,
    nBytes: Int
): sqlite3_mutable_pointer? = sqlite3_mutable_pointer.from(
    pointer = native.sqlite3_aggregate_context(
        /* x0 = */ context.pointer,
        /* nBytes = */ nBytes
    ),
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
        /* db = */ db.pointer,
        /* x1 = */ functionPointer(callback, ::AutoVacuumPagesHandler),
        /* x2 = */ keyedStableRefPointer(KEY_AUTOVACUUM_PAGES, callback, userData, destructor),
        /* x3 = */ stableRefDisposer(callback, destructor)
    )
})

public actual fun sqlite3_backup_finish(backup: sqlite3_backup): Sqlite3Result = convertResult(
    native.sqlite3_backup_finish(backup.pointer)
)

public actual fun sqlite3_backup_init(
    destDb: sqlite3,
    destDbName: String,
    srcDb: sqlite3,
    srcDbName: String
): sqlite3_backup? = memScoped {
    native.sqlite3_backup_init(
        /* pDest = */ destDb.pointer,
        /* zDestName = */ destDbName.allocateUtf8(),
        /* pSource = */ srcDb.pointer,
        /* zSourceName = */ srcDbName.allocateUtf8()
    ).orNull?.let(::sqlite3_backup)
}

public actual fun sqlite3_backup_pagecount(backup: sqlite3_backup): Int =
    native.sqlite3_backup_pagecount(backup.pointer)

public actual fun sqlite3_backup_remaining(backup: sqlite3_backup): Int =
    native.sqlite3_backup_remaining(backup.pointer)

public actual fun sqlite3_backup_step(
    backup: sqlite3_backup,
    nPage: Int
): Sqlite3Result = convertResult(
    native.sqlite3_backup_step(
        /* p = */ backup.pointer,
        /* nPage = */ nPage
    )
)

public actual fun sqlite3_bind_blob(
    stmt: sqlite3_stmt,
    index: Int,
    data: ByteArray?,
    size: Int,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(
    native.sqlite3_bind_blob(
        /* x0 = */ stmt.pointer,
        /* x1 = */ index,
        /* x2 = */ stmt.memory.byteArrayPointer(data, destructor),
        /* n = */ size,
        /* x4 = */ globalDisposer(data)
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
        /* x0 = */ stmt.pointer,
        /* x1 = */ index,
        /* x2 = */ data?.block?.pointer.notNull,
        /* x3 = */ size,
        /* x4 = */ userDataDisposer(data, destructor)
    )
)

public actual fun sqlite3_bind_double(
    stmt: sqlite3_stmt,
    index: Int,
    value: Double
): Sqlite3Result = convertResult(
    native.sqlite3_bind_double(
        /* x0 = */ stmt.pointer,
        /* x1 = */ index,
        /* x2 = */ value
    )
)

public actual fun sqlite3_bind_int(
    stmt: sqlite3_stmt,
    index: Int,
    value: Int
): Sqlite3Result = convertResult(
    native.sqlite3_bind_int(
        /* x0 = */ stmt.pointer,
        /* x1 = */ index,
        /* x2 = */ value
    )
)

public actual fun sqlite3_bind_int64(
    stmt: sqlite3_stmt,
    index: Int,
    value: Long
): Sqlite3Result = convertResult(
    native.sqlite3_bind_int64(
        /* x0 = */ stmt.pointer,
        /* x1 = */ index,
        /* x2 = */ value
    )
)

public actual fun sqlite3_bind_null(
    stmt: sqlite3_stmt,
    index: Int
): Sqlite3Result = convertResult(
    native.sqlite3_bind_null(
        /* x0 = */ stmt.pointer,
        /* x1 = */ index,
    )
)

public actual fun sqlite3_bind_parameter_count(stmt: sqlite3_stmt): Int =
    native.sqlite3_bind_parameter_count(stmt.pointer)

public actual fun sqlite3_bind_parameter_index(
    stmt: sqlite3_stmt,
    name: String
): Int = memScoped {
    native.sqlite3_bind_parameter_index(
        /* x0 = */ stmt.pointer,
        /* zName = */ name.allocateUtf8()
    )
}

public actual fun sqlite3_bind_parameter_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native.sqlite3_bind_parameter_name(
    /* x0 = */ stmt.pointer,
    /* x1 = */ index
).getStringUtf8OrNull()

public actual fun sqlite3_bind_pointer(
    stmt: sqlite3_stmt,
    index: Int,
    data: sqlite3_mutable_pointer?,
    type: String?,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(with(globalMemory) {
    // Use globalMemory because of lack of information within sqlite3_value_pointer()
    allocateNamedPointer(type, destructor) {
        native.sqlite3_bind_pointer(
            /* x0 = */ stmt.pointer,
            /* x1 = */ index,
            /* x2 = */ stableRefPointer(this, data, disposer),
            /* x3 = */ typePointer,
            /* x4 = */ stableRefDisposer(this, disposer)
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

    native.sqlite3_bind_text(
        /* x0 = */ stmt.pointer,
        /* x1 = */ index,
        /* x2 = */ cText.notNull,
        /* x3 = */ size ?: cText?.byteSize()?.toInt() ?: 0,
        /* x4 = */ SqliteTransient
    )
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
        /* x0 = */ stmt.pointer,
        /* x1 = */ index,
        /* x2 = */ data?.block?.pointer.notNull,
        /* x3 = */ size,
        /* x4 = */ userDataDisposer(data, destructor),
        /* encoding = */ encoding.utf8OrThrow().value.toByte()
    )
)

public actual fun sqlite3_bind_value(
    stmt: sqlite3_stmt,
    index: Int,
    value: sqlite3_value
): Sqlite3Result = convertResult(
    native.sqlite3_bind_value(
        /* x0 = */ stmt.pointer,
        /* x1 = */ index,
        /* x2 = */ value.pointer
    )
)

public actual fun sqlite3_bind_zeroblob(
    stmt: sqlite3_stmt,
    index: Int,
    size: Int
): Sqlite3Result = convertResult(
    native.sqlite3_bind_zeroblob(
        /* x0 = */ stmt.pointer,
        /* x1 = */ index,
        /* n = */ size
    )
)

public actual fun sqlite3_bind_zeroblob64(
    stmt: sqlite3_stmt,
    index: Int,
    size: ULong
): Sqlite3Result = convertResult(
    native.sqlite3_bind_zeroblob64(
        /* x0 = */ stmt.pointer,
        /* x1 = */ index,
        /* x2 = */ size.toLong()
    )
)

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
    outBlob: Sqlite3BlobOutParam
): Sqlite3Result = convertResult(memScoped {
    useParam(outBlob) { blobPtr ->
        native.sqlite3_blob_open(
            /* x0 = */ db.pointer,
            /* zDb = */ databaseName.allocateUtf8(),
            /* zTable = */ tableName.allocateUtf8(),
            /* zColumn = */ columnName.allocateUtf8(),
            /* iRow = */ rowIndex,
            /* flags = */ flags.value,
            /* ppBlob = */ blobPtr
        )
    }
})

public actual fun sqlite3_blob_read(
    blob: sqlite3_blob,
    buffer: ByteArray,
    size: Int,
    offset: Int
): Sqlite3Result = convertResult(
    native.sqlite3_blob_read(
        /* x0 = */ blob.pointer,
        /* Z = */ buffer.backing(),
        /* N = */ size,
        /* iOffset = */ offset
    )
)

public actual fun sqlite3_blob_reopen(
    blob: sqlite3_blob,
    rowIndex: Long
): Sqlite3Result = convertResult(
    native.sqlite3_blob_reopen(
        /* x0 = */ blob.pointer,
        /* x1 = */ rowIndex
    )
)

public actual fun sqlite3_blob_write(
    blob: sqlite3_blob,
    buffer: ByteArray,
    size: Int,
    offset: Int
): Sqlite3Result = convertResult(
    native.sqlite3_blob_write(
        /* x0 = */ blob.pointer,
        /* z = */ buffer.backing(),
        /* n = */ size,
        /* iOffset = */ offset
    )
)

public actual fun sqlite3_busy_handler(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3BusyHandlerCallback?
): Sqlite3Result = convertResult(db.withMemoryManager {
    native.sqlite3_busy_handler(
        /* x0 = */ db.pointer,
        /* x1 = */ functionPointer(callback, ::BusyHandlerHandler),
        /* x2 = */ keyedStableRefPointer(KEY_BUSY_HANDLER, callback, userData)
    )
})

public actual fun sqlite3_busy_timeout(
    db: sqlite3,
    millis: Int
): Sqlite3Result = convertResult(
    native.sqlite3_busy_timeout(
        /* x0 = */ db.pointer,
        /* ms = */ millis
    )
)

public actual fun sqlite3_cancel_auto_extension(callback: Sqlite3AutoExtensionCallback): Int =
    autoExtensionUnregister(callback) {
        native.ksqlite_cancel_auto_extension(SharedAutoExtensionHandler)
    }

public actual fun sqlite3_changes(db: sqlite3): Int =
    native.sqlite3_changes(db.pointer)

public actual fun sqlite3_changes64(db: sqlite3): Long =
    native.sqlite3_changes64(db.pointer)

public actual fun sqlite3_clear_bindings(stmt: sqlite3_stmt): Sqlite3Result =
    commonSqlite3ClearBindings(stmt, native.sqlite3_clear_bindings(stmt.pointer))

public actual fun sqlite3_close(db: sqlite3?): Sqlite3Result =
    db.deallocateNullable { native.sqlite3_close(it?.pointer.notNull) }

public actual fun sqlite3_close_v2(db: sqlite3?): Sqlite3Result =
    db.deallocateNullable { native.sqlite3_close_v2(it?.pointer.notNull) }

public actual fun sqlite3_collation_needed(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3CollationNeededCallback?,
): Sqlite3Result = convertResult(db.withMemoryManager {
    native.sqlite3_collation_needed(
        /* x0 = */ db.pointer,
        /* x1 = */ keyedStableRefPointer(KEY_COLLATION_NEEDED, callback, userData),
        /* x2 = */ functionPointer(callback, ::CollationNeededHandler)
    )
})

public actual fun sqlite3_column_blob(
    stmt: sqlite3_stmt,
    index: Int
): sqlite3_pointer? = sqlite3_pointer.from(
    pointer = native.sqlite3_column_blob(
        /* x0 = */ stmt.pointer,
        /* iCol = */ index
    ),
    size = native.sqlite3_column_bytes(
        /* x0 = */ stmt.pointer,
        /* iCol = */ index
    ).toLong()
)

public actual fun sqlite3_column_bytes(
    stmt: sqlite3_stmt,
    index: Int
): Int = native.sqlite3_column_bytes(
    /* x0 = */ stmt.pointer,
    /* iCol = */ index
)

public actual fun sqlite3_column_count(stmt: sqlite3_stmt): Int =
    native.sqlite3_column_count(stmt.pointer)

public actual fun sqlite3_column_database_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native.sqlite3_column_database_name(
    /* x0 = */ stmt.pointer,
    /* x1 = */ index
).getStringUtf8OrNull()

public actual fun sqlite3_column_decltype(
    stmt: sqlite3_stmt,
    index: Int
): String? = native.sqlite3_column_decltype(
    /* x0 = */ stmt.pointer,
    /* x1 = */ index
).getStringUtf8OrNull()

public actual fun sqlite3_column_double(
    stmt: sqlite3_stmt,
    index: Int
): Double = native.sqlite3_column_double(
    /* x0 = */ stmt.pointer,
    /* iCol = */ index
)

public actual fun sqlite3_column_int(
    stmt: sqlite3_stmt,
    index: Int
): Int = native.sqlite3_column_int(
    /* x0 = */ stmt.pointer,
    /* iCol = */ index
)

public actual fun sqlite3_column_int64(
    stmt: sqlite3_stmt,
    index: Int
): Long = native.sqlite3_column_int64(
    /* x0 = */ stmt.pointer,
    /* iCol = */ index
)

public actual fun sqlite3_column_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native.sqlite3_column_name(
    /* x0 = */ stmt.pointer,
    /* N = */ index
).getStringUtf8OrNull()

public actual fun sqlite3_column_origin_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native.sqlite3_column_origin_name(
    /* x0 = */ stmt.pointer,
    /* x1 = */ index
).getStringUtf8OrNull()

public actual fun sqlite3_column_table_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native.sqlite3_column_table_name(
    /* x0 = */ stmt.pointer,
    /* x1 = */ index
).getStringUtf8OrNull()

public actual fun sqlite3_column_text(
    stmt: sqlite3_stmt,
    index: Int
): String? = native.sqlite3_column_text(
    /* x0 = */ stmt.pointer,
    /* iCol = */ index
).getStringUtf8OrNull()

public actual fun sqlite3_column_type(
    stmt: sqlite3_stmt,
    index: Int
): Sqlite3DataType = convertDataType(
    native.sqlite3_column_type(
        /* x0 = */ stmt.pointer,
        /* iCol = */ index
    )
)

public actual fun sqlite3_column_value(
    stmt: sqlite3_stmt,
    index: Int
): sqlite3_value? = native.sqlite3_column_value(
    /* x0 = */ stmt.pointer,
    /* iCol = */ index
).orNull?.let(::sqlite3_value)

public actual fun sqlite3_commit_hook(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3CommitHookCallback?
): sqlite3_mutable_pointer? = db.withMemoryManager {
    sqlite3_mutable_pointer.fromStableRef(
        native.sqlite3_commit_hook(
            /* x0 = */ db.pointer,
            /* x1 = */ functionPointer(callback, ::CommitHookHandler),
            /* x2 = */ keyedStableRefPointer(KEY_COMMIT_HOOK, callback, userData)
        )
    )
}

public actual fun sqlite3_compileoption_get(index: Int): String? =
    native.sqlite3_compileoption_get(index).getStringUtf8OrNull()

public actual fun sqlite3_compileoption_used(optName: String): Int = memScoped {
    native.sqlite3_compileoption_used(optName.allocateUtf8())
}

public actual fun sqlite3_complete(sql: String): Sqlite3CompleteResult =
    convertCompleteResult(memScoped { native.sqlite3_complete(sql.allocateUtf8()) })

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
    native.sqlite3_context_db_handle(context.pointer).orNull?.let(::sqlite3)

public actual fun sqlite3_create_collation(
    db: sqlite3,
    name: String,
    encoding: Sqlite3TextEncoding.Set0,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3CreateCollationCallback?
): Sqlite3Result = convertResult(db.withMemoryManager {
    memScoped {
        native.sqlite3_create_collation(
            /* x0 = */ db.pointer,
            /* zName = */ name.allocateUtf8(),
            /* eTextRep = */ encoding.utf8OrThrow().value,
            /* pArg = */ keyedStableRefPointer(KEY_CREATE_COLLATION, callback, userData),
            /* xCompare = */ functionPointer(callback, ::CreateCollationHandler),
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
            /* x0 = */ db.pointer,
            /* zName = */ name.allocateUtf8(),
            /* eTextRep = */ encoding.utf8OrThrow().value,
            /* pArg = */ keyedStableRefPointer(
                key = KEY_CREATE_COLLATION,
                data = callback,
                userData = userData,
                destructor = destructor
            ),
            /* xCompare = */ functionPointer(callback, ::CreateCollationHandler),
            /* xDestroy = */ stableRefDisposer(callback, destructor)
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
            /* db = */ db.pointer,
            /* zFunctionName = */ name.allocateUtf8(),
            /* nArg = */ nArg,
            /* eTextRep = */ encoding.utf8OrThrow().value,
            /* pApp = */ keyedStableRefPointer(
                key = uniqueFunctionKey(name, nArg, encoding),
                data = CreateFunction(
                    func = func,
                    step = step,
                    final = final
                ),
                userData = userData
            ),
            /* xFunc = */ functionPointer(func, ::CreateFunctionFuncHandler),
            /* xStep = */ functionPointer(step, ::CreateFunctionStepHandler),
            /* xFinal = */ functionPointer(final, ::CreateFunctionFinalHandler)
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
            /* db = */ db.pointer,
            /* zFunctionName = */ name.allocateUtf8(),
            /* nArg = */ nArg,
            /* eTextRep = */ encoding.utf8OrThrow().value,
            /* pApp = */ keyedStableRefPointer(
                key = uniqueFunctionKey(name, nArg, encoding),
                data = CreateFunction(
                    func = func,
                    step = step,
                    final = final
                ),
                userData = userData
            ),
            /* xFunc = */ functionPointer(func, ::CreateFunctionFuncHandler),
            /* xStep = */ functionPointer(step, ::CreateFunctionStepHandler),
            /* xFinal = */ functionPointer(final, ::CreateFunctionFinalHandler),
            /* xDestroy = */ stableRefDisposer(0, destructor)
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
        /* db = */ db.pointer,
        /* zName = */ name.allocateUtf8(),
        /* p = */ module?.pointer.notNull,
        /* pClientData = */ userData?.block?.pointer.notNull
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
        /* db = */ db.pointer,
        /* zName = */ name.allocateUtf8(),
        /* p = */ module?.pointer.notNull,
        /* pClientData = */ userData?.block?.pointer.notNull,
        /* xDestroy = */ userDataDisposer(userData, destructor)
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
            /* db = */ db.pointer,
            /* zFunctionName = */ name.allocateUtf8(),
            /* nArg = */ nArg,
            /* eTextRep = */ encoding.utf8OrThrow().value,
            /* pApp = */ keyedStableRefPointer(
                key = uniqueFunctionKey(name, nArg, encoding),
                data = CreateFunction(
                    step = step,
                    final = final,
                    value = value,
                    inverse = inverse
                ),
                userData = userData
            ),
            /* xStep = */ functionPointer(step, ::CreateFunctionStepHandler),
            /* xFinal = */ functionPointer(final, ::CreateFunctionFinalHandler),
            /* xValue = */ functionPointer(value, ::CreateFunctionValueHandler),
            /* xInverse = */ functionPointer(inverse, ::CreateFunctionInverseHandler),
            /* xDestroy = */ stableRefDisposer(0, destructor)
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
): sqlite3_filename? = memScoped {
    native.sqlite3_db_filename(
        /* db = */ db.pointer,
        /* zDbName = */ name.allocateUtf8()
    )
}.getStringUtf8OrNull()

public actual fun sqlite3_db_handle(stmt: sqlite3_stmt): sqlite3? =
    native.sqlite3_db_handle(stmt.pointer).orNull?.let(::sqlite3)

public actual fun sqlite3_db_name(
    db: sqlite3,
    index: Int
): String? = native.sqlite3_db_name(
    /* db = */ db.pointer,
    /* N = */ index
).getStringUtf8OrNull()

public actual fun sqlite3_db_readonly(
    db: sqlite3,
    name: String
): Int = memScoped {
    native.sqlite3_db_readonly(
        /* db = */ db.pointer,
        /* zDbName = */ name.allocateUtf8()
    )
}

public actual fun sqlite3_db_release_memory(db: sqlite3): Sqlite3Result =
    convertResult(native.sqlite3_db_release_memory(db.pointer))

public actual fun sqlite3_db_status(
    db: sqlite3,
    option: Sqlite3DbStatusOption,
    outCurrent: Sqlite3IntOutParam?,
    outHighwater: Sqlite3IntOutParam?,
    resetFlag: Int
): Sqlite3Result = convertResult(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
    native.sqlite3_db_status(
        /* x0 = */ db.pointer,
        /* op = */ option.id,
        /* pCur = */ curPtr,
        /* pHiwtr = */ highPtr,
        /* resetFlg = */ resetFlag
    )
})

public actual fun sqlite3_db_status64(
    db: sqlite3,
    option: Sqlite3DbStatusOption,
    outCurrent: Sqlite3LongOutParam?,
    outHighwater: Sqlite3LongOutParam?,
    resetFlag: Int
): Sqlite3Result = convertResult(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
    native.sqlite3_db_status64(
        /* x0 = */ db.pointer,
        /* x1 = */ option.id,
        /* x2 = */ curPtr,
        /* x3 = */ highPtr,
        /* x4 = */ resetFlag
    )
})

public actual fun sqlite3_declare_vtab(
    db: sqlite3,
    sql: String
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_declare_vtab(
        /* x0 = */ db.pointer,
        /* zSQL = */ sql.allocateUtf8()
    )
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
        /* db = */ db.pointer,
        /* zSchema = */ schema.allocateUtf8(),
        /* pData = */ data?.block?.pointer.notNull,
        /* szDb = */ dbSize,
        /* szBuf = */ dataSize,
        /* mFlags = */ flags?.value ?: 0
    )
})

public actual fun sqlite3_drop_modules(
    db: sqlite3,
    keep: Array<String>?
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_drop_modules(
        /* db = */ db.pointer,
        /* azKeep = */ keep.allocateUtf8Array()
    )
})

public actual fun sqlite3_errcode(db: sqlite3): Int =
    native.sqlite3_errcode(db.pointer)

public actual fun sqlite3_errmsg(db: sqlite3): String? =
    native.sqlite3_errmsg(db.pointer).getStringUtf8OrNull()

public actual fun sqlite3_errstr(resultCode: Int): String? =
    native.sqlite3_errstr(resultCode).getStringUtf8OrNull()

public actual fun sqlite3_error_offset(db: sqlite3): Int =
    native.sqlite3_error_offset(db.pointer)

public actual fun sqlite3_exec(
    db: sqlite3,
    sql: String,
    outErrorMessage: Sqlite3Utf8OutParam?,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3ExecCallback?
): Sqlite3Result = convertResult(useMemoryManager {
    memScoped {
        useParam(outErrorMessage) { errorMessagePtr ->
            native.sqlite3_exec(
                /* x0 = */ db.pointer,
                /* sql = */ sql.allocateUtf8(),
                /* callback = */ functionPointer(callback, ::ExecHandler),
                /* x3 = */ stableRefPointer(callback, userData),
                /* errmsg = */ errorMessagePtr
            )
        }
    }
})

public actual fun sqlite3_expanded_sql(stmt: sqlite3_stmt): String? {
    val pointer = native.sqlite3_expanded_sql(stmt.pointer).orNull ?: return null
    val expandedSql = pointer.getStringUtf8()
    native.sqlite3_free(pointer)
    return expandedSql
}

public actual fun sqlite3_extended_errcode(db: sqlite3): Int =
    native.sqlite3_extended_errcode(db.pointer)

public actual fun sqlite3_extended_result_codes(
    db: sqlite3,
    enabled: Int
): Sqlite3Result = convertResult(
    native.sqlite3_extended_result_codes(
        /* x0 = */ db.pointer,
        /* onoff = */ enabled
    )
)

public actual fun sqlite3_file_control(
    db: sqlite3,
    name: String?,
    opcode: Sqlite3FileControlOpcode,
    userData: sqlite3_mutable_pointer?
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_file_control(
        /* x0 = */ db.pointer,
        /* zDbName = */ name.allocateUtf8(),
        /* op = */ opcode.code,
        /* x3 = */ userData?.block?.pointer.notNull
    )
})

public actual fun sqlite3_finalize(stmt: sqlite3_stmt?): Sqlite3Result =
    stmt.deallocateNullable { native.sqlite3_finalize(stmt?.pointer.notNull) }

public actual fun sqlite3_free(data: sqlite3_mutable_pointer?): Unit =
    native.sqlite3_free(data?.block?.pointer.notNull)

public actual fun sqlite3_get_autocommit(db: sqlite3): Int =
    native.sqlite3_get_autocommit(db.pointer)

public actual fun sqlite3_get_auxdata(
    context: sqlite3_context,
    index: Int
): sqlite3_mutable_pointer? = native.sqlite3_get_auxdata(
    /* x0 = */ context.pointer,
    /* N = */ index
).orNull?.let { refPointer ->
    context.db.memory.stableRefData<sqlite3_mutable_pointer>(refPointer).first
}

public actual fun sqlite3_hard_heap_limit64(limit: Long): Long =
    native.sqlite3_hard_heap_limit64(limit)

public actual fun sqlite3_initialize(): Sqlite3Result =
    convertResult(native.sqlite3_initialize())

public actual fun sqlite3_interrupt(db: sqlite3): Unit =
    native.sqlite3_interrupt(db.pointer)

public actual fun sqlite3_is_interrupted(db: sqlite3): Int =
    native.sqlite3_is_interrupted(db.pointer)

public actual fun sqlite3_keyword_count(): Int =
    native.sqlite3_keyword_count()

public actual fun sqlite3_keyword_name(
    index: Int,
    outName: Sqlite3Utf8OutParam,
): Sqlite3Result = convertResult(memScoped {
    useParam(outName) { namePtr ->
        val size = Sqlite3IntOutParam(0)

        useParam(size) { sizePtr ->
            native.sqlite3_keyword_name(
                /* x0 = */ index,
                /* x1 = */ namePtr,
                /* x2 = */ sizePtr
            )
        }.also {
            outName.size = size.value
        }
    }
})

public actual fun sqlite3_keyword_check(
    word: String,
    size: Int?
): Int = memScoped {
    val cWord = word.allocateUtf8()

    native.sqlite3_keyword_check(
        /* x0 = */ cWord,
        /* x1 = */ size ?: cWord.byteSize().toInt()
    )
}

public actual fun sqlite3_last_insert_rowid(db: sqlite3): Long =
    native.sqlite3_last_insert_rowid(db.pointer)

public actual fun sqlite3_libversion(): String =
    native.sqlite3_libversion().getStringUtf8()

public actual fun sqlite3_libversion_number(db: sqlite3): Int =
    native.sqlite3_libversion_number()

public actual fun sqlite3_limit(
    db: sqlite3,
    id: Sqlite3Limit,
    newVal: Int
): Int = native.sqlite3_limit(
    /* x0 = */ db.pointer,
    /* id = */ id.id,
    /* newVal = */ newVal
)

public actual fun sqlite3_log(
    errCode: Int,
    message: String
): Unit = memScoped {
    native.sqlite3_log.makeInvoker().apply(
        /* iErrCode = */ errCode,
        /* zFormat = */ message.allocateUtf8()
    )
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
): sqlite3_stmt? = native.sqlite3_next_stmt(
    /* pDb = */ db.pointer,
    /* pStmt = */ stmt?.pointer.notNull
).orNull?.let(::sqlite3_stmt)

public actual fun sqlite3_open(
    fileName: String,
    outDb: Sqlite3DatabaseConnectionOutParam
): Sqlite3Result = convertResult(memScoped {
    useParam(outDb) { dbPtr ->
        native.sqlite3_open(
            /* filename = */ fileName.allocateUtf8(),
            /* ppDb = */ dbPtr
        )
    }
})

public actual fun sqlite3_open_v2(
    fileName: String,
    outDb: Sqlite3DatabaseConnectionOutParam,
    flags: Sqlite3FileOpenFlag.Valid,
    vfs: String?
): Sqlite3Result = convertResult(memScoped {
    useParam(outDb) { dbPtr ->
        native.sqlite3_open_v2(
            /* filename = */ fileName.allocateUtf8(),
            /* ppDb = */ dbPtr,
            /* flags = */ flags.value,
            /* zVfs = */ vfs.allocateUtf8()
        )
    }
})

public actual fun sqlite3_overload_function(
    db: sqlite3,
    name: String,
    nArg: Int
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_overload_function(
        /* x0 = */ db.pointer,
        /* zFuncName = */ name.allocateUtf8(),
        /* nArg = */ nArg
    )
})

public actual fun sqlite3_prepare_v2(
    db: sqlite3,
    sql: String,
    size: Int?,
    outStmt: Sqlite3StatementOutParam,
    outTail: Sqlite3Utf8OutParam?
): Sqlite3Result = convertResult(memScoped {
    useParams(outStmt, outTail) { stmtPtr, tailPtr ->
        val cSql = sql.allocateUtf8()

        native.sqlite3_prepare_v2(
            /* db = */ db.pointer,
            /* zSql = */ cSql,
            /* nByte = */ size ?: cSql.byteSize().toInt(),
            /* ppStmt = */ stmtPtr,
            /* pzTail = */ tailPtr
        )
    }
})

public actual fun sqlite3_prepare_v3(
    db: sqlite3,
    sql: String,
    size: Int?,
    flags: Sqlite3PrepareFlag?,
    outStmt: Sqlite3StatementOutParam,
    outTail: Sqlite3Utf8OutParam?
): Sqlite3Result = convertResult(memScoped {
    useParams(outStmt, outTail) { stmtPtr, tailPtr ->
        val cSql = sql.allocateUtf8()

        native.sqlite3_prepare_v3(
            /* db = */ db.pointer,
            /* zSql = */ cSql,
            /* nByte = */ size ?: cSql.byteSize().toInt(),
            /* prepFlags = */ flags?.value ?: 0,
            /* ppStmt = */ stmtPtr,
            /* pzTail = */ tailPtr
        )
    }
})

public actual fun sqlite3_preupdate_blobwrite(db: sqlite3): Int =
    native.sqlite3_preupdate_blobwrite(db.pointer)

public actual fun sqlite3_preupdate_count(db: sqlite3): Int =
    native.sqlite3_preupdate_count(db.pointer)

public actual fun sqlite3_preupdate_depth(db: sqlite3): Int =
    native.sqlite3_preupdate_depth(db.pointer)

public actual fun sqlite3_preupdate_hook(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3PreupdateHookCallback?
): sqlite3_mutable_pointer? = db.withMemoryManager {
    sqlite3_mutable_pointer.fromStableRef(
        native.sqlite3_preupdate_hook(
            /* db = */ db.pointer,
            /* xPreUpdate = */ functionPointer(callback, ::PreupdateHookHandler),
            /* x2 = */ keyedStableRefPointer(KEY_PREUPDATE_HOOK, callback, userData)
        )
    )
}

public actual fun sqlite3_preupdate_new(
    db: sqlite3,
    index: Int,
    outValue: Sqlite3ValueOutParam
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    native.sqlite3_preupdate_new(
        /* x0 = */ db.pointer,
        /* x1 = */ index,
        /* x2 = */ valuePtr
    )
})

public actual fun sqlite3_preupdate_old(
    db: sqlite3,
    index: Int,
    outValue: Sqlite3ValueOutParam
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    native.sqlite3_preupdate_old(
        /* x0 = */ db.pointer,
        /* x1 = */ index,
        /* x2 = */ valuePtr
    )
})

public actual fun sqlite3_progress_handler(
    db: sqlite3,
    nOps: Int,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3ProgressHandlerCallback?
): Unit = db.withMemoryManager {
    native.sqlite3_progress_handler(
        /* x0 = */ db.pointer,
        /* x1 = */ nOps,
        /* x2 = */ functionPointer(callback, ::ProgressHandlerHandler),
        /* x3 = */ keyedStableRefPointer(KEY_PROGRESS_HANDLER, callback, userData)
    )
}

public actual fun sqlite3_randomness(
    size: Int,
    data: sqlite3_mutable_pointer?
): Unit = native.sqlite3_randomness(
    /* N = */ size,
    /* P = */ data?.block?.pointer.notNull
)

public actual fun sqlite3_realloc(
    data: sqlite3_mutable_pointer?,
    size: Int
): sqlite3_mutable_pointer? = sqlite3_mutable_pointer.from(
    pointer = native.sqlite3_realloc(
        /* x0 = */ data?.block?.pointer.notNull,
        /* x1 = */ size
    ),
    size = size.toLong()
)

public actual fun sqlite3_realloc64(
    data: sqlite3_mutable_pointer?,
    size: Long
): sqlite3_mutable_pointer? = sqlite3_mutable_pointer.from(
    pointer = native.sqlite3_realloc64(
        /* x0 = */ data?.block?.pointer.notNull,
        /* x1 = */ size
    ),
    size = size
)

public actual fun sqlite3_release_memory(size: Int): Int =
    native.sqlite3_release_memory(size)

public actual fun sqlite3_reset(stmt: sqlite3_stmt): Sqlite3Result =
    convertResult(native.sqlite3_reset(stmt.pointer))

public actual fun sqlite3_reset_auto_extension(): Unit =
    autoExtensionReset { native.sqlite3_reset_auto_extension() }

public actual fun sqlite3_result_blob(
    context: sqlite3_context,
    data: ByteArray?,
    size: Int,
    destructor: Sqlite3DestructorCallback?
): Unit = native.sqlite3_result_blob(
    /* x0 = */ context.pointer,
    /* x1 = */ context.db.memory.byteArrayPointer(data, destructor),
    /* x2 = */ size,
    /* x3 = */ globalDisposer(data)
)

public actual fun sqlite3_result_blob64(
    context: sqlite3_context,
    data: sqlite3_mutable_pointer?,
    size: Long,
    destructor: Sqlite3DestructorCallback?
): Unit = native.sqlite3_result_blob64(
    /* x0 = */ context.pointer,
    /* x1 = */ data?.block?.pointer.notNull,
    /* x2 = */ size,
    /* x3 = */ userDataDisposer(data, destructor)
)

public actual fun sqlite3_result_double(
    context: sqlite3_context,
    value: Double
): Unit = native.sqlite3_result_double(
    /* x0 = */ context.pointer,
    /* x1 = */ value
)

public actual fun sqlite3_result_error(
    context: sqlite3_context,
    message: String?,
    size: Int?
): Unit = memScoped {
    val cMessage = message?.allocateUtf8()

    native.sqlite3_result_error(
        /* x0 = */ context.pointer,
        /* x1 = */ cMessage.notNull,
        /* x2 = */ size ?: cMessage?.byteSize()?.toInt() ?: 0
    )
}

public actual fun sqlite3_result_error_code(
    context: sqlite3_context,
    code: Int
): Unit = native.sqlite3_result_error_code(
    /* x0 = */ context.pointer,
    /* x1 = */ code
)

public actual fun sqlite3_result_error_nomem(context: sqlite3_context): Unit =
    native.sqlite3_result_error_nomem(context.pointer)

public actual fun sqlite3_result_error_toobig(context: sqlite3_context): Unit =
    native.sqlite3_result_error_toobig(context.pointer)

public actual fun sqlite3_result_int(
    context: sqlite3_context,
    value: Int
): Unit = native.sqlite3_result_int(
    /* x0 = */ context.pointer,
    /* x1 = */ value
)

public actual fun sqlite3_result_int64(
    context: sqlite3_context,
    value: Long
): Unit = native.sqlite3_result_int64(
    /* x0 = */ context.pointer,
    /* x1 = */ value
)

public actual fun sqlite3_result_null(context: sqlite3_context): Unit =
    native.sqlite3_result_null(context.pointer)

public actual fun sqlite3_result_pointer(
    context: sqlite3_context,
    data: sqlite3_mutable_pointer?,
    type: String?,
    destructor: Sqlite3DestructorCallback?
): Unit = with(globalMemory) {
    // Use globalMemory because of lack of information within sqlite3_value_pointer()
    allocateNamedPointer(type, destructor) {
        native.sqlite3_result_pointer(
            /* x0 = */ context.pointer,
            /* x1 = */ stableRefPointer(this, data, this.disposer),
            /* x2 = */ typePointer,
            /* x3 = */ stableRefDisposer(0, this.disposer)
        )
    }
}

public actual fun sqlite3_result_subtype(
    context: sqlite3_context,
    subtype: UInt
): Unit = native.sqlite3_result_subtype(
    /* x0 = */ context.pointer,
    /* x1 = */ subtype.toInt()
)

public actual fun sqlite3_result_text(
    context: sqlite3_context,
    text: String?,
    size: Int?
): Unit = memScoped {
    val cText = text?.allocateUtf8()

    native.sqlite3_result_text(
        /* x0 = */ context.pointer,
        /* x1 = */ cText.notNull,
        /* x2 = */ size ?: cText?.byteSize()?.toInt() ?: 0,
        /* x3 = */ SqliteTransient
    )
}

public actual fun sqlite3_result_text64(
    context: sqlite3_context,
    data: sqlite3_mutable_pointer?,
    size: Long,
    encoding: Sqlite3TextEncoding.Set1,
    destructor: Sqlite3DestructorCallback?
): Unit = native.sqlite3_result_text64(
    /* x0 = */ context.pointer,
    /* z = */ data?.block?.pointer.notNull,
    /* n = */ size,
    /* x3 = */ userDataDisposer(data, destructor),
    /* encoding = */ encoding.utf8OrThrow().value.toByte()
)

public actual fun sqlite3_result_value(
    context: sqlite3_context,
    value: sqlite3_value?,
): Unit = native.sqlite3_result_value(
    /* x0 = */ context.pointer,
    /* x1 = */ value?.pointer.notNull
)

public actual fun sqlite3_result_zeroblob(
    context: sqlite3_context,
    size: Int
): Unit = native.sqlite3_result_zeroblob(
    /* x0 = */ context.pointer,
    /* n = */ size
)

public actual fun sqlite3_result_zeroblob64(
    context: sqlite3_context,
    size: ULong
): Int = native.sqlite3_result_zeroblob64(
    /* x0 = */ context.pointer,
    /* n = */ size.toLong()
)

public actual fun sqlite3_rollback_hook(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3RollbackHookCallback?
): sqlite3_mutable_pointer? = db.withMemoryManager {
    sqlite3_mutable_pointer.fromStableRef(
        native.sqlite3_rollback_hook(
            /* x0 = */ db.pointer,
            /* x1 = */ functionPointer(callback, ::RollbackHookHandler),
            /* x2 = */ keyedStableRefPointer(KEY_ROLLBACK_HOOK, callback, userData)
        )
    )
}

public actual fun sqlite3_serialize(
    db: sqlite3,
    schema: String?,
    flags: Sqlite3SerializeFlag?
): sqlite3_mutable_pointer? {
    val size = Sqlite3LongOutParam(0)

    val pointer = memScoped {
        useParam(size) { sizePtr ->
            native.sqlite3_serialize(
                /* db = */ db.pointer,
                /* zSchema = */ schema.allocateUtf8(),
                /* piSize = */ sizePtr,
                /* mFlags = */ flags?.value ?: 0
            )
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
        /* x0 = */ db.pointer,
        /* xAuth = */ functionPointer(callback, ::SetAuthorizerHandler),
        /* pUserData = */ keyedStableRefPointer(KEY_SET_AUTHORIZER, callback, userData)
    )
})

public actual fun sqlite3_set_auxdata(
    context: sqlite3_context,
    index: Int,
    data: sqlite3_mutable_pointer?,
    destructor: Sqlite3DestructorCallback?
): Unit = context.db.withMemoryManager {
    native.sqlite3_set_auxdata(
        /* x0 = */ context.pointer,
        /* N = */ index,
        /* x2 = */ keyedStableRefPointer(auxDataKey(index), data, data, destructor),
        /* x3 = */ stableRefDisposer(data, destructor)
    )
}

public actual fun sqlite3_set_errmsg(
    db: sqlite3,
    errorCode: Sqlite3Result.Failure,
    message: String?
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_set_errmsg(
        /* db = */ db.pointer,
        /* errcode = */ errorCode.code,
        /* zErrMsg = */ message.allocateUtf8()
    )
})

public actual fun sqlite3_set_last_insert_rowid(
    db: sqlite3,
    rowId: Long
): Unit = native.sqlite3_set_last_insert_rowid(
    /* x0 = */ db.pointer,
    /* x1 = */ rowId
)

public actual fun sqlite3_shutdown(): Sqlite3Result =
    convertResult(native.sqlite3_shutdown())

public actual fun sqlite3_snapshot_cmp(
    snapshot1: sqlite3_snapshot,
    snapshot2: sqlite3_snapshot
): Int = native.sqlite3_snapshot_cmp(
    /* p1 = */ snapshot1.pointer,
    /* p2 = */ snapshot2.pointer
)

public actual fun sqlite3_snapshot_free(snapshot: sqlite3_snapshot): Unit =
    native.sqlite3_snapshot_free(snapshot.pointer)

public actual fun sqlite3_snapshot_get(
    db: sqlite3,
    name: String?,
    outSnapshot: Sqlite3SnapshotOutParam
): Sqlite3Result = convertResult(memScoped {
    useParam(outSnapshot) { snapshotPtr ->
        native.sqlite3_snapshot_get(
            /* db = */ db.pointer,
            /* zSchema = */ name.allocateUtf8(),
            /* ppSnapshot = */ snapshotPtr
        )
    }
})

public actual fun sqlite3_snapshot_open(
    db: sqlite3,
    name: String?,
    snapshot: sqlite3_snapshot
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_snapshot_open(
        /* db = */ db.pointer,
        /* zSchema = */ name.allocateUtf8(),
        /* pSnapshot = */ snapshot.pointer
    )
})

public actual fun sqlite3_snapshot_recover(
    db: sqlite3,
    name: String?
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_snapshot_recover(
        /* db = */ db.pointer,
        /* zDb = */ name.allocateUtf8()
    )
})

public actual fun sqlite3_soft_heap_limit64(limit: Long): Long =
    native.sqlite3_soft_heap_limit64(limit)

public actual fun sqlite3_sourceid(): String =
    native.sqlite3_sourceid().getStringUtf8()

public actual fun sqlite3_sql(stmt: sqlite3_stmt): String =
    native.sqlite3_sql(stmt.pointer).getStringUtf8()

public actual fun sqlite3_status(
    option: Sqlite3StatusOption,
    outCurrent: Sqlite3IntOutParam,
    outHighwater: Sqlite3IntOutParam,
    resetFlag: Int
): Sqlite3Result = convertResult(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
    native.sqlite3_status(
        /* op = */ option.id,
        /* pCurrent = */ curPtr,
        /* pHighwater = */ highPtr,
        /* resetFlag = */ resetFlag
    )
})

public actual fun sqlite3_status64(
    option: Sqlite3StatusOption,
    outCurrent: Sqlite3LongOutParam,
    outHighwater: Sqlite3LongOutParam,
    resetFlag: Int
): Sqlite3Result = convertResult(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
    native.sqlite3_status64(
        /* op = */ option.id,
        /* pCurrent = */ curPtr,
        /* pHighwater = */ highPtr,
        /* resetFlag = */ resetFlag
    )
})

public actual fun sqlite3_step(stmt: sqlite3_stmt): Sqlite3Result =
    convertResult(native.sqlite3_step(stmt.pointer))

public actual fun sqlite3_stmt_busy(stmt: sqlite3_stmt): Int =
    native.sqlite3_stmt_busy(stmt.pointer)

public actual fun sqlite3_stmt_explain(
    stmt: sqlite3_stmt,
    mode: Sqlite3ExplainMode
): Sqlite3Result = convertResult(
    native.sqlite3_stmt_explain(
        /* pStmt = */ stmt.pointer,
        /* eMode = */ mode.id
    )
)

public actual fun sqlite3_stmt_isexplain(stmt: sqlite3_stmt): Sqlite3ExplainMode =
    convertExplainMode(native.sqlite3_stmt_isexplain(stmt.pointer))

public actual fun sqlite3_stmt_readonly(stmt: sqlite3_stmt): Int =
    native.sqlite3_stmt_readonly(stmt.pointer)

public actual fun sqlite3_stmt_status(
    stmt: sqlite3_stmt,
    counter: Sqlite3StatementStatusCounter,
    resetFlag: Int
): Int = native.sqlite3_stmt_status(
    /* x0 = */ stmt.pointer,
    /* op = */ counter.id,
    /* resetFlg = */ resetFlag
)

public actual fun sqlite3_strglob(
    pattern: String,
    string: String
): Int = memScoped {
    native.sqlite3_strglob(
        /* zGlob = */ pattern.allocateUtf8(),
        /* zStr = */ string.allocateUtf8()
    )
}

public actual fun sqlite3_stricmp(
    left: String,
    right: String
): Int = memScoped {
    native.sqlite3_stricmp(
        /* x0 = */ left.allocateUtf8(),
        /* x1 = */ right.allocateUtf8()
    )
}

public actual fun sqlite3_strlike(
    pattern: String,
    string: String,
    escape: Char
): Int = memScoped {
    native.sqlite3_strlike(
        /* zGlob = */ pattern.allocateUtf8(),
        /* zStr = */ string.allocateUtf8(),
        /* cEsc = */ escape.code
    )
}

public actual fun sqlite3_strnicmp(
    left: String,
    right: String,
    size: Int
): Int = memScoped {
    native.sqlite3_strnicmp(
        /* x0 = */ left.allocateUtf8(),
        /* x1 = */ right.allocateUtf8(),
        /* x2 = */ size
    )
}

public actual fun sqlite3_system_errno(db: sqlite3): Int =
    native.sqlite3_system_errno(db.pointer)

public actual fun sqlite3_table_column_metadata(
    db: sqlite3,
    dbName: String?,
    tableName: String,
    columnName: String,
    outDataType: Sqlite3Utf8OutParam?,
    outCollationName: Sqlite3Utf8OutParam?,
    outNotNull: Sqlite3IntOutParam?,
    outPrimaryKey: Sqlite3IntOutParam?,
    outAutoIncrement: Sqlite3IntOutParam?
): Sqlite3Result = convertResult(memScoped {
    val dataTypePtr = outDataType?.attach(this)
    val collationNamePtr = outCollationName?.attach(this)
    val notNullPtr = outNotNull?.attach(this)
    val primaryKeyPtr = outPrimaryKey?.attach(this)
    val autoIncrementPtr = outAutoIncrement?.attach(this)

    try {
        native.sqlite3_table_column_metadata(
            /* db = */ db.pointer,
            /* zDbName = */ dbName.allocateUtf8(),
            /* zTableName = */ tableName.allocateUtf8(),
            /* zColumnName = */ columnName.allocateUtf8(),
            /* pzDataType = */ dataTypePtr.notNull,
            /* pzCollSeq = */ collationNamePtr.notNull,
            /* pNotNull = */ notNullPtr.notNull,
            /* pPrimaryKey = */ primaryKeyPtr.notNull,
            /* pAutoinc = */ autoIncrementPtr.notNull
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
        /* x0 = */ db.pointer,
        /* uMask = */ mask?.value ?: 0,
        /* xCallback = */ functionPointer(callback, ::TraceHandler),
        /* pCtx = */ keyedStableRefPointer(KEY_TRACE, callback, userData)
    )
})

public actual fun sqlite3_txn_state(
    db: sqlite3,
    schema: String?
): Sqlite3TransactionState? = convertTransactionState(memScoped {
    native.sqlite3_txn_state(
        /* x0 = */ db.pointer,
        /* zSchema = */ schema.allocateUtf8()
    )
})

public actual fun sqlite3_update_hook(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3UpdateHookCallback?
): sqlite3_mutable_pointer? = db.withMemoryManager {
    sqlite3_mutable_pointer.fromStableRef(
        native.sqlite3_update_hook(
            /* x0 = */ db.pointer,
            /* x1 = */ functionPointer(callback, ::UpdateHookHandler),
            /* x2 = */ keyedStableRefPointer(KEY_UPDATE_HOOK, callback, userData)
        )
    )
}

public actual fun sqlite3_uri_boolean(
    fileName: sqlite3_filename,
    parameter: String,
    default: Int
): Int = memScoped {
    native.sqlite3_uri_boolean(
        /* z = */ fileName.allocateUtf8(),
        /* zParam = */ parameter.allocateUtf8(),
        /* bDefault = */ default
    )
}

public actual fun sqlite3_uri_int64(
    fileName: sqlite3_filename,
    parameter: String,
    default: Long
): Long = memScoped {
    native.sqlite3_uri_int64(
        /* x0 = */ fileName.allocateUtf8(),
        /* x1 = */ parameter.allocateUtf8(),
        /* x2 = */ default
    )
}

public actual fun sqlite3_uri_key(
    fileName: sqlite3_filename,
    index: Int
): String? = memScoped {
    native.sqlite3_uri_key(
        /* z = */ fileName.allocateUtf8(),
        /* N = */ index
    )
}.getStringUtf8OrNull()

public actual fun sqlite3_uri_parameter(
    fileName: sqlite3_filename,
    parameter: String
): String? = memScoped {
    native.sqlite3_uri_parameter(
        /* z = */ fileName.allocateUtf8(),
        /* zParam = */ parameter.allocateUtf8()
    )
}.getStringUtf8OrNull()

public actual fun sqlite3_user_data(context: sqlite3_context): sqlite3_mutable_pointer? =
    context.db.memory
        .stableRefData<CreateFunction>(
            native.sqlite3_user_data(context.pointer).orNull ?: return null
        )
        .second

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

public actual fun sqlite3_value_pointer(
    value: sqlite3_value,
    type: String?
): sqlite3_mutable_pointer? = memScoped {
    globalMemory.stableRefData<NamedPointer>(
        native.sqlite3_value_pointer(
            /* x0 = */ value.pointer,
            /* x1 = */ type.allocateUtf8()
        ) ?: return null
    ).second
}

public actual fun sqlite3_value_subtype(value: sqlite3_value): UInt =
    native.sqlite3_value_subtype(value.pointer).toUInt()

public actual fun sqlite3_value_text(value: sqlite3_value): String? =
    native.sqlite3_value_text(value.pointer).getStringUtf8OrNull()

public actual fun sqlite3_value_type(value: sqlite3_value): Sqlite3DataType =
    convertDataType(native.sqlite3_value_type(value.pointer))

public actual fun sqlite3_vfs_find(name: String?): sqlite3_vfs? = memScoped {
    native.sqlite3_vfs_find(name.allocateUtf8()).orNull?.let(::sqlite3_vfs)
}

public actual fun sqlite3_vfs_register(
    vfs: sqlite3_vfs,
    makeDefault: Int
): Sqlite3Result = convertResult(
    native.sqlite3_vfs_register(
        /* x0 = */ vfs.pointer,
        /* makeDflt = */ makeDefault
    )
)

public actual fun sqlite3_vfs_unregister(vfs: sqlite3_vfs): Sqlite3Result =
    convertResult(native.sqlite3_vfs_unregister(vfs.pointer))

public actual fun sqlite3_vtab_collation(
    info: sqlite3_index_info,
    index: Int
): String? = native.sqlite3_vtab_collation(
    /* x0 = */ info.pointer,
    /* x1 = */ index
).getStringUtf8OrNull()

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
): Int = native.sqlite3_vtab_in(
    /* x0 = */ info.pointer,
    /* iCons = */ index,
    /* bHandle = */ handle
)

public actual fun sqlite3_vtab_in_first(
    value: sqlite3_value,
    outValue: Sqlite3ValueOutParam?
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    native.sqlite3_vtab_in_first(
        /* pVal = */ value.pointer,
        /* ppOut = */ valuePtr
    )
})

public actual fun sqlite3_vtab_in_next(
    value: sqlite3_value,
    outValue: Sqlite3ValueOutParam?
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    native.sqlite3_vtab_in_next(
        /* pVal = */ value.pointer,
        /* ppOut = */ valuePtr
    )
})

public actual fun sqlite3_vtab_nochange(context: sqlite3_context): Int =
    native.sqlite3_vtab_nochange(context.pointer)

public actual fun sqlite3_vtab_on_conflict(db: sqlite3): Sqlite3Result =
    convertResult(native.sqlite3_vtab_on_conflict(db.pointer))

public actual fun sqlite3_vtab_rhs_value(
    info: sqlite3_index_info,
    index: Int,
    outValue: Sqlite3ValueOutParam?
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    native.sqlite3_vtab_rhs_value(
        /* x0 = */ info.pointer,
        /* x1 = */ index,
        /* ppVal = */ valuePtr
    )
})

public actual fun sqlite3_wal_autocheckpoint(
    db: sqlite3,
    nFrame: Int
): Sqlite3Result = convertResult(
    native.sqlite3_wal_autocheckpoint(
        /* db = */ db.pointer,
        /* N = */ nFrame
    )
)

public actual fun sqlite3_wal_checkpoint(
    db: sqlite3,
    name: String?
): Sqlite3Result = convertResult(memScoped {
    native.sqlite3_wal_checkpoint(
        /* db = */ db.pointer,
        /* zDb = */ name.allocateUtf8()
    )
})

public actual fun sqlite3_wal_checkpoint_v2(
    db: sqlite3,
    name: String?,
    mode: Sqlite3CheckpointMode,
    outNLog: Sqlite3IntOutParam?,
    outNCkpt: Sqlite3IntOutParam?
): Sqlite3Result = convertResult(memScoped {
    useParams(outNLog, outNCkpt) { nLogPtr, nCkptPtr ->
        native.sqlite3_wal_checkpoint_v2(
            /* db = */ db.pointer,
            /* zDb = */ name.allocateUtf8(),
            /* eMode = */ mode.id,
            /* pnLog = */ nLogPtr,
            /* pnCkpt = */ nCkptPtr
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
            /* x0 = */ db.pointer,
            /* x1 = */ functionPointer(callback, ::WalHookHandler),
            /* x2 = */ keyedStableRefPointer(KEY_WAL_HOOK, callback, userData)
        )
    )
}