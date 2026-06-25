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
import ksqlite.foreign.js.plus
import ksqlite.foreign.structs.member
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.JsFunction
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction
import kotlin.js.toLong
import ksqlite.foreign.wasm.FunctionSignature.Int32 as I32
import ksqlite.foreign.wasm.FunctionSignature.Int64 as I64

///////////////////////////////////////////////////////////////////////////
// Aliases
///////////////////////////////////////////////////////////////////////////

internal typealias s3_index_info = ksqlite.foreign.structs.sqlite3_index_info
internal typealias s3_module = ksqlite.foreign.structs.sqlite3_module
internal typealias s3_vtab = ksqlite.foreign.structs.sqlite3_vtab
internal typealias s3_vtab_cursor = ksqlite.foreign.structs.sqlite3_vtab_cursor

///////////////////////////////////////////////////////////////////////////
// Cursor
///////////////////////////////////////////////////////////////////////////

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

///////////////////////////////////////////////////////////////////////////
// Handlers
///////////////////////////////////////////////////////////////////////////

/**
 * Installs a new [function] returning [Int] on WASM heap.
 */
private fun vTabHandler(
    vararg params: FunctionSignature.Parameter,
    function: JsFunction
): WasmPointer = wasm.installFunction(
    signature = I32(*params),
    function = function
)

@JsFun("(p0) => handler(p0)")
private external fun function1(handler: (p0: WasmPointer) -> Int): JsFunction

/**
 * Installs a new [vTabHandler] accepting only a single [WasmPointer].
 */
private fun vTabHandler1(handler: (p0: WasmPointer) -> Int) =
    vTabHandler(I64, function = function1(handler))

@JsFun("(p0, p1) => handler(p0, p1)")
private external fun function2(handler: (p0: WasmPointer, p1: WasmPointer) -> Int): JsFunction

/**
 * Installs a new [vTabHandler] accepting two [WasmPointer].
 */
private fun vTabHandler2(handler: (p0: WasmPointer, p1: WasmPointer) -> Int) =
    vTabHandler(I64, I64, function = function2(handler))

///////////////////////////////////////////////////////////////////////////
// Create, connect
///////////////////////////////////////////////////////////////////////////

@JsFun("(p0, p1, p2, p3, p4, p5, p6, p7) => handler(p0, p1, p2, p3, p4, p5, p6, p7)")
private external fun createOrConnect(
    handler: (
        db: WasmPointer,
        refPointer: WasmPointer,
        argc: Int,
        argv: WasmPointer,
        outVtab: WasmPointer,
        outErr: WasmPointer
    ) -> Int
): JsFunction

internal val VTabCreateHandler = vTabHandler(
    I64, I32, I64, I64, I64, I64,
    function = createOrConnect { db, refPointer, argc, argv, outVtab, outErr ->
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
)

internal val VTabConnectHandler = vTabHandler(
    I64, I32, I64, I64, I64, I64,
    function = createOrConnect { db, refPointer, argc, argv, outVtab, outErr ->
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
)

///////////////////////////////////////////////////////////////////////////
// BestIndex
///////////////////////////////////////////////////////////////////////////

@JsFun("(p0, p1) => handler(p0, p1)")
private external fun bestIndex(
    handler: (
        vTab: WasmPointer,
        info: WasmPointer
    ) -> Int
): JsFunction

internal val VTabBestIndexHandler = vTabHandler(
    I64, I64,
    function = bestIndex { vTab, info ->
        vTabBestIndex(
            vTab = vTab.toLong(),
            info = sqlite3_index_info(info)
        )
    }
)

///////////////////////////////////////////////////////////////////////////
// Disconnect, Destroy, Open, Close
///////////////////////////////////////////////////////////////////////////

internal val VTabDisconnectHandler = vTabHandler1 { vTab ->
    vTabDisconnect(vTab.toLong())
}

internal val VTabDestroyHandler = vTabHandler1 { vTab ->
    vTabDestroy(vTab.toLong())
}

internal val VTabOpenHandler = vTabHandler2 { vTab, outCursor ->
    vTabOpen(
        vTab = vTab.toLong(),
        setCursor = { outCursor.setPointerValue(it.pointer) }
    )
}

internal val VTabCloseHandler = vTabHandler1 { cursor: WasmPointer ->
    vTabClose(
        vTab = vTabPointerAddressFromCursor(cursor),
        cursor = cursor.toLong(),
        cleanup = Struct::free
    )
}

///////////////////////////////////////////////////////////////////////////
// Filter
///////////////////////////////////////////////////////////////////////////

@JsFun("(p0, p1, p2, p3, p4) => handler(p0, p1, p2, p3, p4)")
private external fun filter(
    handler: (
        cursor: WasmPointer,
        idxNum: Int,
        idxStr: WasmPointer,
        argc: Int,
        argv: WasmPointer
    ) -> Int
): JsFunction

internal val VTabFilterHandler = vTabHandler(
    I64, I32, I64, I32, I64,
    function = filter { cursor, idxNum, idxStr, argc, argv ->
        vTabFilter(
            vTab = vTabPointerAddressFromCursor(cursor),
            cursor = cursor.toLong(),
            idxNum = idxNum,
            idxStr = idxStr.toKStringFromUtf8OrNull(),
            arguments = argv.toArrayOrEmpty(argc) { sqlite3_value(it) }
        )
    }
)

///////////////////////////////////////////////////////////////////////////
// Next, Eof
///////////////////////////////////////////////////////////////////////////

internal val VTabNextHandler = vTabHandler1 { cursor ->
    vTabNext(
        vTab = vTabPointerAddressFromCursor(cursor),
        cursor = cursor.toLong()
    )
}

internal val VTabEofHandler = vTabHandler1 { cursor ->
    vTabEof(
        vTab = vTabPointerAddressFromCursor(cursor),
        cursor = cursor.toLong()
    )
}

///////////////////////////////////////////////////////////////////////////
// Column
///////////////////////////////////////////////////////////////////////////

@JsFun("(p0, p1, p2) => handler(p0, p1, p2)")
private external fun column(
    handler: (
        cursor: WasmPointer,
        context: WasmPointer,
        columnIndex: Int
    ) -> Int
): JsFunction

internal val VTabColumnHandler = vTabHandler(
    I64, I64, I32,
    function = column { cursor, context, columnIndex ->
        vTabColumn(
            vTab = vTabPointerAddressFromCursor(cursor),
            cursor = cursor.toLong(),
            context = sqlite3_context(context),
            columnIndex = columnIndex
        )
    }
)

///////////////////////////////////////////////////////////////////////////
// Rowid
///////////////////////////////////////////////////////////////////////////

internal val VTabRowidHandler = vTabHandler2 { cursor, outRowid ->
    vTabRowid(
        vTab = vTabPointerAddressFromCursor(cursor),
        cursor = cursor.toLong(),
        setRowid = outRowid::setValue
    )
}

///////////////////////////////////////////////////////////////////////////
// Update
///////////////////////////////////////////////////////////////////////////

@JsFun("(p0, p1, p2, p3) => handler(p0, p1, p2, p3)")
private external fun update(
    handler: (
        vTab: WasmPointer,
        argc: Int,
        argv: WasmPointer,
        outRowid: WasmPointer
    ) -> Int
): JsFunction

internal val VTabUpdateHandler = vTabHandler(
    I64, I32, I64, I64,
    function = update { vTab, argc, argv, outRowid ->
        vTabUpdate(
            vTab = vTab.toLong(),
            arguments = argv.toArrayOrEmpty(argc) { sqlite3_value(it) },
            setRowid = outRowid::setValue
        )
    }
)

///////////////////////////////////////////////////////////////////////////
// FindFunction
///////////////////////////////////////////////////////////////////////////

@JsFun("(p0, p1, p2, p3, p4) => handler(p0, p1, p2, p3, p4)")
private external fun findFunction(
    handler: (
        vTab: WasmPointer,
        argc: Int,
        name: WasmPointer,
        outFn: WasmPointer,
        outData: WasmPointer
    ) -> Int
): JsFunction

internal val VTabFindFunctionHandler = vTabHandler(
    I64, I32, I64, I64, I64,
    function = findFunction { vTab, argc, name, outFn, outData ->
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
)

///////////////////////////////////////////////////////////////////////////
// Begin, Sync, Commit, Rollback, Rename
///////////////////////////////////////////////////////////////////////////

internal val VTabBeginHandler = vTabHandler1 { vTab ->
    vTabBegin(vTab.toLong())
}

internal val VTabSyncHandler = vTabHandler1 { vTab ->
    vTabSync(vTab.toLong())
}

internal val VTabCommitHandler = vTabHandler1 { vTab ->
    vTabCommit(vTab.toLong())
}

internal val VTabRollbackHandler = vTabHandler1 { vTab ->
    vTabRollback(vTab.toLong())
}

internal val VTabRenameHandler = vTabHandler2 { vTab, newName ->
    vTabRename(
        vTab = vTab.toLong(),
        newName = newName.toKStringFromUtf8()
    )
}

///////////////////////////////////////////////////////////////////////////
// Savepoint, Release, RollbackTo
///////////////////////////////////////////////////////////////////////////

@JsFun("(p0, p1) => handler(p0, p1)")
private external fun savepoint(handler: (vTab: WasmPointer, savepoint: Int) -> Int): JsFunction

private fun vTabSavepointHandler(handler: (vTab: WasmPointer, savepoint: Int) -> Int) =
    vTabHandler(I64, I32, function = savepoint(handler))

internal val VTabSavepointHandler = vTabSavepointHandler { vTab, savepoint ->
    vTabSavepoint(
        vTab = vTab.toLong(),
        savepoint = savepoint
    )
}

internal val VTabReleaseHandler = vTabSavepointHandler { vTab, savepoint ->
    vTabRelease(
        vTab = vTab.toLong(),
        savepoint = savepoint
    )
}

internal val VTabRollbackToHandler = vTabSavepointHandler { vTab, savepoint ->
    vTabRollbackTo(
        vTab = vTab.toLong(),
        savepoint = savepoint
    )
}

///////////////////////////////////////////////////////////////////////////
// Integrity
///////////////////////////////////////////////////////////////////////////

@JsFun("(p0, p1, p2, p3, p4) => handler(p0, p1, p2, p3, p4)")
private external fun integrity(
    handler: (
        vTab: WasmPointer,
        schema: WasmPointer,
        tableName: WasmPointer,
        flags: Int,
        outErr: WasmPointer
    ) -> Int
): JsFunction

internal val VTabIntegrityHandler = vTabHandler(
    I64, I64, I64, I32, I64,
    function = integrity { vTab, schema, tableName, flags, outErr ->
        vTabIntegrity(
            vTab = vTab.toLong(),
            schema = schema.toKStringFromUtf8(),
            tableName = tableName.toKStringFromUtf8(),
            flags = flags,
            setError = { outErr.setPointerValue(sqlite3_mprintf(it)) }
        )
    }
)