package ksqlite.capi.vtab

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.pointed
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.cinterop.toLong
import kotlinx.cinterop.value
import ksqlite.capi.createFunction
import ksqlite.capi.functionKey
import ksqlite.capi.handlers.FunctionFuncHandler
import ksqlite.capi.handlers.callbackHandler
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.memory
import ksqlite.capi.memory.stableRefData
import ksqlite.capi.memory.toArrayOrEmpty
import ksqlite.capi.memory.toStringArrayOrEmpty
import ksqlite.capi.types.s3
import ksqlite.capi.types.s3_context
import ksqlite.capi.types.s3_value
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value
import ksqlite.foreign.sqlite3_int64Var
import ksqlite.foreign.sqlite3_mprintf

///////////////////////////////////////////////////////////////////////////
// Aliases
///////////////////////////////////////////////////////////////////////////

internal typealias s3_index_info = ksqlite.foreign.sqlite3_index_info
internal typealias s3_module = ksqlite.foreign.sqlite3_module
internal typealias s3_vtab = ksqlite.foreign.sqlite3_vtab
internal typealias s3_vtab_cursor = ksqlite.foreign.sqlite3_vtab_cursor

///////////////////////////////////////////////////////////////////////////
// Handlers
///////////////////////////////////////////////////////////////////////////

internal val VTabCreateHandler = staticCFunction { db: CPointer<s3>?,
                                                   refPointer: COpaquePointer?,
                                                   argc: Int,
                                                   argv: CPointer<CPointerVar<ByteVar>>?,
                                                   outVtab: CPointer<CPointerVar<s3_vtab>>?,
                                                   outErrMsg: CPointer<CPointerVar<ByteVar>>? ->
    vTabCreate(
        module = stableRefData<VTabModule<*, *, *>>(refPointer),
        db = sqlite3(db!!),
        argv = argv.toStringArrayOrEmpty(argc),
        setVTab = { outVtab!!.pointed.value = it.pointer },
        setError = { outErrMsg!!.pointed.value = sqlite3_mprintf(it) }
    )
}

internal val VTabConnectHandler = staticCFunction { db: CPointer<s3>?,
                                                    refPointer: COpaquePointer?,
                                                    argc: Int,
                                                    argv: CPointer<CPointerVar<ByteVar>>?,
                                                    outVtab: CPointer<CPointerVar<s3_vtab>>?,
                                                    outErrMsg: CPointer<CPointerVar<ByteVar>>? ->
    vTabConnect(
        module = stableRefData<VTabModule<*, *, *>>(refPointer),
        db = sqlite3(db!!),
        argv = argv.toStringArrayOrEmpty(argc),
        setVTab = { outVtab!!.pointed.value = it.pointer },
        setError = { outErrMsg!!.pointed.value = sqlite3_mprintf(it) }
    )
}

internal val VTabBestIndexHandler = staticCFunction { vTab: CPointer<s3_vtab>?,
                                                      info: CPointer<s3_index_info>? ->
    vTabBestIndex(
        vTab = vTab.toLong(),
        info = sqlite3_index_info(info!!)
    )
}

internal val VTabDisconnectHandler = staticCFunction { vTab: CPointer<s3_vtab>? ->
    vTabDisconnect(vTab.toLong())
}

internal val VTabDestroyHandler = staticCFunction { vTab: CPointer<s3_vtab>? ->
    vTabDestroy(vTab.toLong())
}

internal val VTabOpenHandler = staticCFunction { vTab: CPointer<s3_vtab>?,
                                                 outCursor: CPointer<CPointerVar<s3_vtab_cursor>>? ->
    vTabOpen(
        vTab = vTab.toLong(),
        setCursor = { outCursor!!.pointed.value = it.pointer }
    )
}

internal val VTabCloseHandler = staticCFunction { cursor: CPointer<s3_vtab_cursor>? ->
    vTabClose(
        vTab = cursor!!.pointed.pVtab!!.toLong(),
        cursor = cursor.toLong(),
        cleanup = Struct::free
    )
}

internal val VTabFilterHandler = staticCFunction { cursor: CPointer<s3_vtab_cursor>?,
                                                   idxNum: Int,
                                                   idxStr: CPointer<ByteVar>?,
                                                   argc: Int,
                                                   argv: CPointer<CPointerVar<s3_value>>? ->
    vTabFilter(
        vTab = cursor!!.pointed.pVtab!!.toLong(),
        cursor = cursor.toLong(),
        idxNum = idxNum,
        idxStr = idxStr?.toKStringFromUtf8(),
        arguments = argv.toArrayOrEmpty(argc) { sqlite3_value(it!!) }
    )
}

internal val VTabNextHandler = staticCFunction { cursor: CPointer<s3_vtab_cursor>? ->
    vTabNext(
        vTab = cursor!!.pointed.pVtab!!.toLong(),
        cursor = cursor.toLong()
    )
}

internal val VTabEofHandler = staticCFunction { cursor: CPointer<s3_vtab_cursor>? ->
    vTabEof(
        vTab = cursor!!.pointed.pVtab!!.toLong(),
        cursor = cursor.toLong()
    )
}

internal val VTabColumnHandler = staticCFunction { cursor: CPointer<s3_vtab_cursor>?,
                                                   context: CPointer<s3_context>?,
                                                   columnIndex: Int ->
    vTabColumn(
        vTab = cursor!!.pointed.pVtab!!.toLong(),
        cursor = cursor.toLong(),
        context = sqlite3_context(context!!),
        columnIndex = columnIndex
    )
}

internal val VTabRowidHandler = staticCFunction { cursor: CPointer<s3_vtab_cursor>?,
                                                  outRowid: CPointer<sqlite3_int64Var>? ->
    vTabRowid(
        vTab = cursor!!.pointed.pVtab!!.toLong(),
        cursor = cursor.toLong(),
        setRowid = { outRowid!!.pointed.value = it }
    )
}

internal val VTabUpdateHandler = staticCFunction { vTab: CPointer<s3_vtab>?,
                                                   argc: Int,
                                                   argv: CPointer<CPointerVar<s3_value>>?,
                                                   outRowid: CPointer<sqlite3_int64Var>? ->
    vTabUpdate(
        vTab = vTab.toLong(),
        arguments = argv.toArrayOrEmpty(argc) { sqlite3_value(it!!) },
        setRowid = { outRowid!!.pointed.value = it }
    )
}

internal val VTabFindFunctionHandler = staticCFunction { vTab: CPointer<s3_vtab>?,
                                                         argc: Int,
                                                         name: CPointer<ByteVar>?,
                                                         outFunction: CPointer<CPointerVar<CFunction<(CPointer<s3_context>?, Int, CPointer<CPointerVar<s3_value>>?) -> Unit>>>?,
                                                         outAppData: CPointer<COpaquePointerVar>? ->
    val functionName = name!!.toKStringFromUtf8()

    vTabFindFunction(
        vTab = vTab.toLong(),
        argumentCount = argc,
        functionName = functionName,
        setFunction = { instance, appData, function, destroy ->
            // Keep the same logic as regular function from C-API
            // The function is bound to the sqlite3_vtab lifecycle
            createFunction(appData, function, null, null, destroy) { fn, fnDestroy ->
                outFunction!!.pointed.value = callbackHandler(function, FunctionFuncHandler)

                outAppData!!.pointed.value = instance.memory.keyedStableRefPointer(
                    key = functionKey(functionName, argc, null),
                    data = fn,
                    appData = appData,
                    destructor = fnDestroy
                )
            }
        }
    )
}

internal val VTabBeginHandler = staticCFunction { vTab: CPointer<s3_vtab>? ->
    vTabBegin(vTab.toLong())
}

internal val VTabSyncHandler = staticCFunction { vTab: CPointer<s3_vtab>? ->
    vTabSync(vTab.toLong())
}

internal val VTabCommitHandler = staticCFunction { vTab: CPointer<s3_vtab>? ->
    vTabCommit(vTab.toLong())
}

internal val VTabRollbackHandler = staticCFunction { vTab: CPointer<s3_vtab>? ->
    vTabRollback(vTab.toLong())
}

internal val VTabRenameHandler = staticCFunction { vTab: CPointer<s3_vtab>?,
                                                   newName: CPointer<ByteVar>? ->
    vTabRename(
        vTab = vTab.toLong(),
        newName = newName!!.toKStringFromUtf8()
    )
}

internal val VTabSavepointHandler = staticCFunction { vTab: CPointer<s3_vtab>?,
                                                      savepoint: Int ->
    vTabSavepoint(
        vTab = vTab.toLong(),
        savepoint = savepoint
    )
}

internal val VTabReleaseHandler = staticCFunction { vTab: CPointer<s3_vtab>?,
                                                    savepoint: Int ->
    vTabRelease(
        vTab = vTab.toLong(),
        savepoint = savepoint
    )
}

internal val VTabRollbackToHandler = staticCFunction { vTab: CPointer<s3_vtab>?,
                                                       savepoint: Int ->
    vTabRollbackTo(
        vTab = vTab.toLong(),
        savepoint = savepoint
    )
}

internal val VTabIntegrityHandler = staticCFunction { vTab: CPointer<s3_vtab>?,
                                                      schema: CPointer<ByteVar>?,
                                                      tableName: CPointer<ByteVar>?,
                                                      flags: Int,
                                                      outError: CPointer<CPointerVar<ByteVar>>? ->
    vTabIntegrity(
        vTab = vTab.toLong(),
        schema = schema!!.toKStringFromUtf8(),
        tableName = tableName!!.toKStringFromUtf8(),
        flags = flags,
        setError = { outError!!.pointed.value = sqlite3_mprintf(it) }
    )
}