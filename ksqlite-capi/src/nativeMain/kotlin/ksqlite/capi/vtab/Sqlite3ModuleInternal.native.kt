package ksqlite.capi.vtab

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.pointed
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toLong
import kotlinx.cinterop.value
import ksqlite.capi.memory.StructPointer
import ksqlite.capi.memory.stableRefData
import ksqlite.capi.memory.toStringArrayOrEmpty
import ksqlite.capi.types.s3
import ksqlite.capi.types.s3_context
import ksqlite.capi.types.s3_value
import ksqlite.capi.types.sqlite3
import ksqlite.sqlite3_int64Var
import ksqlite.sqlite3_mprintf

internal val VTabCreateHandler = staticCFunction { db: CPointer<s3>?,
                                                   refPointer: COpaquePointer?,
                                                   argc: Int,
                                                   argv: CPointer<CPointerVar<ByteVar>>?,
                                                   ppVtab: CPointer<CPointerVar<s3_vtab>>?,
                                                   pzErrMsg: CPointer<CPointerVar<ByteVar>>? ->
    vTabCreate(
        module = stableRefData<VTabModule<*, *, *>>(refPointer),
        db = sqlite3(db!!),
        argv = argv.toStringArrayOrEmpty(argc),
        setVTab = { ppVtab!!.pointed.value = it.pointer },
        setError = { pzErrMsg!!.pointed.value = sqlite3_mprintf(it) }
    )
}

internal val VTabConnectHandler = staticCFunction { db: CPointer<s3>?,
                                                    refPointer: COpaquePointer?,
                                                    argc: Int,
                                                    argv: CPointer<CPointerVar<ByteVar>>?,
                                                    ppVtab: CPointer<CPointerVar<s3_vtab>>?,
                                                    pzErrMsg: CPointer<CPointerVar<ByteVar>>? ->
    vTabConnect(
        module = stableRefData<VTabModule<*, *, *>>(refPointer),
        db = sqlite3(db!!),
        argv = argv.toStringArrayOrEmpty(argc),
        setVTab = { ppVtab!!.pointed.value = it.pointer },
        setError = { pzErrMsg!!.pointed.value = sqlite3_mprintf(it) }
    )
}

internal val VTabBestIndexHandler = staticCFunction { vTab: CPointer<s3_vtab>?,
                                                      info: CPointer<s3_index_info>? ->
    vTabBestIndex(vTab.toLong(), sqlite3_index_info(info!!))
}

internal val VTabDisconnectHandler = staticCFunction { vTab: CPointer<s3_vtab>? ->
    vTabDisconnect(vTab.toLong(), StructPointer::free)
}

internal val VTabDestroyHandler = staticCFunction { vTab: CPointer<s3_vtab>? ->
    vTabDestroy(vTab.toLong(), StructPointer::free)
}

internal val VTabOpenHandler = staticCFunction { vTab: CPointer<s3_vtab>?,
                                                 outCursor: CPointer<CPointerVar<s3_vtab_cursor>>? ->
    vTabOpen(vTab.toLong()) { outCursor!!.pointed.value = it.pointer }
}

internal val VTabCloseHandler = staticCFunction { cursor: CPointer<s3_vtab_cursor>? ->
    0
}

internal val VTabFilterHandler = staticCFunction { cursor: CPointer<s3_vtab_cursor>?,
                                                   idxNum: Int,
                                                   idxStr: CPointer<ByteVar>?,
                                                   argc: Int,
                                                   argv: CPointer<CPointerVar<s3_value>>? ->
    0
}

internal val VTabNextHandler = staticCFunction { cursor: CPointer<s3_vtab_cursor>? ->
    0
}

internal val VTabEofHandler = staticCFunction { cursor: CPointer<s3_vtab_cursor>? ->
    0
}

internal val VTabColumnHandler = staticCFunction { cursor: CPointer<s3_vtab_cursor>?,
                                                   context: CPointer<s3_context>?,
                                                   columnIndex: Int ->
    0
}

internal val VTabRowidHandler = staticCFunction { vTab: CPointer<s3_vtab_cursor>?,
                                                  outRowId: CPointer<sqlite3_int64Var>? ->
    0
}

internal val VTabUpdateHandler = staticCFunction { vTab: CPointer<s3_vtab>?,
                                                   argc: Int,
                                                   argv: CPointer<CPointerVar<s3_value>>?,
                                                   outRowId: CPointer<sqlite3_int64Var>? ->
    0
}

internal val VTabBeginHandler = staticCFunction { vTab: CPointer<s3_vtab>? ->
    0
}

internal val VTabSyncHandler = staticCFunction { vTab: CPointer<s3_vtab>? ->
    0
}

internal val VTabCommitHandler = staticCFunction { vTab: CPointer<s3_vtab>? ->
    0
}

internal val VTabRollbackHandler = staticCFunction { vTab: CPointer<s3_vtab>? ->
    0
}

internal val VTabFindFunctionHandler = staticCFunction { vTab: CPointer<s3_vtab>?,
                                                         argc: Int, name: CPointer<ByteVar>?,
                                                         outFunction: CPointer<CPointerVar<CFunction<(CPointer<s3_context>?, Int, CPointer<CPointerVar<s3_value>>?) -> Unit>>>?,
                                                         outAppData: CPointer<COpaquePointerVar>? ->
    0
}

internal val VTabRenameHandler = staticCFunction { vTab: CPointer<s3_vtab>?,
                                                   newName: CPointer<ByteVar>? ->
    0
}

internal val VTabSavepointHandler = staticCFunction { vTab: CPointer<s3_vtab>?,
                                                      savepoint: Int ->
    0
}

internal val VTabReleaseHandler = staticCFunction { vTab: CPointer<s3_vtab>?,
                                                    savepoint: Int ->
    0
}

internal val VTabRollbackToHandler = staticCFunction { vTab: CPointer<s3_vtab>?,
                                                       savepoint: Int ->
    0
}

internal val VTabShadowNameHandler = staticCFunction { name: CPointer<ByteVar>? ->
    0
}

internal val VTabIntegrityHandler = staticCFunction { vtab: CPointer<s3_vtab>?,
                                                      schema: CPointer<ByteVar>?,
                                                      tableName: CPointer<ByteVar>?,
                                                      flags: Int,
                                                      outError: CPointer<CPointerVar<ByteVar>>? ->
    0
}