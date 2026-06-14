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
    internal val callbacks: VTabModuleCallbacks<AppData, *, *>,
    override val pointer: CPointer<s3_module>,
    placement: NativeFreeablePlacement? = null
) : Struct(pointer, placement),
    AutoCloseable {

    internal actual constructor(
        version: Int,
        callbacks: VTabModuleCallbacks<AppData, *, *>
    ) : this(callbacks, nativeHeap.alloc<s3_module>().apply {
        iVersion = version

        xCreate = when (callbacks.moduleKind) {
            EponymousOnly -> null
            Eponymous -> VTabConnectHandler
            Ordinal -> VTabCreateHandler
        }

        xConnect = VTabConnectHandler
        xBestIndex = VTabBestIndexHandler
        xDisconnect = VTabDisconnectHandler
        xDestroy = VTabDestroyHandler
        xOpen = VTabOpenHandler
        xClose = VTabCloseHandler
        xFilter = VTabFilterHandler
        xNext = VTabNextHandler
        xEof = VTabEofHandler
        xColumn = VTabColumnHandler
        xRowid = VTabRowidHandler
        xUpdate = callbacks.update?.let { VTabUpdateHandler }
        xBegin = callbacks.begin?.let { VTabBeginHandler }
        xSync = callbacks.sync?.let { VTabSyncHandler }
        xCommit = callbacks.commit?.let { VTabCommitHandler }
        xRollback = callbacks.rollback?.let { VTabRollbackHandler }
        xFindFunction = callbacks.findFunction?.let { VTabFindFunctionHandler }
        xRename = callbacks.rename?.let { VTabRenameHandler }
        xSavepoint = callbacks.savepoint?.let { VTabSavepointHandler }
        xRelease = callbacks.release?.let { VTabReleaseHandler }
        xRollbackTo = callbacks.rollbackTo?.let { VTabRollbackToHandler }
        xIntegrity = callbacks.integrity?.let { VTabIntegrityHandler }
    }.ptr, nativeHeap)

    actual override fun close(): Unit = free()
}