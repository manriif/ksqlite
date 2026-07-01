@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.capi
import ksqlite.capi.memory.NullPtr
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.notNull
import ksqlite.foreign.structs.invoke

public actual class sqlite3_module<AppData> private constructor(
    internal val callbacks: VtabModuleCallbacks<AppData, *, *>,
    module: s3_module
) : Struct(module),
    AutoCloseable {

    internal actual constructor(
        version: Int,
        callbacks: VtabModuleCallbacks<AppData, *, *>
    ) : this(callbacks, capi.sqlite3_module().apply {
        iVersion = version

        xCreate = when (callbacks.moduleKind) {
            EponymousOnly -> NullPtr
            Eponymous -> VtabConnectHandler
            Ordinal -> VtabCreateHandler
        }

        xConnect = VtabConnectHandler
        xBestIndex = VtabBestIndexHandler
        xDisconnect = VtabDisconnectHandler
        xDestroy = VtabDestroyHandler
        xOpen = VtabOpenHandler
        xClose = VtabCloseHandler
        xFilter = VtabFilterHandler
        xNext = VtabNextHandler
        xEof = VtabEofHandler
        xColumn = VtabColumnHandler
        xRowid = VtabRowidHandler
        xUpdate = callbacks.update?.let { VtabUpdateHandler }.notNull
        xBegin = callbacks.begin?.let { VtabBeginHandler }.notNull
        xSync = callbacks.sync?.let { VtabSyncHandler }.notNull
        xCommit = callbacks.commit?.let { VtabCommitHandler }.notNull
        xRollback = callbacks.rollback?.let { VtabRollbackHandler }.notNull
        xFindFunction = callbacks.findFunction?.let { VtabFindFunctionHandler }.notNull
        xRename = callbacks.rename?.let { VtabRenameHandler }.notNull
        xSavepoint = callbacks.savepoint?.let { VtabSavepointHandler }.notNull
        xRelease = callbacks.release?.let { VtabReleaseHandler }.notNull
        xRollbackTo = callbacks.rollbackTo?.let { VtabRollbackToHandler }.notNull
        xIntegrity = callbacks.integrity?.let { VtabIntegrityHandler }.notNull
    })

    actual override fun close(): Unit = free()
}