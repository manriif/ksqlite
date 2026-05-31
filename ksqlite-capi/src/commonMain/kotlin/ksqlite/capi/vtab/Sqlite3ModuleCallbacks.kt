package ksqlite.capi.vtab

import ksqlite.capi.vtab.callbacks.Sqlite3VTabBeginCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabBestIndexCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabCloseCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabColumnCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabCommitCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabConnectCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabCreateCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabDestroyCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabDisconnectCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabEofCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabFilterCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabFindFunctionCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabIntegrityCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabNextCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabOpenCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabReleaseCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabRenameCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabRollbackCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabRollbackToCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabRowidCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabSavepointCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabShadowNameCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabSyncCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabUpdateCallback

/**
 * Callbacks of an SQLite 3 Virtual Table module.
 */
internal class Sqlite3ModuleCallbacks<AppData, VTab : sqlite3_vtab, VTabCursor : sqlite3_vtab_cursor>(
    val create: Sqlite3VTabCreateCallback<AppData, VTab>?,
    val connect: Sqlite3VTabConnectCallback<AppData, VTab>,
    val bestIndex: Sqlite3VTabBestIndexCallback<VTab>,
    val disconnect: Sqlite3VTabDisconnectCallback<VTab>,
    val destroy: Sqlite3VTabDestroyCallback<VTab>,
    val open: Sqlite3VTabOpenCallback<VTab, VTabCursor>,
    val close: Sqlite3VTabCloseCallback<VTabCursor>,
    val filter: Sqlite3VTabFilterCallback<VTabCursor>,
    val next: Sqlite3VTabNextCallback<VTabCursor>,
    val eof: Sqlite3VTabEofCallback<VTabCursor>,
    val column: Sqlite3VTabColumnCallback<VTabCursor>,
    val rowid: Sqlite3VTabRowidCallback<VTabCursor>,
    val update: Sqlite3VTabUpdateCallback<VTab>?,
    val begin: Sqlite3VTabBeginCallback<VTab>?,
    val sync: Sqlite3VTabSyncCallback<VTab>?,
    val commit: Sqlite3VTabCommitCallback<VTab>?,
    val rollback: Sqlite3VTabRollbackCallback<VTab>?,
    val findFunction: Sqlite3VTabFindFunctionCallback<VTab>?,
    val rename: Sqlite3VTabRenameCallback<VTab>?,
    val savepoint: Sqlite3VTabSavepointCallback<VTab>?,
    val release: Sqlite3VTabReleaseCallback<VTab>?,
    val rollbackTo: Sqlite3VTabRollbackToCallback<VTab>?,
    val shadowName: Sqlite3VTabShadowNameCallback?,
    val integrity: Sqlite3VTabIntegrityCallback<VTab>?
) {

    val moduleKind: Sqlite3ModuleKind
        inline get() = when {
            create == null -> Sqlite3ModuleKind.EponymousOnly
            create === connect -> Sqlite3ModuleKind.Eponymous
            else -> Sqlite3ModuleKind.Ordinal
        }
}