package ksqlite.capi.vtab

import ksqlite.OutputPointer
import ksqlite.callbacks.DestructorCallback
import ksqlite.callbacks.FunctionCallback
import ksqlite.capi.createFunction
import ksqlite.capi.handlers.FunctionFuncHandler
import ksqlite.capi.handlers.callbackHandler
import ksqlite.capi.handlers.destructorHandler
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.toArray
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value
import ksqlite.callbacks.VTabModuleCallbacks as JniVTabModuleCallbacks

///////////////////////////////////////////////////////////////////////////
// Aliases
///////////////////////////////////////////////////////////////////////////

internal typealias s3_index_info = ksqlite.structs.sqlite3_index_info
internal typealias s3_module = ksqlite.structs.sqlite3_module
internal typealias s3_vtab = ksqlite.structs.sqlite3_vtab
internal typealias s3_vtab_cursor = ksqlite.structs.sqlite3_vtab_cursor

///////////////////////////////////////////////////////////////////////////
// Handlers
///////////////////////////////////////////////////////////////////////////

/**
 * Handles all callbacks of a Virtual Table module.
 */
internal class VTabModuleHandler : JniVTabModuleCallbacks {

    override fun create(
        db: Long,
        appData: Any?,
        argv: Array<String>,
        outVtab: OutputPointer.OfPointer,
        outErrMsg: OutputPointer.OfString
    ): Int = vTabCreate(
        module = appData as VTabModule<*, *, *>,
        db = sqlite3(db),
        argv = argv,
        setVTab = { outVtab.value = it.pointer },
        setError = outErrMsg::value::set
    )

    override fun connect(
        db: Long,
        appData: Any?,
        argv: Array<String>,
        outVtab: OutputPointer.OfPointer,
        outErrMsg: OutputPointer.OfString
    ): Int = vTabCreate(
        module = appData as VTabModule<*, *, *>,
        db = sqlite3(db),
        argv = argv,
        setVTab = { outVtab.value = it.pointer },
        setError = outErrMsg::value::set
    )

    override fun bestIndex(
        vTab: Long,
        info: Long
    ): Int = vTabBestIndex(
        vTab = vTab,
        info = sqlite3_index_info(info)
    )

    override fun disconnect(vTab: Long): Int = vTabDisconnect(
        vTab = vTab,
        cleanup = sqlite3_vtab::free
    )

    override fun destroy(vTab: Long): Int = vTabDestroy(
        vTab = vTab,
        cleanup = sqlite3_vtab::free
    )

    override fun open(
        vTab: Long,
        outCursor: OutputPointer.OfPointer
    ): Int = vTabOpen(
        vTab = vTab,
        setCursor = { outCursor.value = it.pointer }
    )

    override fun close(
        vTab: Long,
        cursor: Long
    ): Int = vTabClose(
        vTab = vTab,
        cursor = cursor,
        cleanup = Struct::free
    )

    override fun filter(
        vTab: Long,
        cursor: Long,
        idxNum: Int,
        idxStr: String?,
        argv: LongArray
    ): Int = vTabFilter(
        vTab = vTab,
        cursor = cursor,
        idxNum = idxNum,
        idxStr = idxStr,
        arguments = argv.toArray(::sqlite3_value),
    )

    override fun next(
        vTab: Long,
        cursor: Long
    ): Int = vTabNext(
        vTab = vTab,
        cursor = cursor,
    )

    override fun eof(
        vTab: Long,
        cursor: Long
    ): Int = vTabEof(
        vTab = vTab,
        cursor = cursor,
    )

    override fun column(
        vTab: Long,
        cursor: Long,
        context: Long,
        columnIndex: Int
    ): Int = vTabColumn(
        vTab = vTab,
        cursor = cursor,
        context = sqlite3_context(context),
        columnIndex = columnIndex
    )

    override fun rowid(
        vTab: Long,
        cursor: Long,
        outRowid: OutputPointer.OfInt64
    ): Int = vTabRowid(
        vTab = vTab,
        cursor = cursor,
        setRowid = outRowid::value::set
    )

    override fun update(
        vTab: Long,
        argv: LongArray,
        outRowid: OutputPointer.OfInt64
    ): Int = vTabUpdate(
        vTab = vTab,
        arguments = argv.toArray(::sqlite3_value),
        setRowid = outRowid::value::set
    )

    override fun begin(vTab: Long): Int = vTabBegin(vTab)

    override fun sync(vTab: Long): Int = vTabSync(vTab)

    override fun commit(vTab: Long): Int = vTabCommit(vTab)

    override fun rollback(vTab: Long): Int = vTabRollback(vTab)

    override fun findFunction(
        vTab: Long,
        argc: Int,
        name: String,
        outAppData: OutputPointer.OfObject<Any>,
        outFunction: OutputPointer.OfObject<FunctionCallback.Func>,
        outDestroy: OutputPointer.OfObject<DestructorCallback>
    ): Int = vTabFindFunction(
        vTab = vTab,
        argumentCount = argc,
        functionName = name,
        setFunction = { _, appData, function ->
            createFunction(appData, function, null, null, null) { fn, fnDestroy ->
                outAppData.value = fn
                outFunction.value = callbackHandler(fn, null, ::FunctionFuncHandler)
                outDestroy.value = destructorHandler(fn, fnDestroy)
            }
        }
    )

    override fun rename(
        vTab: Long,
        newName: String
    ): Int = vTabRename(
        vTab = vTab,
        newName = newName
    )

    override fun savepoint(
        vTab: Long,
        savepoint: Int
    ): Int = vTabSavepoint(
        vTab = vTab,
        savepoint = savepoint
    )

    override fun release(
        vTab: Long,
        savepoint: Int
    ): Int = vTabRelease(
        vTab = vTab,
        savepoint = savepoint
    )

    override fun rollbackTo(
        vTab: Long,
        savepoint: Int
    ): Int = vTabRollbackTo(
        vTab = vTab,
        savepoint = savepoint
    )

    override fun integrity(
        vTab: Long,
        schema: String,
        tableName: String,
        flags: Int,
        outError: OutputPointer.OfString
    ): Int = vTabIntegrity(
        vTab = vTab,
        schema = schema,
        tableName = tableName,
        flags = flags,
        setError = outError::value::set
    )
}