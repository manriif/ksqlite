@file:Suppress("ClassName")

package ksqlite.capi.vtab

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import ksqlite.capi.memory.StructPointer
import ksqlite.capi.vtab.Sqlite3ModuleKind.Eponymous
import ksqlite.capi.vtab.Sqlite3ModuleKind.EponymousOnly
import ksqlite.capi.vtab.Sqlite3ModuleKind.Ordinal

public actual class sqlite3_module<AppData, VTab : sqlite3_vtab, VTabCursor : sqlite3_vtab_cursor>
internal constructor(
    internal val callbacks: Sqlite3ModuleCallbacks<AppData, VTab, VTabCursor>,
    override val pointer: CPointer<s3_module>
) : StructPointer(pointer) {

    internal actual constructor(
        version: Int,
        callbacks: Sqlite3ModuleCallbacks<AppData, VTab, VTabCursor>
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
        xShadowName = callbacks.shadowName?.let { VTabShadowNameHandler }
        xIntegrity = callbacks.integrity?.let { VTabIntegrityHandler }
    }.ptr)
}