@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.memory.StructPointer

public actual class sqlite3_module<AppData>
internal actual constructor(
    version: Int,
    internal val callbacks: VTabModuleCallbacks<AppData, *, *>
) : StructPointer(allocate = { s3_module.allocate(this) }),
    AutoCloseable {

    init {
        s3_module.iVersion(pointer, version)

        s3_module.xCreate(
            pointer,
            when (callbacks.moduleKind) {
                EponymousOnly -> null
                Eponymous -> VTabConnectHandler
                Ordinal -> VTabCreateHandler
            }
        )

        /*xCreate = when (callbacks.moduleKind) {
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
        xIntegrity = callbacks.integrity?.let { VTabIntegrityHandler }*/
    }

    @Suppress("ACTUAL_IGNORABILITY_NOT_MATCH_EXPECT")
    actual override fun close(): Unit = free()
}