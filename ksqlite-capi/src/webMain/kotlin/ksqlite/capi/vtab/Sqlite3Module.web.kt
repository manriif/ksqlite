@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.capi
import ksqlite.capi.memory.NullPtr
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.notNull
import ksqlite.structs.invoke

public actual class sqlite3_module<AppData> internal constructor(
    internal val callbacks: VTabModuleCallbacks<AppData, *, *>,
    module: s3_module
) : Struct(module),
    AutoCloseable {

    internal actual constructor(
        version: Int,
        callbacks: VTabModuleCallbacks<AppData, *, *>
    ) : this(callbacks, capi.sqlite3_module().apply {
        iVersion = version

        xCreate = when (callbacks.moduleKind) {
            EponymousOnly -> NullPtr
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
        xUpdate = callbacks.update?.let { VTabUpdateHandler }.notNull
        xBegin = callbacks.begin?.let { VTabBeginHandler }.notNull
        xSync = callbacks.sync?.let { VTabSyncHandler }.notNull
        xCommit = callbacks.commit?.let { VTabCommitHandler }.notNull
        xRollback = callbacks.rollback?.let { VTabRollbackHandler }.notNull
        xFindFunction = callbacks.findFunction?.let { VTabFindFunctionHandler }.notNull
        xRename = callbacks.rename?.let { VTabRenameHandler }.notNull
        xSavepoint = callbacks.savepoint?.let { VTabSavepointHandler }.notNull
        xRelease = callbacks.release?.let { VTabReleaseHandler }.notNull
        xRollbackTo = callbacks.rollbackTo?.let { VTabRollbackToHandler }.notNull
        xIntegrity = callbacks.integrity?.let { VTabIntegrityHandler }.notNull
    })

    actual override fun close(): Unit = free()
}