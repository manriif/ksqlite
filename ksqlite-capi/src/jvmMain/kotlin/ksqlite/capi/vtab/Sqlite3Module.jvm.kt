@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.notNull
import java.lang.foreign.MemorySegment

public actual class sqlite3_module<AppData>
internal actual constructor(
    version: Int,
    internal val callbacks: VTabModuleCallbacks<AppData, *, *>
) : Struct(allocate = { s3_module.allocate(this) }),
    AutoCloseable {

    init {
        s3_module.iVersion(pointer, version)

        s3_module.xCreate(
            pointer,
            when (callbacks.moduleKind) {
                EponymousOnly -> MemorySegment.NULL
                Eponymous -> VTabConnectHandler
                Ordinal -> VTabCreateHandler
            }
        )

        s3_module.xConnect(pointer, VTabConnectHandler)
        s3_module.xBestIndex(pointer, VTabBestIndexHandler)
        s3_module.xDisconnect(pointer, VTabDisconnectHandler)
        s3_module.xDestroy(pointer, VTabDestroyHandler)
        s3_module.xOpen(pointer, VTabOpenHandler)
        s3_module.xClose(pointer, VTabCloseHandler)
        s3_module.xFilter(pointer, VTabFilterHandler)
        s3_module.xNext(pointer, VTabNextHandler)
        s3_module.xEof(pointer, VTabEofHandler)
        s3_module.xColumn(pointer, VTabColumnHandler)
        s3_module.xRowid(pointer, VTabRowidHandler)
        s3_module.xUpdate(pointer, callbacks.update?.let { VTabUpdateHandler }.notNull)
        s3_module.xBegin(pointer, callbacks.begin?.let { VTabBeginHandler }.notNull)
        s3_module.xSync(pointer, callbacks.sync?.let { VTabSyncHandler }.notNull)
        s3_module.xCommit(pointer, callbacks.commit?.let { VTabCommitHandler }.notNull)
        s3_module.xRollback(pointer, callbacks.rollback?.let { VTabRollbackHandler }.notNull)

        s3_module.xFindFunction(
            pointer,
            callbacks.findFunction?.let { VTabFindFunctionHandler }.notNull
        )

        s3_module.xRename(pointer, callbacks.rename?.let { VTabRenameHandler }.notNull)
        s3_module.xSavepoint(pointer, callbacks.savepoint?.let { VTabSavepointHandler }.notNull)
        s3_module.xRelease(pointer, callbacks.release?.let { VTabReleaseHandler }.notNull)
        s3_module.xRollbackTo(pointer, callbacks.rollbackTo?.let { VTabRollbackToHandler }.notNull)
        s3_module.xIntegrity(pointer, callbacks.integrity?.let { VTabIntegrityHandler }.notNull)
    }

    @Suppress("ACTUAL_IGNORABILITY_NOT_MATCH_EXPECT")
    actual override fun close(): Unit = free()
}