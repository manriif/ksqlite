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
package ksqlite.capi.vtab

import ksqlite.capi.createFunction
import ksqlite.capi.handlers.FunctionFuncHandler
import ksqlite.capi.memory.StaticMemoryAllocator
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
import ksqlite.foreign.sqlite3_module.xBegin
import ksqlite.foreign.sqlite3_module.xBestIndex
import ksqlite.foreign.sqlite3_module.xClose
import ksqlite.foreign.sqlite3_module.xColumn
import ksqlite.foreign.sqlite3_module.xCommit
import ksqlite.foreign.sqlite3_module.xConnect
import ksqlite.foreign.sqlite3_module.xCreate
import ksqlite.foreign.sqlite3_module.xDestroy
import ksqlite.foreign.sqlite3_module.xDisconnect
import ksqlite.foreign.sqlite3_module.xEof
import ksqlite.foreign.sqlite3_module.xFilter
import ksqlite.foreign.sqlite3_module.xFindFunction
import ksqlite.foreign.sqlite3_module.xIntegrity
import ksqlite.foreign.sqlite3_module.xNext
import ksqlite.foreign.sqlite3_module.xOpen
import ksqlite.foreign.sqlite3_module.xRelease
import ksqlite.foreign.sqlite3_module.xRename
import ksqlite.foreign.sqlite3_module.xRollback
import ksqlite.foreign.sqlite3_module.xRollbackTo
import ksqlite.foreign.sqlite3_module.xRowid
import ksqlite.foreign.sqlite3_module.xSavepoint
import ksqlite.foreign.sqlite3_module.xSync
import ksqlite.foreign.sqlite3_module.xUpdate

///////////////////////////////////////////////////////////////////////////
// Aliases
///////////////////////////////////////////////////////////////////////////

internal typealias s3_index_info = ksqlite.foreign.sqlite3_index_info
internal typealias s3_module = ksqlite.foreign.sqlite3_module
internal typealias s3_vtab_cursor = ksqlite.foreign.sqlite3_vtab_cursor
internal typealias s3_vtab = ksqlite.foreign.sqlite3_vtab

///////////////////////////////////////////////////////////////////////////
// Handlers
///////////////////////////////////////////////////////////////////////////

internal val VtabCreateHandler = xCreate.allocate({ db, refPointer, argc, argv, outVtab, outErr ->
    sqlite3(db).let { db ->
        vTabCreate(
            module = db.memory.stableRefData<VtabModule<*, *, *>>(refPointer),
            db = db,
            argv = argv.toStringArrayOrEmpty(argc),
            setVtab = { outVtab.setPointerValue(it.pointer) },
            setError = { outErr.setPointerValue(sqlite3_mprintf(it)) }
        )
    }
}, StaticMemoryAllocator)

internal val VtabConnectHandler = xConnect.allocate({ db, refPointer, argc, argv, outVtab, outErr ->
    sqlite3(db).let { db ->
        vTabCreate(
            module = db.memory.stableRefData<VtabModule<*, *, *>>(refPointer),
            db = db,
            argv = argv.toStringArrayOrEmpty(argc),
            setVtab = { outVtab.setPointerValue(it.pointer) },
            setError = { outErr.setPointerValue(sqlite3_mprintf(it)) }
        )
    }
}, StaticMemoryAllocator)

internal val VtabBestIndexHandler = xBestIndex.allocate({ vTab, info ->
    vTabBestIndex(
        vTab = vTab.address(),
        info = sqlite3_index_info(info)
    )
}, StaticMemoryAllocator)

internal val VtabDisconnectHandler = xDisconnect.allocate({ vTab ->
    vTabDisconnect(vTab.address())
}, StaticMemoryAllocator)

internal val VtabDestroyHandler = xDestroy.allocate({ vTab ->
    vTabDestroy(vTab.address())
}, StaticMemoryAllocator)

internal val VtabOpenHandler = xOpen.allocate({ vTab, outCursor ->
    vTabOpen(
        vTab = vTab.address(),
        setCursor = { outCursor.setPointerValue(it.pointer) }
    )
}, StaticMemoryAllocator)

internal val VtabCloseHandler = xClose.allocate({ cursor ->
    vTabClose(
        vTab = s3_vtab_cursor.pVtab(cursor).address(),
        cursor = cursor.address()
    )
}, StaticMemoryAllocator)

internal val VtabFilterHandler = xFilter.allocate({ cursor, idxNum, idxStr, argc, argv ->
    vTabFilter(
        vTab = s3_vtab_cursor.pVtab(cursor).address(),
        cursor = cursor.address(),
        idxNum = idxNum,
        idxStr = idxStr.toKStringFromUtf8OrNull(),
        arguments = argv.toArrayOrEmpty(argc, ::sqlite3_value)
    )
}, StaticMemoryAllocator)

internal val VtabNextHandler = xNext.allocate({ cursor ->
    vTabNext(
        vTab = s3_vtab_cursor.pVtab(cursor).address(),
        cursor = cursor.address()
    )
}, StaticMemoryAllocator)

internal val VtabEofHandler = xEof.allocate({ cursor ->
    vTabEof(
        vTab = s3_vtab_cursor.pVtab(cursor).address(),
        cursor = cursor.address()
    )
}, StaticMemoryAllocator)

internal val VtabColumnHandler = xColumn.allocate({ cursor, context, columnIndex ->
    vTabColumn(
        vTab = s3_vtab_cursor.pVtab(cursor).address(),
        cursor = cursor.address(),
        context = sqlite3_context(context),
        columnIndex = columnIndex
    )
}, StaticMemoryAllocator)

internal val VtabRowidHandler = xRowid.allocate({ cursor, outRowid ->
    vTabRowid(
        vTab = s3_vtab_cursor.pVtab(cursor).address(),
        cursor = cursor.address(),
        setRowid = outRowid::setValue
    )
}, StaticMemoryAllocator)

internal val VtabUpdateHandler = xUpdate.allocate({ vTab, argc, argv, outRowid ->
    vTabUpdate(
        vTab = vTab.address(),
        arguments = argv.toArrayOrEmpty(argc, ::sqlite3_value),
        setRowid = outRowid::setValue
    )
}, StaticMemoryAllocator)

internal val VtabFindFunctionHandler = xFindFunction.allocate({ vTab, argc, name, outFn, outData ->
    val functionName = name.toKStringFromUtf8()

    vTabFindFunction(
        vTab = vTab.address(),
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
}, StaticMemoryAllocator)

internal val VtabBeginHandler = xBegin.allocate({ vTab ->
    vTabBegin(vTab.address())
}, StaticMemoryAllocator)

internal val VtabSyncHandler = xSync.allocate({ vTab ->
    vTabSync(vTab.address())
}, StaticMemoryAllocator)

internal val VtabCommitHandler = xCommit.allocate({ vTab ->
    vTabCommit(vTab.address())
}, StaticMemoryAllocator)

internal val VtabRollbackHandler = xRollback.allocate({ vTab ->
    vTabRollback(vTab.address())
}, StaticMemoryAllocator)

internal val VtabRenameHandler = xRename.allocate({ vTab, newName ->
    vTabRename(
        vTab = vTab.address(),
        newName = newName.toKStringFromUtf8()
    )
}, StaticMemoryAllocator)

internal val VtabSavepointHandler = xSavepoint.allocate({ vTab, savepoint ->
    vTabSavepoint(
        vTab = vTab.address(),
        savepoint = savepoint
    )
}, StaticMemoryAllocator)

internal val VtabReleaseHandler = xRelease.allocate({ vTab, savepoint ->
    vTabRelease(
        vTab = vTab.address(),
        savepoint = savepoint
    )
}, StaticMemoryAllocator)

internal val VtabRollbackToHandler = xRollbackTo.allocate({ vTab, savepoint ->
    vTabRollbackTo(
        vTab = vTab.address(),
        savepoint = savepoint
    )
}, StaticMemoryAllocator)

internal val VtabIntegrityHandler = xIntegrity.allocate({ vTab, schema, tableName, flags, outErr ->
    vTabIntegrity(
        vTab = vTab.address(),
        schema = schema.toKStringFromUtf8(),
        tableName = tableName.toKStringFromUtf8(),
        flags = flags,
        setError = { outErr.setPointerValue(sqlite3_mprintf(it)) }
    )
}, StaticMemoryAllocator)