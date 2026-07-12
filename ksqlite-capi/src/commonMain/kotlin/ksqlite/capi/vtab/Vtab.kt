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
@file:Suppress("NOTHING_TO_INLINE")

package ksqlite.capi.vtab

import ksqlite.capi.callbacks.SqliteDestroyCallback
import ksqlite.capi.callbacks.SqliteFunctionFuncCallback
import ksqlite.capi.memory.ConcurrentMap
import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_context
import ksqlite.capi.sqlite3_value
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
import ksqlite.capi.vtab.callbacks.VtabCreateOrConnectFailureResult
import ksqlite.capi.vtab.callbacks.VtabCreateOrConnectSuccessResult
import ksqlite.capi.vtab.callbacks.VtabFindFunctionDoNotOverloadResult
import ksqlite.capi.vtab.callbacks.VtabFindFunctionOverloadResult
import ksqlite.capi.vtab.callbacks.VtabIntegrityFailureResult
import ksqlite.capi.vtab.callbacks.VtabIntegritySuccessResult
import ksqlite.capi.vtab.callbacks.VtabOpenSuccessResult
import ksqlite.capi.vtab.callbacks.VtabResultFailureResult
import ksqlite.capi.vtab.callbacks.VtabRowidSuccessResult
import ksqlite.capi.vtab.callbacks.VtabUpdateSuccessResult
import ksqlite.capi.vtab.callbacks.vTabCreateOrConnectScope
import ksqlite.capi.vtab.callbacks.vTabFindFunctionScope
import ksqlite.capi.vtab.callbacks.vTabIntegrityScope
import ksqlite.capi.vtab.callbacks.vTabOpenScope
import ksqlite.capi.vtab.callbacks.vTabRowidScope
import ksqlite.capi.vtab.callbacks.vTabUpdateScope
import ksqlite.types.SqliteResultCode
import ksqlite.capi.vtab.callbacks.SqliteVtabCreateOrConnectCallback as CreateOrConnect

///////////////////////////////////////////////////////////////////////////
// Module
///////////////////////////////////////////////////////////////////////////

/**
 * Callbacks of an SQLite 3 Virtual Table module.
 */
internal class VtabModuleCallbacks<AppData, Vtab : sqlite3_vtab, VtabCursor : sqlite3_vtab_cursor>(
    val create: SqliteVtabCreateCallback<in AppData, Vtab>?,
    val connect: SqliteVtabConnectCallback<in AppData, Vtab>,
    val bestIndex: SqliteVtabBestIndexCallback<Vtab>,
    val disconnect: SqliteVtabDisconnectCallback<Vtab>,
    val destroy: SqliteVtabDestroyCallback<Vtab>,
    val open: SqliteVtabOpenCallback<Vtab, VtabCursor>,
    val close: SqliteVtabCloseCallback<VtabCursor>,
    val filter: SqliteVtabFilterCallback<VtabCursor>,
    val next: SqliteVtabNextCallback<VtabCursor>,
    val eof: SqliteVtabEofCallback<VtabCursor>,
    val column: SqliteVtabColumnCallback<VtabCursor>,
    val rowid: SqliteVtabRowidCallback<VtabCursor>,
    val update: SqliteVtabUpdateCallback<Vtab>?,
    val findFunction: SqliteVtabFindFunctionCallback<Vtab>?,
    val begin: SqliteVtabBeginCallback<Vtab>?,
    val sync: SqliteVtabSyncCallback<Vtab>?,
    val commit: SqliteVtabCommitCallback<Vtab>?,
    val rollback: SqliteVtabRollbackCallback<Vtab>?,
    val rename: SqliteVtabRenameCallback<Vtab>?,
    val savepoint: SqliteVtabSavepointCallback<Vtab>?,
    val release: SqliteVtabReleaseCallback<Vtab>?,
    val rollbackTo: SqliteVtabRollbackToCallback<Vtab>?,
    val integrity: SqliteVtabIntegrityCallback<Vtab>?
) {

    val moduleKind: SqliteModuleKind
        inline get() = when {
            create == null -> SqliteModuleKind.EponymousOnly
            create === connect -> SqliteModuleKind.Eponymous
            else -> SqliteModuleKind.Regular
        }
}

/**
 * Holds virtual table callbacks and application data.
 */
internal data class VtabModule<AppData, Vtab : sqlite3_vtab, VtabCursor : sqlite3_vtab_cursor>(
    val callbacks: VtabModuleCallbacks<AppData, Vtab, VtabCursor>,
    val appData: AppData
)

/**
 * Invokes and returns [block]'s value passing it an instance of [VtabModule] that may be `null`
 * according to [callbacks].
 */
internal inline fun <AppData, R> createVtabModule(
    callbacks: VtabModuleCallbacks<AppData, *, *>?,
    appData: AppData,
    block: (VtabModule<AppData, *, *>?) -> R
): R {
    if (callbacks == null) {
        return block(null)
    }

    return block(VtabModule(callbacks, appData))
}

///////////////////////////////////////////////////////////////////////////
// Vtab
///////////////////////////////////////////////////////////////////////////

/**
 * State for virtual table [vTab].
 * Cursors are kept in a map associated by their address as there is no way to put
 */
internal class VtabState<Vtab : sqlite3_vtab, VtabCursor : sqlite3_vtab_cursor>(
    val vTab: Vtab,
    val callbacks: VtabModuleCallbacks<*, Vtab, VtabCursor>
) {

    /**
     * Map linking [VtabCursor]s by the native address of their associated [sqlite3_vtab_cursor].
     */
    private val cursors = ConcurrentMap<Long, VtabCursor>()

    /**
     * Returns the [VtabCursor] for `this` address.
     * This property should be accessed between xOpen and xClose.
     */
    private inline val Long.cursor: VtabCursor
        get() = checkNotNull(cursors[this]) {
            "Virtual table cursor state for address $this was not found"
        }

    /**
     * Removes this state from [VtabStates].
     */
    private inline fun closeVtab(callback: (Vtab) -> SqliteResultCode) = callback(vTab).code.also {
        check(VtabStates.remove(vTab.address) === this) {
            "Virtual table at address ${vTab.address} is not present or has changed"
        }
    }

    inline fun open(setCursor: (sqlite3_vtab_cursor) -> Unit) = callbacks.open.run {
        when (val result = vTabOpenScope<VtabCursor>().apply(vTab)) {
            is VtabOpenSuccessResult -> {
                setCursor(result.cursor.apply {
                    check(cursors.put(address, this) == null) {
                        "A virtual table cursor instance is already registered with the address " +
                                "$address"
                    }
                })

                SqliteResultCode.OK
            }

            is VtabResultFailureResult -> result.result
        }.code
    }

    inline fun closeCursor(cursorAddress: Long) = cursorAddress.cursor.let { cursor ->
        callbacks.close.apply(cursor).code.also {
            check(cursors.remove(cursor.address) === cursor) {
                "Virtual table cursor at address ${vTab.address} is not present or has changed"
            }
        }
    }

    inline fun disconnect() = closeVtab(callbacks.disconnect::apply)

    inline fun destroy() = closeVtab(callbacks.destroy::apply)

    inline fun bestIndex(info: sqlite3_index_info) = callbacks.bestIndex.apply(vTab, info).code

    inline fun filter(
        cursorAddress: Long,
        idxNum: Int,
        idxStr: String?,
        arguments: Array<sqlite3_value>
    ) = callbacks.filter.apply(cursorAddress.cursor, idxNum, idxStr, arguments).code

    inline fun next(cursorAddress: Long) = callbacks.next.apply(cursorAddress.cursor).code

    inline fun eof(cursorAddress: Long) = callbacks.eof.apply(cursorAddress.cursor)

    inline fun column(
        cursorAddress: Long,
        context: sqlite3_context,
        columnIndex: Int
    ) = callbacks.column.apply(cursorAddress.cursor, context, columnIndex).code

    inline fun rowid(
        cursorAddress: Long,
        setRowid: (Long) -> Unit
    ) = callbacks.rowid.run {
        when (val result = vTabRowidScope().apply(cursorAddress.cursor)) {
            is VtabRowidSuccessResult -> {
                setRowid(result.rowid)
                SqliteResultCode.OK
            }

            is VtabResultFailureResult -> result.result
        }.code
    }

    inline fun update(
        arguments: Array<sqlite3_value>,
        setRowid: (Long) -> Unit
    ) = checkNotNull(callbacks.update).run {
        when (val result = vTabUpdateScope().apply(vTab, arguments)) {
            is VtabUpdateSuccessResult -> {
                result.rowid?.let(setRowid)
                SqliteResultCode.OK
            }

            is VtabResultFailureResult -> result.result
        }.code
    }

    inline fun begin() = checkNotNull(callbacks.begin).apply(vTab).code

    inline fun sync() = checkNotNull(callbacks.sync).apply(vTab).code

    inline fun commit() = checkNotNull(callbacks.commit).apply(vTab).code

    inline fun rollback() = checkNotNull(callbacks.rollback).apply(vTab).code

    inline fun findFunction(
        argumentCount: Int,
        functionName: String,
        setFunction: (
            sqlite3_vtab,
            Any?,
            SqliteFunctionFuncCallback<Any?>,
            SqliteDestroyCallback<Any?>?
        ) -> Unit
    ) = checkNotNull(callbacks.findFunction).run {
        when (val result = vTabFindFunctionScope().apply(vTab, argumentCount, functionName)) {
            VtabFindFunctionDoNotOverloadResult -> 0

            is VtabFindFunctionOverloadResult -> {
                setFunction(vTab, result.appData, result.function, result.destroy)
                result.result
            }
        }
    }

    inline fun rename(newName: String) =
        checkNotNull(callbacks.rename).apply(vTab, newName).code

    inline fun savepoint(savepoint: Int) =
        checkNotNull(callbacks.savepoint).apply(vTab, savepoint).code

    inline fun release(savepoint: Int) =
        checkNotNull(callbacks.release).apply(vTab, savepoint).code

    inline fun rollbackTo(savepoint: Int) =
        checkNotNull(callbacks.rollbackTo).apply(vTab, savepoint).code

    inline fun integrity(
        schema: String,
        tableName: String,
        flags: Int,
        setError: (String) -> Unit
    ) = checkNotNull(callbacks.integrity).run {
        when (val result = vTabIntegrityScope().apply(vTab, schema, tableName, flags)) {
            is VtabIntegritySuccessResult -> {
                result.error?.let(setError)
                SqliteResultCode.OK
            }

            is VtabIntegrityFailureResult -> {
                setError(result.error)
                result.result
            }
        }.code
    }
}

/**
 * Map linking [VtabState]s by the native address of their associated [sqlite3_vtab].
 * This is because there is no way to store an arbitrary object in the native [sqlite3_vtab] struct
 * in most interops.
 */
private val VtabStates = ConcurrentMap<Long, VtabState<*, *>>()

/**
 * Returns the [VtabState] for `this` address.
 * This property should be accessed between xCreate/xConnect and xDisconnect/xDestroy.
 */
private inline val Long.vTabState: VtabState<*, *>
    get() = checkNotNull(VtabStates[this]) {
        "Virtual table state for address $this was not found"
    }

/**
 * Invokes `this` create or connect callback.
 */
private inline fun <AppData, Vtab : sqlite3_vtab> CreateOrConnect<in AppData, Vtab>.createOrConnect(
    module: VtabModule<AppData, Vtab, *>,
    db: sqlite3,
    argv: Array<String>,
    setVtab: (sqlite3_vtab) -> Unit,
    setError: (String) -> Unit
): Int = when (val result = vTabCreateOrConnectScope<Vtab>().apply(db, module.appData, argv)) {
    is VtabCreateOrConnectSuccessResult -> {
        setVtab(result.vTab.apply {
            check(VtabStates.put(address, VtabState(this, module.callbacks)) == null) {
                "A virtual table instance is already registered with the address $address"
            }
        })

        SqliteResultCode.OK
    }

    is VtabCreateOrConnectFailureResult -> {
        setError(result.error)
        SqliteResultCode.ERROR
    }
}.code

/**
 * Invokes the [VtabModuleCallbacks.create].
 */
internal inline fun <AppData, Vtab : sqlite3_vtab> vTabCreate(
    module: VtabModule<AppData, Vtab, *>,
    db: sqlite3,
    argv: Array<String>,
    setVtab: (sqlite3_vtab) -> Unit,
    setError: (String) -> Unit
) = checkNotNull(module.callbacks.create).createOrConnect(module, db, argv, setVtab, setError)

/**
 * Invokes the [VtabModuleCallbacks.connect].
 */
internal inline fun <AppData, Vtab : sqlite3_vtab> vTabConnect(
    module: VtabModule<AppData, Vtab, *>,
    db: sqlite3,
    argv: Array<String>,
    setVtab: (sqlite3_vtab) -> Unit,
    setError: (String) -> Unit
) = module.callbacks.connect.createOrConnect(module, db, argv, setVtab, setError)

/**
 * Invokes the [VtabModuleCallbacks.bestIndex].
 */
internal inline fun vTabBestIndex(
    vTab: Long,
    info: sqlite3_index_info
) = vTab.vTabState.bestIndex(info)

/**
 * Invokes the [VtabModuleCallbacks.disconnect].
 */
internal inline fun vTabDisconnect(vTab: Long): Int = vTab.vTabState.disconnect()

/**
 * Invokes the [VtabModuleCallbacks.destroy].
 */
internal inline fun vTabDestroy(vTab: Long) = vTab.vTabState.destroy()

/**
 * Invokes the [VtabModuleCallbacks.open].
 */
internal inline fun vTabOpen(
    vTab: Long,
    setCursor: (sqlite3_vtab_cursor) -> Unit
) = vTab.vTabState.open(setCursor)

/**
 * Invokes the [VtabModuleCallbacks.close].
 */
internal inline fun vTabClose(
    vTab: Long,
    cursor: Long
): Int = vTab.vTabState.closeCursor(cursor)

/**
 * Invokes the [VtabModuleCallbacks.filter].
 */
internal inline fun vTabFilter(
    vTab: Long,
    cursor: Long,
    idxNum: Int,
    idxStr: String?,
    arguments: Array<sqlite3_value>
) = vTab.vTabState.filter(cursor, idxNum, idxStr, arguments)

/**
 * Invokes the [VtabModuleCallbacks.next].
 */
internal inline fun vTabNext(
    vTab: Long,
    cursor: Long
) = vTab.vTabState.next(cursor)

/**
 * Invokes the [VtabModuleCallbacks.eof].
 */
internal inline fun vTabEof(
    vTab: Long,
    cursor: Long
) = vTab.vTabState.eof(cursor)

/**
 * Invokes the [VtabModuleCallbacks.column].
 */
internal inline fun vTabColumn(
    vTab: Long,
    cursor: Long,
    context: sqlite3_context,
    columnIndex: Int
) = vTab.vTabState.column(cursor, context, columnIndex)

/**
 * Invokes the [VtabModuleCallbacks.rowid].
 */
internal inline fun vTabRowid(
    vTab: Long,
    cursor: Long,
    setRowid: (Long) -> Unit
) = vTab.vTabState.rowid(cursor, setRowid)

/**
 * Invokes the [VtabModuleCallbacks.update].
 */
internal inline fun vTabUpdate(
    vTab: Long,
    arguments: Array<sqlite3_value>,
    setRowid: (Long) -> Unit
) = vTab.vTabState.update(arguments, setRowid)

/**
 * Invokes the [VtabModuleCallbacks.findFunction].
 */
internal inline fun vTabFindFunction(
    vTab: Long,
    argumentCount: Int,
    functionName: String,
    setFunction: (
        sqlite3_vtab,
        Any?,
        SqliteFunctionFuncCallback<Any?>,
        SqliteDestroyCallback<Any?>?
    ) -> Unit
) = vTab.vTabState.findFunction(argumentCount, functionName, setFunction)

/**
 * Invokes the [VtabModuleCallbacks.begin].
 */
internal inline fun vTabBegin(vTab: Long) = vTab.vTabState.begin()

/**
 * Invokes the [VtabModuleCallbacks.sync].
 */
internal inline fun vTabSync(vTab: Long) = vTab.vTabState.sync()

/**
 * Invokes the [VtabModuleCallbacks.commit].
 */
internal inline fun vTabCommit(vTab: Long) = vTab.vTabState.commit()

/**
 * Invokes the [VtabModuleCallbacks.rollback].
 */
internal inline fun vTabRollback(vTab: Long) = vTab.vTabState.rollback()

/**
 * Invokes the [VtabModuleCallbacks.rename].
 */
internal inline fun vTabRename(
    vTab: Long,
    newName: String
) = vTab.vTabState.rename(newName)

/**
 * Invokes the [VtabModuleCallbacks.savepoint].
 */
internal inline fun vTabSavepoint(
    vTab: Long,
    savepoint: Int
) = vTab.vTabState.savepoint(savepoint)

/**
 * Invokes the [VtabModuleCallbacks.release].
 */
internal inline fun vTabRelease(
    vTab: Long,
    savepoint: Int
) = vTab.vTabState.release(savepoint)

/**
 * Invokes the [VtabModuleCallbacks.rollbackTo].
 */
internal inline fun vTabRollbackTo(
    vTab: Long,
    savepoint: Int
) = vTab.vTabState.rollbackTo(savepoint)

/**
 * Invokes the [VtabModuleCallbacks.integrity].
 */
internal inline fun vTabIntegrity(
    vTab: Long,
    schema: String,
    tableName: String,
    flags: Int,
    setError: (String) -> Unit
) = vTab.vTabState.integrity(schema, tableName, flags, setError)