/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")

package ksqlite.capi.vtab

import ksqlite.capi.capi
import ksqlite.capi.createFunction
import ksqlite.capi.handlers.FunctionFuncHandler
import ksqlite.capi.memory.memory
import ksqlite.capi.memory.setPointerValue
import ksqlite.capi.memory.setValue
import ksqlite.capi.memory.stableRefData
import ksqlite.capi.memory.toArrayOrEmpty
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.memory.toStringArrayOrEmpty
import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_context
import ksqlite.capi.sqlite3_mprintf
import ksqlite.capi.sqlite3_value
import ksqlite.capi.wasm
import ksqlite.foreign.js.plus
import ksqlite.foreign.structs.member
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.JsFunction
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction
import kotlin.js.toLong
import ksqlite.foreign.wasm.FunctionSignature.Int32 as I32
import ksqlite.foreign.wasm.FunctionSignature.Pointer as Ptr

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
private val vTabCursorPVtabMemberCursor = capi.sqlite3_vtab_cursor.structInfo
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

@JsFun("(handler) => (p0) => handler(p0)")
private external fun function1(handler: (p0: WasmPointer) -> Int): JsFunction

/**
 * Installs a new [vTabHandler] accepting only a single [WasmPointer].
 */
private fun vTabHandler1(handler: (p0: WasmPointer) -> Int) =
    vTabHandler(Ptr, function = function1(handler))

@JsFun("(handler) => (p0, p1) => handler(p0, p1)")
private external fun function2(handler: (p0: WasmPointer, p1: WasmPointer) -> Int): JsFunction

/**
 * Installs a new [vTabHandler] accepting two [WasmPointer].
 */
private fun vTabHandler2(handler: (p0: WasmPointer, p1: WasmPointer) -> Int) =
    vTabHandler(Ptr, Ptr, function = function2(handler))

///////////////////////////////////////////////////////////////////////////
// Create, connect
///////////////////////////////////////////////////////////////////////////

@JsFun("(handler) => (p0, p1, p2, p3, p4, p5) => handler(p0, p1, p2, p3, p4, p5)")
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

internal val VtabCreateHandler = vTabHandler(
    Ptr, Ptr, I32, Ptr, Ptr, Ptr,
    function = createOrConnect { db, refPointer, argc, argv, outVtab, outErr ->
        sqlite3(db).let { db ->
            vTabCreate(
                module = db.memory.stableRefData<VtabModule<*, *, *>>(refPointer),
                db = db,
                argv = argv.toStringArrayOrEmpty(argc),
                setVtab = { outVtab.setPointerValue(it.pointer) },
                setError = { outErr.setPointerValue(sqlite3_mprintf(it)) }
            )
        }
    }
)

internal val VtabConnectHandler = vTabHandler(
    Ptr, Ptr, I32, Ptr, Ptr, Ptr,
    function = createOrConnect { db, refPointer, argc, argv, outVtab, outErr ->
        sqlite3(db).let { db ->
            vTabCreate(
                module = db.memory.stableRefData<VtabModule<*, *, *>>(refPointer),
                db = db,
                argv = argv.toStringArrayOrEmpty(argc),
                setVtab = { outVtab.setPointerValue(it.pointer) },
                setError = { outErr.setPointerValue(sqlite3_mprintf(it)) }
            )
        }
    }
)

///////////////////////////////////////////////////////////////////////////
// BestIndex
///////////////////////////////////////////////////////////////////////////

@JsFun("(handler) => (p0, p1) => handler(p0, p1)")
private external fun bestIndex(
    handler: (
        vTab: WasmPointer,
        info: WasmPointer
    ) -> Int
): JsFunction

internal val VtabBestIndexHandler = vTabHandler(
    Ptr, Ptr,
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

internal val VtabDisconnectHandler = vTabHandler1 { vTab ->
    vTabDisconnect(vTab.toLong())
}

internal val VtabDestroyHandler = vTabHandler1 { vTab ->
    vTabDestroy(vTab.toLong())
}

internal val VtabOpenHandler = vTabHandler2 { vTab, outCursor ->
    vTabOpen(
        vTab = vTab.toLong(),
        setCursor = { outCursor.setPointerValue(it.pointer) }
    )
}

internal val VtabCloseHandler = vTabHandler1 { cursor: WasmPointer ->
    vTabClose(
        vTab = vTabPointerAddressFromCursor(cursor),
        cursor = cursor.toLong()
    )
}

///////////////////////////////////////////////////////////////////////////
// Filter
///////////////////////////////////////////////////////////////////////////

@JsFun("(handler) => (p0, p1, p2, p3, p4) => handler(p0, p1, p2, p3, p4)")
private external fun filter(
    handler: (
        cursor: WasmPointer,
        idxNum: Int,
        idxStr: WasmPointer,
        argc: Int,
        argv: WasmPointer
    ) -> Int
): JsFunction

internal val VtabFilterHandler = vTabHandler(
    Ptr, I32, Ptr, I32, Ptr,
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

internal val VtabNextHandler = vTabHandler1 { cursor ->
    vTabNext(
        vTab = vTabPointerAddressFromCursor(cursor),
        cursor = cursor.toLong()
    )
}

internal val VtabEofHandler = vTabHandler1 { cursor ->
    vTabEof(
        vTab = vTabPointerAddressFromCursor(cursor),
        cursor = cursor.toLong()
    )
}

///////////////////////////////////////////////////////////////////////////
// Column
///////////////////////////////////////////////////////////////////////////

@JsFun("(handler) => (p0, p1, p2) => handler(p0, p1, p2)")
private external fun column(
    handler: (
        cursor: WasmPointer,
        context: WasmPointer,
        columnIndex: Int
    ) -> Int
): JsFunction

internal val VtabColumnHandler = vTabHandler(
    Ptr, Ptr, I32,
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

internal val VtabRowidHandler = vTabHandler2 { cursor, outRowid ->
    vTabRowid(
        vTab = vTabPointerAddressFromCursor(cursor),
        cursor = cursor.toLong(),
        setRowid = outRowid::setValue
    )
}

///////////////////////////////////////////////////////////////////////////
// Update
///////////////////////////////////////////////////////////////////////////

@JsFun("(handler) => (p0, p1, p2, p3) => handler(p0, p1, p2, p3)")
private external fun update(
    handler: (
        vTab: WasmPointer,
        argc: Int,
        argv: WasmPointer,
        outRowid: WasmPointer
    ) -> Int
): JsFunction

internal val VtabUpdateHandler = vTabHandler(
    Ptr, I32, Ptr, Ptr,
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

@JsFun("(handler) => (p0, p1, p2, p3, p4) => handler(p0, p1, p2, p3, p4)")
private external fun findFunction(
    handler: (
        vTab: WasmPointer,
        argc: Int,
        name: WasmPointer,
        outFn: WasmPointer,
        outData: WasmPointer
    ) -> Int
): JsFunction

internal val VtabFindFunctionHandler = vTabHandler(
    Ptr, I32, Ptr, Ptr, Ptr,
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
                            key = vTabFunctionKey(functionName, argc),
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

internal val VtabBeginHandler = vTabHandler1 { vTab ->
    vTabBegin(vTab.toLong())
}

internal val VtabSyncHandler = vTabHandler1 { vTab ->
    vTabSync(vTab.toLong())
}

internal val VtabCommitHandler = vTabHandler1 { vTab ->
    vTabCommit(vTab.toLong())
}

internal val VtabRollbackHandler = vTabHandler1 { vTab ->
    vTabRollback(vTab.toLong())
}

internal val VtabRenameHandler = vTabHandler2 { vTab, newName ->
    vTabRename(
        vTab = vTab.toLong(),
        newName = newName.toKStringFromUtf8()
    )
}

///////////////////////////////////////////////////////////////////////////
// Savepoint, Release, RollbackTo
///////////////////////////////////////////////////////////////////////////

@JsFun("(handler) => (p0, p1) => handler(p0, p1)")
private external fun savepoint(handler: (vTab: WasmPointer, savepoint: Int) -> Int): JsFunction

private fun vTabSavepointHandler(handler: (vTab: WasmPointer, savepoint: Int) -> Int) =
    vTabHandler(Ptr, I32, function = savepoint(handler))

internal val VtabSavepointHandler = vTabSavepointHandler { vTab, savepoint ->
    vTabSavepoint(
        vTab = vTab.toLong(),
        savepoint = savepoint
    )
}

internal val VtabReleaseHandler = vTabSavepointHandler { vTab, savepoint ->
    vTabRelease(
        vTab = vTab.toLong(),
        savepoint = savepoint
    )
}

internal val VtabRollbackToHandler = vTabSavepointHandler { vTab, savepoint ->
    vTabRollbackTo(
        vTab = vTab.toLong(),
        savepoint = savepoint
    )
}

///////////////////////////////////////////////////////////////////////////
// Integrity
///////////////////////////////////////////////////////////////////////////

@JsFun("(handler) => (p0, p1, p2, p3, p4) => handler(p0, p1, p2, p3, p4)")
private external fun integrity(
    handler: (
        vTab: WasmPointer,
        schema: WasmPointer,
        tableName: WasmPointer,
        flags: Int,
        outErr: WasmPointer
    ) -> Int
): JsFunction

internal val VtabIntegrityHandler = vTabHandler(
    Ptr, Ptr, Ptr, I32, Ptr,
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