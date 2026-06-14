@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.memory.Struct
import ksqlite.types.vtab.SqliteModuleVersion
import ksqlite.capi.vtab.callbacks.SqliteVTabBeginCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabBestIndexCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabCloseCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabColumnCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabCommitCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabConnectCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabCreateCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabDestroyCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabDisconnectCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabEofCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabFilterCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabFindFunctionCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabIntegrityCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabNextCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabOpenCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabReleaseCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabRenameCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabRollbackCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabRollbackToCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabRowidCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabSavepointCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabSyncCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabUpdateCallback

/**
 * This structure, sometimes called a "virtual table module", defines the implementation of a
 * virtual table. This structure consists mostly of methods for the module.
 *
 * [sqlite3_module](https://sqlite.org/c3ref/module.html)
 *
 * -------------------------------------------------------------------------------------------------
 *
 * # Ksqlite
 *
 * The module must be [close]d by the owner when no longer required to release the associated
 * resource(s).
 */
public expect class sqlite3_module<AppData>
internal constructor(
    version: Int,
    callbacks: VTabModuleCallbacks<AppData, *, *>
) : Struct,
    AutoCloseable {

    /**
     * Releases associated resource(s) and frees native memory.
     */
    override fun close()
}

///////////////////////////////////////////////////////////////////////////
// Factory
///////////////////////////////////////////////////////////////////////////

/**
 * Returns an instance of [sqlite3_module].
 * For an eponymous virtual table, [create] and [connect] must be referentially equals (===).
 *
 * See [ksqlite.types.vtab.SqliteModuleVersion.VERSION_3] for explanation about why xShadowName is
 * not supported.
 *
 * The caller take the ownership of the returned [sqlite3_module] and is responsible to release it
 * by invoking [sqlite3_module.close].
 */
public fun <AppData, VTab : sqlite3_vtab, VTabCursor : sqlite3_vtab_cursor> sqlite3_module(
    version: SqliteModuleVersion,
    create: SqliteVTabCreateCallback<in AppData, VTab>?,
    connect: SqliteVTabConnectCallback<in AppData, VTab>,
    bestIndex: SqliteVTabBestIndexCallback<VTab>,
    disconnect: SqliteVTabDisconnectCallback<VTab>,
    destroy: SqliteVTabDestroyCallback<VTab>,
    open: SqliteVTabOpenCallback<VTab, VTabCursor>,
    close: SqliteVTabCloseCallback<VTabCursor>,
    filter: SqliteVTabFilterCallback<VTabCursor>,
    next: SqliteVTabNextCallback<VTabCursor>,
    eof: SqliteVTabEofCallback<VTabCursor>,
    column: SqliteVTabColumnCallback<VTabCursor>,
    rowid: SqliteVTabRowidCallback<VTabCursor>,
    update: SqliteVTabUpdateCallback<VTab>?,
    findFunction: SqliteVTabFindFunctionCallback<VTab>?,
    begin: SqliteVTabBeginCallback<VTab>?,
    sync: SqliteVTabSyncCallback<VTab>?,
    commit: SqliteVTabCommitCallback<VTab>?,
    rollback: SqliteVTabRollbackCallback<VTab>?,
    rename: SqliteVTabRenameCallback<VTab>?,
    savepoint: SqliteVTabSavepointCallback<VTab>?,
    release: SqliteVTabReleaseCallback<VTab>?,
    rollbackTo: SqliteVTabRollbackToCallback<VTab>?,
    integrity: SqliteVTabIntegrityCallback<VTab>?
): sqlite3_module<AppData> = sqlite3_module(
    version = version.iVersion,
    callbacks = VTabModuleCallbacks(
        create = create,
        connect = connect,
        bestIndex = bestIndex,
        disconnect = disconnect,
        destroy = destroy,
        open = open,
        close = close,
        filter = filter,
        next = next,
        eof = eof,
        column = column,
        rowid = rowid,
        update = update,
        findFunction = findFunction,
        begin = begin,
        sync = sync,
        commit = commit,
        rollback = rollback,
        rename = rename,
        savepoint = savepoint,
        release = release,
        rollbackTo = rollbackTo,
        integrity = integrity,
    )
)