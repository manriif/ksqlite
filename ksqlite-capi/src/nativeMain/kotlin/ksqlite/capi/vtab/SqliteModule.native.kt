@file:Suppress("ClassName")

package ksqlite.capi.vtab

import kotlinx.cinterop.CPointer
import ksqlite.capi.memory.AllocatedStruct
import ksqlite.capi.vtab.SqliteModuleKind.Eponymous
import ksqlite.capi.vtab.SqliteModuleKind.EponymousOnly
import ksqlite.capi.vtab.SqliteModuleKind.Regular

public actual class sqlite3_module<AppData> private constructor(
    internal val callbacks: VtabModuleCallbacks<AppData, *, *>,
    override val pointer: CPointer<s3_module>
) : AllocatedStruct(pointer),
    AutoCloseable {

    internal actual constructor(
        version: Int,
        callbacks: VtabModuleCallbacks<AppData, *, *>
    ) : this(callbacks, allocate<s3_module> {
        iVersion = version

        xCreate = when (callbacks.moduleKind) {
            EponymousOnly -> null
            Eponymous -> VtabConnectHandler
            Regular -> VtabCreateHandler
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
        xUpdate = callbacks.update?.let { VtabUpdateHandler }
        xBegin = callbacks.begin?.let { VtabBeginHandler }
        xSync = callbacks.sync?.let { VtabSyncHandler }
        xCommit = callbacks.commit?.let { VtabCommitHandler }
        xRollback = callbacks.rollback?.let { VtabRollbackHandler }
        xFindFunction = callbacks.findFunction?.let { VtabFindFunctionHandler }
        xRename = callbacks.rename?.let { VtabRenameHandler }
        xSavepoint = callbacks.savepoint?.let { VtabSavepointHandler }
        xRelease = callbacks.release?.let { VtabReleaseHandler }
        xRollbackTo = callbacks.rollbackTo?.let { VtabRollbackToHandler }
        xIntegrity = callbacks.integrity?.let { VtabIntegrityHandler }
    })
}