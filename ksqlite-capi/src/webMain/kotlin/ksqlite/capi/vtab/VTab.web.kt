package ksqlite.capi.vtab

import ksqlite.capi.createFunction
import ksqlite.capi.functionKey
import ksqlite.capi.handlers.FunctionFuncHandler
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.memory
import ksqlite.capi.memory.setPointerValue
import ksqlite.capi.memory.setValue
import ksqlite.capi.memory.stableRefData
import ksqlite.capi.memory.toArrayOrEmpty
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.memory.toStringArrayOrEmpty
import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_mprintf
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value
import ksqlite.capi.wasm
import ksqlite.foreign.structs.sqlite3_module
import ksqlite.foreign.structs.sqlite3_vtab
import ksqlite.foreign.js.plus
import ksqlite.foreign.structs.member
import ksqlite.foreign.structs.sqlite3_vtab_cursor
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction
import kotlin.js.toLong
import ksqlite.foreign.wasm.FunctionSignature.Int32 as I32
import ksqlite.foreign.wasm.FunctionSignature.Int64 as I64

///////////////////////////////////////////////////////////////////////////
// Aliases
///////////////////////////////////////////////////////////////////////////

internal typealias s3_index_info = ksqlite.foreign.structs.sqlite3_index_info
internal typealias s3_module = sqlite3_module
internal typealias s3_vtab = sqlite3_vtab
internal typealias s3_vtab_cursor = sqlite3_vtab_cursor

///////////////////////////////////////////////////////////////////////////
// Handlers
///////////////////////////////////////////////////////////////////////////

/**
 * Installs a new [function] returning [Int] on WASM heap.
 */
private fun vTabHandler(
    vararg params: FunctionSignature.Parameter,
    function: Function<Int>
): WasmPointer = wasm.installFunction(
    signature = I32(*params),
    function = function
)

internal val VTabCreateHandler = vTabHandler(I64, I32, I64, I64, I64, I64) { db: WasmPointer,
                                                                             refPointer: WasmPointer,
                                                                             argc: Int,
                                                                             argv: WasmPointer,
                                                                             outVtab: WasmPointer,
                                                                             outErr: WasmPointer ->
    sqlite3(db).let { db ->
        vTabCreate(
            module = db.memory.stableRefData<VTabModule<*, *, *>>(refPointer),
            db = db,
            argv = argv.toStringArrayOrEmpty(argc),
            setVTab = { outVtab.setPointerValue(it.pointer) },
            setError = { outErr.setPointerValue(sqlite3_mprintf(it)) }
        )
    }
}

internal val VTabConnectHandler = vTabHandler(I64, I32, I64, I64, I64, I64) { db: WasmPointer,
                                                                              refPointer: WasmPointer,
                                                                              argc: Int,
                                                                              argv: WasmPointer,
                                                                              outVtab: WasmPointer,
                                                                              outErr: WasmPointer ->
    sqlite3(db).let { db ->
        vTabCreate(
            module = db.memory.stableRefData<VTabModule<*, *, *>>(refPointer),
            db = db,
            argv = argv.toStringArrayOrEmpty(argc),
            setVTab = { outVtab.setPointerValue(it.pointer) },
            setError = { outErr.setPointerValue(sqlite3_mprintf(it)) }
        )
    }
}

internal val VTabBestIndexHandler = vTabHandler(I64, I64) { vTab: WasmPointer, info: WasmPointer ->
    vTabBestIndex(
        vTab = vTab.toLong(),
        info = sqlite3_index_info(info)
    )
}

internal val VTabDisconnectHandler = vTabHandler(I64) { vTab: WasmPointer ->
    vTabDisconnect(vTab.toLong())
}

internal val VTabDestroyHandler = vTabHandler(I64) { vTab: WasmPointer ->
    vTabDestroy(vTab.toLong())
}

internal val VTabOpenHandler = vTabHandler(I64, I64) { vTab: WasmPointer, outCursor: WasmPointer ->
    vTabOpen(
        vTab = vTab.toLong(),
        setCursor = { outCursor.setPointerValue(it.pointer) }
    )
}

/**
 * Offset of the pVtab member of [s3_vtab_cursor].
 */
private val vTabCursorPVtabMemberCursor = sqlite3.capi.sqlite3_vtab_cursor.structInfo
    .member(s3_vtab_cursor::pVtab)
    .offset

/**
 * Returns the address of the pVtab member of [s3_vtab_cursor] in [cursor].
 */
private fun vTabPointerAddressFromCursor(cursor: WasmPointer): Long =
    wasm.peekPtr(cursor + vTabCursorPVtabMemberCursor).toLong()

internal val VTabCloseHandler = vTabHandler(I64) { cursor: WasmPointer ->
    vTabClose(
        vTab = vTabPointerAddressFromCursor(cursor),
        cursor = cursor.toLong(),
        cleanup = Struct::free
    )
}

internal val VTabFilterHandler = vTabHandler(I64, I32, I64, I32, I64) { cursor: WasmPointer,
                                                                        idxNum: Int,
                                                                        idxStr: WasmPointer,
                                                                        argc: Int,
                                                                        argv: WasmPointer ->
    vTabFilter(
        vTab = vTabPointerAddressFromCursor(cursor),
        cursor = cursor.toLong(),
        idxNum = idxNum,
        idxStr = idxStr.toKStringFromUtf8OrNull(),
        arguments = argv.toArrayOrEmpty(argc) { sqlite3_value(it) }
    )
}

internal val VTabNextHandler = vTabHandler(I64) { cursor: WasmPointer ->
    vTabNext(
        vTab = vTabPointerAddressFromCursor(cursor),
        cursor = cursor.toLong()
    )
}

internal val VTabEofHandler = vTabHandler(I64) { cursor: WasmPointer ->
    vTabEof(
        vTab = vTabPointerAddressFromCursor(cursor),
        cursor = cursor.toLong()
    )
}

internal val VTabColumnHandler = vTabHandler(I64, I64, I32) { cursor: WasmPointer,
                                                              context: WasmPointer,
                                                              columnIndex: Int ->
    vTabColumn(
        vTab = vTabPointerAddressFromCursor(cursor),
        cursor = cursor.toLong(),
        context = sqlite3_context(context),
        columnIndex = columnIndex
    )
}

internal val VTabRowidHandler = vTabHandler(I64, I64) { cursor: WasmPointer,
                                                        outRowid: WasmPointer ->
    vTabRowid(
        vTab = vTabPointerAddressFromCursor(cursor),
        cursor = cursor.toLong(),
        setRowid = outRowid::setValue
    )
}

internal val VTabUpdateHandler = vTabHandler(I64, I32, I64, I64) { vTab: WasmPointer,
                                                                   argc: Int,
                                                                   argv: WasmPointer,
                                                                   outRowid: WasmPointer ->
    vTabUpdate(
        vTab = vTab.toLong(),
        arguments = argv.toArrayOrEmpty(argc) { sqlite3_value(it) },
        setRowid = outRowid::setValue
    )
}

internal val VTabFindFunctionHandler = vTabHandler(I64, I32, I64, I64, I64) { vTab: WasmPointer,
                                                                              argc: Int,
                                                                              name: WasmPointer,
                                                                              outFn: WasmPointer,
                                                                              outData: WasmPointer ->
    val functionName = name.toKStringFromUtf8()

    vTabFindFunction(
        vTab = vTab.toLong(),
        argumentCount = argc,
        functionName = functionName,
        setFunction = { instance, appData, function, destroy ->
            // Keep the same logic as regular function from C-API
            // The function is bound to the sqlite3_vtab lifecycle
            createFunction(appData, function, null, null, destroy) { fn, fnDestroy ->
                outFn.setPointerValue(instance.memory.functionPointer(::FunctionFuncHandler))

                outData.setPointerValue(
                    instance.memory.keyedStableRefPointer(
                        key = functionKey(functionName, argc, null),
                        data = fn,
                        appData = appData,
                        destructor = fnDestroy
                    )
                )
            }
        }
    )
}

internal val VTabBeginHandler = vTabHandler(I64) { vTab: WasmPointer ->
    vTabBegin(vTab.toLong())
}

internal val VTabSyncHandler = vTabHandler(I64) { vTab: WasmPointer ->
    vTabSync(vTab.toLong())
}

internal val VTabCommitHandler = vTabHandler(I64) { vTab: WasmPointer ->
    vTabCommit(vTab.toLong())
}

internal val VTabRollbackHandler = vTabHandler(I64) { vTab: WasmPointer ->
    vTabRollback(vTab.toLong())
}

internal val VTabRenameHandler = vTabHandler(I64, I64) { vTab: WasmPointer, newName: WasmPointer ->
    vTabRename(
        vTab = vTab.toLong(),
        newName = newName.toKStringFromUtf8()
    )
}

internal val VTabSavepointHandler = vTabHandler(I64, I32) { vTab: WasmPointer, savepoint: Int ->
    vTabSavepoint(
        vTab = vTab.toLong(),
        savepoint = savepoint
    )
}

internal val VTabReleaseHandler = vTabHandler(I64, I32) { vTab: WasmPointer, savepoint: Int ->
    vTabRelease(
        vTab = vTab.toLong(),
        savepoint = savepoint
    )
}

internal val VTabRollbackToHandler = vTabHandler(I64, I32) { vTab: WasmPointer, savepoint: Int ->
    vTabRollbackTo(
        vTab = vTab.toLong(),
        savepoint = savepoint
    )
}

internal val VTabIntegrityHandler = vTabHandler(I64, I64, I64, I32, I64) { vTab: WasmPointer,
                                                                           schema: WasmPointer,
                                                                           tableName: WasmPointer,
                                                                           flags: Int,
                                                                           outErr: WasmPointer ->
    vTabIntegrity(
        vTab = vTab.toLong(),
        schema = schema.toKStringFromUtf8(),
        tableName = tableName.toKStringFromUtf8(),
        flags = flags,
        setError = { outErr.setPointerValue(sqlite3_mprintf(it)) }
    )
}