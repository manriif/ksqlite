package ksqlite.capi.vtab

import ksqlite.capi.createFunction
import ksqlite.capi.functionKey
import ksqlite.capi.handlers.FunctionFuncHandler
import ksqlite.capi.memory.StaticMemoryAllocator
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.memory
import ksqlite.capi.memory.setPointerValue
import ksqlite.capi.memory.setValue
import ksqlite.capi.memory.stableRefData
import ksqlite.capi.memory.toArrayOrEmpty
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.memory.toStringArrayOrEmpty
import ksqlite.capi.sqlite3_mprintf
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value
import ksqlite.sqlite3_module.xBegin
import ksqlite.sqlite3_module.xBestIndex
import ksqlite.sqlite3_module.xClose
import ksqlite.sqlite3_module.xColumn
import ksqlite.sqlite3_module.xCommit
import ksqlite.sqlite3_module.xConnect
import ksqlite.sqlite3_module.xCreate
import ksqlite.sqlite3_module.xDestroy
import ksqlite.sqlite3_module.xDisconnect
import ksqlite.sqlite3_module.xEof
import ksqlite.sqlite3_module.xFilter
import ksqlite.sqlite3_module.xFindFunction
import ksqlite.sqlite3_module.xIntegrity
import ksqlite.sqlite3_module.xNext
import ksqlite.sqlite3_module.xOpen
import ksqlite.sqlite3_module.xRelease
import ksqlite.sqlite3_module.xRename
import ksqlite.sqlite3_module.xRollback
import ksqlite.sqlite3_module.xRollbackTo
import ksqlite.sqlite3_module.xRowid
import ksqlite.sqlite3_module.xSavepoint
import ksqlite.sqlite3_module.xSync
import ksqlite.sqlite3_module.xUpdate

///////////////////////////////////////////////////////////////////////////
// Aliases
///////////////////////////////////////////////////////////////////////////

internal typealias s3_index_info = ksqlite.sqlite3_index_info
internal typealias s3_module = ksqlite.sqlite3_module
internal typealias s3_vtab_cursor = ksqlite.sqlite3_vtab_cursor
internal typealias s3_vtab = ksqlite.sqlite3_vtab

///////////////////////////////////////////////////////////////////////////
// Handlers
///////////////////////////////////////////////////////////////////////////

internal val VTabCreateHandler = xCreate.allocate({ db, refPointer, argc, argv, outVtab, outErr ->
    sqlite3(db).let { db ->
        vTabCreate(
            module = db.memory.stableRefData<VTabModule<*, *, *>>(refPointer),
            db = db,
            argv = argv.toStringArrayOrEmpty(argc),
            setVTab = { outVtab.setPointerValue(it.pointer) },
            setError = { outErr.setPointerValue(sqlite3_mprintf(it)) }
        )
    }
}, StaticMemoryAllocator)

internal val VTabConnectHandler = xConnect.allocate({ db, refPointer, argc, argv, outVtab, outErr ->
    sqlite3(db).let { db ->
        vTabCreate(
            module = db.memory.stableRefData<VTabModule<*, *, *>>(refPointer),
            db = db,
            argv = argv.toStringArrayOrEmpty(argc),
            setVTab = { outVtab.setPointerValue(it.pointer) },
            setError = { outErr.setPointerValue(sqlite3_mprintf(it)) }
        )
    }
}, StaticMemoryAllocator)

internal val VTabBestIndexHandler = xBestIndex.allocate({ vTab, info ->
    vTabBestIndex(
        vTab = vTab.address(),
        info = sqlite3_index_info(info)
    )
}, StaticMemoryAllocator)

internal val VTabDisconnectHandler = xDisconnect.allocate({ vTab ->
    vTabDisconnect(vTab.address())
}, StaticMemoryAllocator)

internal val VTabDestroyHandler = xDestroy.allocate({ vTab ->
    vTabDestroy(vTab.address())
}, StaticMemoryAllocator)

internal val VTabOpenHandler = xOpen.allocate({ vTab, outCursor ->
    vTabOpen(
        vTab = vTab.address(),
        setCursor = { outCursor.setPointerValue(it.pointer) }
    )
}, StaticMemoryAllocator)

internal val VTabCloseHandler = xClose.allocate({ cursor ->
    vTabClose(
        vTab = s3_vtab_cursor.pVtab(cursor).address(),
        cursor = cursor.address(),
        cleanup = Struct::free
    )
}, StaticMemoryAllocator)

internal val VTabFilterHandler = xFilter.allocate({ cursor, idxNum, idxStr, argc, argv ->
    vTabFilter(
        vTab = s3_vtab_cursor.pVtab(cursor).address(),
        cursor = cursor.address(),
        idxNum = idxNum,
        idxStr = idxStr.toKStringFromUtf8OrNull(),
        arguments = argv.toArrayOrEmpty(argc, ::sqlite3_value)
    )
}, StaticMemoryAllocator)

internal val VTabNextHandler = xNext.allocate({ cursor ->
    vTabNext(
        vTab = s3_vtab_cursor.pVtab(cursor).address(),
        cursor = cursor.address()
    )
}, StaticMemoryAllocator)

internal val VTabEofHandler = xEof.allocate({ cursor ->
    vTabEof(
        vTab = s3_vtab_cursor.pVtab(cursor).address(),
        cursor = cursor.address()
    )
}, StaticMemoryAllocator)

internal val VTabColumnHandler = xColumn.allocate({ cursor, context, columnIndex ->
    vTabColumn(
        vTab = s3_vtab_cursor.pVtab(cursor).address(),
        cursor = cursor.address(),
        context = sqlite3_context(context),
        columnIndex = columnIndex
    )
}, StaticMemoryAllocator)

internal val VTabRowidHandler = xRowid.allocate({ cursor, outRowid ->
    vTabRowid(
        vTab = s3_vtab_cursor.pVtab(cursor).address(),
        cursor = cursor.address(),
        setRowid = outRowid::setValue
    )
}, StaticMemoryAllocator)

internal val VTabUpdateHandler = xUpdate.allocate({ vTab, argc, argv, outRowid ->
    vTabUpdate(
        vTab = vTab.address(),
        arguments = argv.toArrayOrEmpty(argc, ::sqlite3_value),
        setRowid = outRowid::setValue
    )
}, StaticMemoryAllocator)

internal val VTabBeginHandler = xBegin.allocate({ vTab ->
    vTabBegin(vTab.address())
}, StaticMemoryAllocator)

internal val VTabSyncHandler = xSync.allocate({ vTab ->
    vTabSync(vTab.address())
}, StaticMemoryAllocator)

internal val VTabCommitHandler = xCommit.allocate({ vTab ->
    vTabCommit(vTab.address())
}, StaticMemoryAllocator)

internal val VTabRollbackHandler = xRollback.allocate({ vTab ->
    vTabRollback(vTab.address())
}, StaticMemoryAllocator)

internal val VTabFindFunctionHandler = xFindFunction.allocate({ vTab, argc, name, outFn, outData ->
    val functionName = name.toKStringFromUtf8()

    vTabFindFunction(
        vTab = vTab.address(),
        argumentCount = argc,
        functionName = functionName,
        setFunction = { instance, appData, function ->
            // Keep the same logic as regular function from C-API
            // The function is bound to the sqlite3_vtab lifecycle
            createFunction(appData, function, null, null, null) { fn, fnDestroy ->
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
}, StaticMemoryAllocator)

internal val VTabRenameHandler = xRename.allocate({ vTab, newName ->
    vTabRename(
        vTab = vTab.address(),
        newName = newName.toKStringFromUtf8()
    )
}, StaticMemoryAllocator)

internal val VTabSavepointHandler = xSavepoint.allocate({ vTab, savepoint ->
    vTabSavepoint(
        vTab = vTab.address(),
        savepoint = savepoint
    )
}, StaticMemoryAllocator)

internal val VTabReleaseHandler = xRelease.allocate({ vTab, savepoint ->
    vTabRelease(
        vTab = vTab.address(),
        savepoint = savepoint
    )
}, StaticMemoryAllocator)

internal val VTabRollbackToHandler = xRollbackTo.allocate({ vTab, savepoint ->
    vTabRollbackTo(
        vTab = vTab.address(),
        savepoint = savepoint
    )
}, StaticMemoryAllocator)

internal val VTabIntegrityHandler = xIntegrity.allocate({ vTab, schema, tableName, flags, outErr ->
    vTabIntegrity(
        vTab = vTab.address(),
        schema = schema.toKStringFromUtf8(),
        tableName = tableName.toKStringFromUtf8(),
        flags = flags,
        setError = { outErr.setPointerValue(sqlite3_mprintf(it)) }
    )
}, StaticMemoryAllocator)