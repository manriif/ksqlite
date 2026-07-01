@file:Suppress("ClassName")

package ksqlite.capi.vtab

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.NativeFreeablePlacement
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import ksqlite.capi.memory.Struct
import ksqlite.capi.vtab.SqliteModuleKind.Eponymous
import ksqlite.capi.vtab.SqliteModuleKind.EponymousOnly
import ksqlite.capi.vtab.SqliteModuleKind.Ordinal

public actual class sqlite3_module<AppData> private constructor(
    internal val callbacks: VtabModuleCallbacks<AppData, *, *>,
    override val pointer: CPointer<s3_module>,
    placement: NativeFreeablePlacement? = null
) : Struct(pointer, placement),
    AutoCloseable {

    internal actual constructor(
        version: Int,
        callbacks: VtabModuleCallbacks<AppData, *, *>
    ) : this(callbacks, nativeHeap.alloc<s3_module>().apply {
        iVersion = version

        xCreate = when (callbacks.moduleKind) {
            EponymousOnly -> null
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
    }.ptr, nativeHeap)

    actual override fun close(): Unit = free()
}