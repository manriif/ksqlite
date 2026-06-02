package ksqlite.capi.vtab

import ksqlite.capi.createFunction
import ksqlite.capi.functionKey
import ksqlite.capi.handlers.FunctionFuncHandler
import ksqlite.capi.memory.StructPointer
import ksqlite.capi.memory.memory
import ksqlite.capi.memory.setPointer
import ksqlite.capi.memory.setValue
import ksqlite.capi.memory.stableRefData
import ksqlite.capi.memory.toArrayOrEmpty
import ksqlite.capi.memory.toKStringFromUtf8
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
import java.lang.foreign.Arena

/**
 * Arena for all the top level VTab module handlers.
 * Arena is alive during all the application lifetime.
 */
private val VTabModuleArena = Arena.ofShared()

internal val VTabCreateHandler = xCreate.allocate({ db, refPointer, argc, argv, ppVtab, pzErr ->
    sqlite3(db).let { db ->
        vTabCreate(
            module = db.memory.stableRefData<VTabModule<*, *, *>>(refPointer),
            db = db,
            argv = argv.toStringArrayOrEmpty(argc),
            setVTab = { ppVtab.setPointer(it.pointer) },
            setError = { pzErr.setPointer(sqlite3_mprintf(it)) }
        )
    }
}, VTabModuleArena)

internal val VTabConnectHandler = xConnect.allocate({ db, refPointer, argc, argv, ppVtab, pzErr ->
    sqlite3(db).let { db ->
        vTabCreate(
            module = db.memory.stableRefData<VTabModule<*, *, *>>(refPointer),
            db = db,
            argv = argv.toStringArrayOrEmpty(argc),
            setVTab = { ppVtab.setPointer(it.pointer) },
            setError = { pzErr.setPointer(sqlite3_mprintf(it)) }
        )
    }
}, VTabModuleArena)

internal val VTabBestIndexHandler = xBestIndex.allocate({ vTab, info ->
    vTabBestIndex(
        vTab = vTab.address(),
        info = sqlite3_index_info(info)
    )
}, VTabModuleArena)

internal val VTabDisconnectHandler = xDisconnect.allocate({ vTab ->
    vTabDisconnect(
        vTab = vTab.address(),
        destroyMemory = true,
        cleanup = StructPointer::free
    )
}, VTabModuleArena)

internal val VTabDestroyHandler = xDestroy.allocate({ vTab ->
    vTabDestroy(
        vTab = vTab.address(),
        destroyMemory = true,
        cleanup = StructPointer::free
    )
}, VTabModuleArena)

internal val VTabOpenHandler = xOpen.allocate({ vTab, outCursor ->
    vTabOpen(
        vTab = vTab.address(),
        setCursor = { outCursor.setPointer(it.pointer) }
    )
}, VTabModuleArena)

internal val VTabCloseHandler = xClose.allocate({ cursor ->
    vTabClose(
        vTab = s3_vtab_cursor.pVtab(cursor).address(),
        cursor = cursor.address(),
        cleanup = StructPointer::free
    )
}, VTabModuleArena)

internal val VTabFilterHandler = xFilter.allocate({ cursor, idxNum, idxStr, argc, argv ->
    vTabFilter(
        vTab = s3_vtab_cursor.pVtab(cursor).address(),
        cursor = cursor.address(),
        idxNum = idxNum,
        idxStr = idxStr?.toKStringFromUtf8(),
        arguments = argv.toArrayOrEmpty(argc, ::sqlite3_value)
    )
}, VTabModuleArena)

internal val VTabNextHandler = xNext.allocate({ cursor ->
    vTabNext(
        vTab = s3_vtab_cursor.pVtab(cursor).address(),
        cursor = cursor.address()
    )
}, VTabModuleArena)

internal val VTabEofHandler = xEof.allocate({ cursor ->
    vTabEof(
        vTab = s3_vtab_cursor.pVtab(cursor).address(),
        cursor = cursor.address()
    )
}, VTabModuleArena)

internal val VTabColumnHandler = xColumn.allocate({ cursor, context, columnIndex ->
    vTabColumn(
        vTab = s3_vtab_cursor.pVtab(cursor).address(),
        cursor = cursor.address(),
        context = sqlite3_context(context),
        columnIndex = columnIndex
    )
}, VTabModuleArena)

internal val VTabRowidHandler = xRowid.allocate({ cursor, outRowId ->
    vTabRowid(
        vTab = s3_vtab_cursor.pVtab(cursor).address(),
        cursor = cursor.address(),
        setRowid = outRowId::setValue
    )
}, VTabModuleArena)

internal val VTabUpdateHandler = xUpdate.allocate({ vTab, argc, argv, outRowId ->
    vTabUpdate(
        vTab = vTab.address(),
        arguments = argv.toArrayOrEmpty(argc, ::sqlite3_value),
        setRowid = outRowId::setValue
    )
}, VTabModuleArena)

internal val VTabBeginHandler = xBegin.allocate({ vTab ->
    vTabBegin(vTab.address())
}, VTabModuleArena)

internal val VTabSyncHandler = xSync.allocate({ vTab ->
    vTabSync(vTab.address())
}, VTabModuleArena)

internal val VTabCommitHandler = xCommit.allocate({ vTab ->
    vTabCommit(vTab.address())
}, VTabModuleArena)

internal val VTabRollbackHandler = xRollback.allocate({ vTab ->
    vTabRollback(vTab.address())
}, VTabModuleArena)

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
                outFn.setPointer(instance.memory.functionPointer(::FunctionFuncHandler))

                outData.setPointer(
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
}, VTabModuleArena)

internal val VTabRenameHandler = xRename.allocate({ vTab, newName ->
    vTabRename(
        vTab = vTab.address(),
        newName = newName.toKStringFromUtf8()
    )
}, VTabModuleArena)

internal val VTabSavepointHandler = xSavepoint.allocate({ vTab, savepoint ->
    vTabSavepoint(
        vTab = vTab.address(),
        savepoint = savepoint
    )
}, VTabModuleArena)

internal val VTabReleaseHandler = xRelease.allocate({ vTab, savepoint ->
    vTabRelease(
        vTab = vTab.address(),
        savepoint = savepoint
    )
}, VTabModuleArena)

internal val VTabRollbackToHandler = xRollbackTo.allocate({ vTab, savepoint ->
    vTabRollbackTo(
        vTab = vTab.address(),
        savepoint = savepoint
    )
}, VTabModuleArena)

internal val VTabIntegrityHandler = xIntegrity.allocate({ vtab, schema, tableName, flags, outErr ->
    vTabIntegrity(
        vTab = vtab.address(),
        schema = schema.toKStringFromUtf8(),
        tableName = tableName.toKStringFromUtf8(),
        flags = flags,
        setError = { outErr.setPointer(sqlite3_mprintf(it)) }
    )
}, VTabModuleArena)