@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.memory.StructPointer
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
 * This structure, sometimes called a "virtual table module", defines the implementation of a
 * virtual table. This structure consists mostly of methods for the module.
 *
 * [sqlite3_module](https://sqlite.org/c3ref/module.html)
 *
 * # Ksqlite
 *
 * For eponymous virtual table, [create] and [connect] must be referentially equals (===).
 */
public expect class sqlite3_module<AppData, VTab : sqlite3_vtab, VTabCursor: sqlite3_vtab_cursor>(
    version: Sqlite3ModuleVersion,
    create: Sqlite3VTabCreateCallback<AppData, VTab>?,
    connect: Sqlite3VTabConnectCallback<AppData, VTab>,
    bestIndex: Sqlite3VTabBestIndexCallback<VTab>,
    disconnect: Sqlite3VTabDisconnectCallback<VTab>,
    destroy: Sqlite3VTabDestroyCallback<VTab>,
    open: Sqlite3VTabOpenCallback<VTab, VTabCursor>,
    close: Sqlite3VTabCloseCallback<VTabCursor>,
    filter: Sqlite3VTabFilterCallback<VTabCursor>,
    next: Sqlite3VTabNextCallback<VTabCursor>,
    eof: Sqlite3VTabEofCallback<VTabCursor>,
    column: Sqlite3VTabColumnCallback<VTabCursor>,
    rowid: Sqlite3VTabRowidCallback<VTabCursor>,
    update: Sqlite3VTabUpdateCallback<VTab>?,
    begin: Sqlite3VTabBeginCallback<VTab>?,
    sync: Sqlite3VTabSyncCallback<VTab>?,
    commit: Sqlite3VTabCommitCallback<VTab>?,
    rollback: Sqlite3VTabRollbackCallback<VTab>?,
    findFunction: Sqlite3VTabFindFunctionCallback<VTab>?,
    rename: Sqlite3VTabRenameCallback<VTab>?,
    savepoint: Sqlite3VTabSavepointCallback<VTab>?,
    release: Sqlite3VTabReleaseCallback<VTab>?,
    rollbackTo: Sqlite3VTabRollbackToCallback<VTab>?,
    shadowName: Sqlite3VTabShadowNameCallback?,
    integrity: Sqlite3VTabIntegrityCallback<VTab>?
) : StructPointer