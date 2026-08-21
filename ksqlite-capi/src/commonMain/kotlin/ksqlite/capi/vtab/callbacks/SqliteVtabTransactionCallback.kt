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
package ksqlite.capi.vtab.callbacks

import ksqlite.capi.vtab.sqlite3_vtab
import ksqlite.types.SqliteResultCode

/**
 * Serves both for [SqliteVtabBeginCallback], [SqliteVtabSyncCallback], [SqliteVtabCommitCallback]
 * and [SqliteVtabRollbackCallback].
 */
public fun interface SqliteVtabTransactionCallback<Vtab : sqlite3_vtab> {

    /**
     * Details on parameters and result can be found here:
     * - [begin](https://sqlite.org/vtab.html#the_xbegin_method).
     * - [sync](https://sqlite.org/vtab.html#the_xsync_method)
     * - [commit](https://sqlite.org/vtab.html#the_xcommit_method)
     * - [rollback](https://sqlite.org/vtab.html#the_xrollback_method)
     */
    public fun apply(vTab: Vtab): SqliteResultCode.OkOrFailure
}

///////////////////////////////////////////////////////////////////////////
// Aliases
///////////////////////////////////////////////////////////////////////////

/**
 * This method begins a transaction on a virtual table. This method is optional. The xBegin pointer
 * of sqlite3_module may be NULL.
 *
 * This method is always followed by one call to either the xCommit or xRollback method. Virtual
 * table transactions do not nest, so the xBegin method will not be invoked more than once on a
 * single virtual table without an intervening call to either xCommit or xRollback. Multiple calls
 * to other methods can and likely will occur in between the xBegin and the corresponding xCommit or
 * xRollback.
 *
 * [The xBegin Method](https://sqlite.org/vtab.html#the_xbegin_method)
 */
public typealias SqliteVtabBeginCallback<Vtab> = SqliteVtabTransactionCallback<Vtab>

/**
 * This method signals the start of a two-phase commit on a virtual table. This method is optional.
 * The xSync pointer of sqlite3_module may be NULL.
 *
 * This method is only invoked after a call to the xBegin method and prior to an xCommit or
 * xRollback. In order to implement two-phase commit, the xSync method on all virtual tables is
 * invoked prior to invoking the xCommit method on any virtual table. If any of the xSync methods
 * fail, the entire transaction is rolled back.
 *
 * [The xSync Method](https://sqlite.org/vtab.html#the_xsync_method)
 */
public typealias SqliteVtabSyncCallback<Vtab> = SqliteVtabTransactionCallback<Vtab>

/**
 * This method causes a virtual table transaction to commit. This method is optional. The xCommit
 * pointer of sqlite3_module may be NULL.
 *
 * A call to this method always follows a prior call to xBegin and xSync.
 *
 * [The xCommit Method](https://sqlite.org/vtab.html#the_xcommit_method)
 */
public typealias SqliteVtabCommitCallback<Vtab> = SqliteVtabTransactionCallback<Vtab>

/**
 * This method causes a virtual table transaction to rollback. This method is optional. The
 * xRollback pointer of sqlite3_module may be NULL.
 *
 * A call to this method always follows a prior call to xBegin.
 *
 * [The xRollback Method](https://sqlite.org/vtab.html#the_xrollback_method)
 */
public typealias SqliteVtabRollbackCallback<Vtab> = SqliteVtabTransactionCallback<Vtab>