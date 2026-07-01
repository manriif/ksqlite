@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.memory.NullPtr
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.notNull

public actual class sqlite3_module<AppData> internal actual constructor(
    version: Int,
    internal val callbacks: VtabModuleCallbacks<AppData, *, *>
) : Struct(allocate = s3_module::allocate),
    AutoCloseable {

    init {
        s3_module.iVersion(pointer, version)

        s3_module.xCreate(
            pointer,
            when (callbacks.moduleKind) {
                EponymousOnly -> NullPtr
                Eponymous -> VtabConnectHandler
                Ordinal -> VtabCreateHandler
            }
        )

        s3_module.xConnect(pointer, VtabConnectHandler)
        s3_module.xBestIndex(pointer, VtabBestIndexHandler)
        s3_module.xDisconnect(pointer, VtabDisconnectHandler)
        s3_module.xDestroy(pointer, VtabDestroyHandler)
        s3_module.xOpen(pointer, VtabOpenHandler)
        s3_module.xClose(pointer, VtabCloseHandler)
        s3_module.xFilter(pointer, VtabFilterHandler)
        s3_module.xNext(pointer, VtabNextHandler)
        s3_module.xEof(pointer, VtabEofHandler)
        s3_module.xColumn(pointer, VtabColumnHandler)
        s3_module.xRowid(pointer, VtabRowidHandler)
        s3_module.xUpdate(pointer, callbacks.update?.let { VtabUpdateHandler }.notNull)
        s3_module.xBegin(pointer, callbacks.begin?.let { VtabBeginHandler }.notNull)
        s3_module.xSync(pointer, callbacks.sync?.let { VtabSyncHandler }.notNull)
        s3_module.xCommit(pointer, callbacks.commit?.let { VtabCommitHandler }.notNull)
        s3_module.xRollback(pointer, callbacks.rollback?.let { VtabRollbackHandler }.notNull)

        s3_module.xFindFunction(
            pointer,
            callbacks.findFunction?.let { VtabFindFunctionHandler }.notNull
        )

        s3_module.xRename(pointer, callbacks.rename?.let { VtabRenameHandler }.notNull)
        s3_module.xSavepoint(pointer, callbacks.savepoint?.let { VtabSavepointHandler }.notNull)
        s3_module.xRelease(pointer, callbacks.release?.let { VtabReleaseHandler }.notNull)
        s3_module.xRollbackTo(pointer, callbacks.rollbackTo?.let { VtabRollbackToHandler }.notNull)
        s3_module.xIntegrity(pointer, callbacks.integrity?.let { VtabIntegrityHandler }.notNull)
    }

    @Suppress("ACTUAL_IGNORABILITY_NOT_MATCH_EXPECT")
    actual override fun close(): Unit = free()
}