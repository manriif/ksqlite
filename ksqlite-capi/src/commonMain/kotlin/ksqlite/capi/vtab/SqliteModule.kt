/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.memory.ClosableStruct
import ksqlite.capi.vtab.callbacks.SqliteVtabBeginCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabBestIndexCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabCloseCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabColumnCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabCommitCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabConnectCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabCreateCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabDestroyCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabDisconnectCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabEofCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabFilterCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabFindFunctionCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabIntegrityCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabNextCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabOpenCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabReleaseCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabRenameCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabRollbackCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabRollbackToCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabRowidCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabSavepointCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabSyncCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabUpdateCallback
import ksqlite.types.vtab.SqliteModuleVersion

/**
 * This structure, sometimes called a "virtual table module", defines the implementation of a
 * virtual table. This structure consists mostly of methods for the module.
 *
 * [sqlite3_module](https://sqlite.org/c3ref/module.html)
 */
public expect class sqlite3_module<AppData> internal constructor(
    version: Int,
    callbacks: VtabModuleCallbacks<AppData, *, *>
) : ClosableStruct

///////////////////////////////////////////////////////////////////////////
// Factory
///////////////////////////////////////////////////////////////////////////

/**
 * Returns an instance of [sqlite3_module].
 * For an eponymous virtual table, [create] and [connect] must be referentially equals (===).
 *
 * The caller take the ownership of the returned [sqlite3_module] and is responsible to release it
 * by invoking [sqlite3_module.close].
 */
public fun <AppData, Vtab : sqlite3_vtab, VtabCursor : sqlite3_vtab_cursor> sqlite3_module(
    version: SqliteModuleVersion,
    create: SqliteVtabCreateCallback<in AppData, Vtab>?,
    connect: SqliteVtabConnectCallback<in AppData, Vtab>,
    bestIndex: SqliteVtabBestIndexCallback<Vtab>,
    disconnect: SqliteVtabDisconnectCallback<Vtab>,
    destroy: SqliteVtabDestroyCallback<Vtab>,
    open: SqliteVtabOpenCallback<Vtab, VtabCursor>,
    close: SqliteVtabCloseCallback<VtabCursor>,
    filter: SqliteVtabFilterCallback<VtabCursor>,
    next: SqliteVtabNextCallback<VtabCursor>,
    eof: SqliteVtabEofCallback<VtabCursor>,
    column: SqliteVtabColumnCallback<VtabCursor>,
    rowid: SqliteVtabRowidCallback<VtabCursor>,
    update: SqliteVtabUpdateCallback<Vtab>?,
    findFunction: SqliteVtabFindFunctionCallback<Vtab>?,
    begin: SqliteVtabBeginCallback<Vtab>?,
    sync: SqliteVtabSyncCallback<Vtab>?,
    commit: SqliteVtabCommitCallback<Vtab>?,
    rollback: SqliteVtabRollbackCallback<Vtab>?,
    rename: SqliteVtabRenameCallback<Vtab>?,
    savepoint: SqliteVtabSavepointCallback<Vtab>?,
    release: SqliteVtabReleaseCallback<Vtab>?,
    rollbackTo: SqliteVtabRollbackToCallback<Vtab>?,
    integrity: SqliteVtabIntegrityCallback<Vtab>?
): sqlite3_module<AppData> = sqlite3_module(
    version = version.iVersion,
    callbacks = VtabModuleCallbacks(
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