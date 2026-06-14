@file:Suppress("NOTHING_TO_INLINE")

package ksqlite.capi.vtab

import ksqlite.capi.callbacks.SqliteDestroyCallback
import ksqlite.capi.callbacks.SqliteFunctionFuncCallback
import ksqlite.capi.memory.ConcurrentMap
import ksqlite.types.SqliteResultCode
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value
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
import ksqlite.capi.vtab.callbacks.VTabCreateOrConnectFailureResult
import ksqlite.capi.vtab.callbacks.VTabCreateOrConnectSuccessResult
import ksqlite.capi.vtab.callbacks.VTabFindFunctionDoNotOverloadResult
import ksqlite.capi.vtab.callbacks.VTabFindFunctionOverloadResult
import ksqlite.capi.vtab.callbacks.VTabIntegrityFailureResult
import ksqlite.capi.vtab.callbacks.VTabIntegritySuccessResult
import ksqlite.capi.vtab.callbacks.VTabOpenSuccessResult
import ksqlite.capi.vtab.callbacks.VTabResultFailureResult
import ksqlite.capi.vtab.callbacks.VTabRowidSuccessResult
import ksqlite.capi.vtab.callbacks.VTabUpdateSuccessResult
import ksqlite.capi.vtab.callbacks.vTabCreateOrConnectScope
import ksqlite.capi.vtab.callbacks.vTabFindFunctionScope
import ksqlite.capi.vtab.callbacks.vTabIntegrityScope
import ksqlite.capi.vtab.callbacks.vTabOpenScope
import ksqlite.capi.vtab.callbacks.vTabRowidScope
import ksqlite.capi.vtab.callbacks.vTabUpdateScope
import ksqlite.capi.vtab.callbacks.SqliteVTabCreateOrConnectCallback as CreateOrConnect

///////////////////////////////////////////////////////////////////////////
// Module
///////////////////////////////////////////////////////////////////////////

/**
 * Callbacks of an SQLite 3 Virtual Table module.
 */
internal class VTabModuleCallbacks<AppData, VTab : sqlite3_vtab, VTabCursor : sqlite3_vtab_cursor>(
    val create: SqliteVTabCreateCallback<in AppData, VTab>?,
    val connect: SqliteVTabConnectCallback<in AppData, VTab>,
    val bestIndex: SqliteVTabBestIndexCallback<VTab>,
    val disconnect: SqliteVTabDisconnectCallback<VTab>,
    val destroy: SqliteVTabDestroyCallback<VTab>,
    val open: SqliteVTabOpenCallback<VTab, VTabCursor>,
    val close: SqliteVTabCloseCallback<VTabCursor>,
    val filter: SqliteVTabFilterCallback<VTabCursor>,
    val next: SqliteVTabNextCallback<VTabCursor>,
    val eof: SqliteVTabEofCallback<VTabCursor>,
    val column: SqliteVTabColumnCallback<VTabCursor>,
    val rowid: SqliteVTabRowidCallback<VTabCursor>,
    val update: SqliteVTabUpdateCallback<VTab>?,
    val findFunction: SqliteVTabFindFunctionCallback<VTab>?,
    val begin: SqliteVTabBeginCallback<VTab>?,
    val sync: SqliteVTabSyncCallback<VTab>?,
    val commit: SqliteVTabCommitCallback<VTab>?,
    val rollback: SqliteVTabRollbackCallback<VTab>?,
    val rename: SqliteVTabRenameCallback<VTab>?,
    val savepoint: SqliteVTabSavepointCallback<VTab>?,
    val release: SqliteVTabReleaseCallback<VTab>?,
    val rollbackTo: SqliteVTabRollbackToCallback<VTab>?,
    val integrity: SqliteVTabIntegrityCallback<VTab>?
) {

    val moduleKind: SqliteModuleKind
        inline get() = when {
            create == null -> SqliteModuleKind.EponymousOnly
            create === connect -> SqliteModuleKind.Eponymous
            else -> SqliteModuleKind.Ordinal
        }
}

/**
 * Holds virtual table callbacks and application data.
 */
internal data class VTabModule<AppData, VTab : sqlite3_vtab, VTabCursor : sqlite3_vtab_cursor>(
    val callbacks: VTabModuleCallbacks<AppData, VTab, VTabCursor>,
    val appData: AppData
)

/**
 * Invokes and returns [block]'s value passing it an instance of [VTabModule] that may be `null`
 * according to [callbacks].
 */
internal inline fun <AppData, R> createVTabModule(
    callbacks: VTabModuleCallbacks<AppData, *, *>?,
    appData: AppData,
    block: (VTabModule<AppData, *, *>?) -> R
): R {
    if (callbacks == null) {
        return block(null)
    }

    return block(VTabModule(callbacks, appData))
}

///////////////////////////////////////////////////////////////////////////
// VTab
///////////////////////////////////////////////////////////////////////////

/**
 * State for virtual table [vTab].
 * Cursors are kept in a map associated by their address as there is no way to put
 */
internal class VTabState<VTab : sqlite3_vtab, VTabCursor : sqlite3_vtab_cursor>(
    val vTab: VTab,
    val callbacks: VTabModuleCallbacks<*, VTab, VTabCursor>
) {

    /**
     * Map linking [VTabCursor]s by the native address of their associated [sqlite3_vtab_cursor].
     */
    private val cursors = ConcurrentMap<Long, VTabCursor>()

    /**
     * Returns the [VTabCursor] for `this` address.
     * This property should be accessed between xOpen and xClose.
     */
    private inline val Long.cursor: VTabCursor
        get() = checkNotNull(cursors[this]) {
            "Virtual table cursor state for address $this was not found"
        }

    /**
     * Removes this state from [VTabStates].
     */
    private inline fun closeVTab(
        cleanup: (sqlite3_vtab) -> Unit,
        callback: (VTab) -> SqliteResultCode
    ) = callback(vTab).code.also {
        check(VTabStates.remove(vTab.address) === this) {
            "Virtual table at address ${vTab.address} is not present or has changed"
        }

        cleanup(vTab)
    }

    inline fun open(setCursor: (sqlite3_vtab_cursor) -> Unit) = callbacks.open.run {
        when (val result = vTabOpenScope<VTabCursor>().handle(vTab)) {
            is VTabOpenSuccessResult -> {
                setCursor(result.cursor.apply {
                    check(cursors.put(address, this) == null) {
                        "A virtual table cursor instance is already registered with the address " +
                                "$address"
                    }
                })

                SqliteResultCode.OK
            }

            is VTabResultFailureResult -> result.result
        }.code
    }

    inline fun closeCursor(
        cursorAddress: Long,
        cleanup: (sqlite3_vtab_cursor) -> Unit,
    ) = cursorAddress.cursor.let { cursor ->
        callbacks.close.handle(cursor).code.also {
            check(cursors.remove(cursor.address) === cursor) {
                "Virtual table cursor at address ${vTab.address} is not present or has changed"
            }

            cleanup(cursor)
        }
    }

    inline fun disconnect(cleanup: (sqlite3_vtab) -> Unit) =
        closeVTab(cleanup, callbacks.disconnect::handle)

    inline fun destroy(cleanup: (sqlite3_vtab) -> Unit) =
        closeVTab(cleanup, callbacks.destroy::handle)

    inline fun bestIndex(info: sqlite3_index_info) = callbacks.bestIndex.handle(vTab, info).code

    inline fun filter(
        cursorAddress: Long,
        idxNum: Int,
        idxStr: String?,
        arguments: Array<sqlite3_value>
    ) = callbacks.filter.handle(cursorAddress.cursor, idxNum, idxStr, arguments).code

    inline fun next(cursorAddress: Long) = callbacks.next.handle(cursorAddress.cursor).code

    inline fun eof(cursorAddress: Long) = callbacks.eof.handle(cursorAddress.cursor)

    inline fun column(
        cursorAddress: Long,
        context: sqlite3_context,
        columnIndex: Int
    ) = callbacks.column.handle(cursorAddress.cursor, context, columnIndex).code

    inline fun rowid(
        cursorAddress: Long,
        setRowid: (Long) -> Unit
    ) = callbacks.rowid.run {
        when (val result = vTabRowidScope().handle(cursorAddress.cursor)) {
            is VTabRowidSuccessResult -> {
                setRowid(result.rowid)
                SqliteResultCode.OK
            }

            is VTabResultFailureResult -> result.result
        }.code
    }

    inline fun update(
        arguments: Array<sqlite3_value>,
        setRowid: (Long) -> Unit
    ) = checkNotNull(callbacks.update).run {
        when (val result = vTabUpdateScope().handle(vTab, arguments)) {
            is VTabUpdateSuccessResult -> {
                result.rowid?.let(setRowid)
                SqliteResultCode.OK
            }

            is VTabResultFailureResult -> result.result
        }.code
    }

    inline fun begin() = checkNotNull(callbacks.begin).handle(vTab).code

    inline fun sync() = checkNotNull(callbacks.sync).handle(vTab).code

    inline fun commit() = checkNotNull(callbacks.commit).handle(vTab).code

    inline fun rollback() = checkNotNull(callbacks.rollback).handle(vTab).code

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
        when (val result = vTabFindFunctionScope().handle(vTab, argumentCount, functionName)) {
            VTabFindFunctionDoNotOverloadResult -> 0

            is VTabFindFunctionOverloadResult -> {
                setFunction(vTab, result.appData, result.function, result.destroy)
                result.result
            }
        }
    }

    inline fun rename(newName: String) =
        checkNotNull(callbacks.rename).handle(vTab, newName).code

    inline fun savepoint(savepoint: Int) =
        checkNotNull(callbacks.savepoint).handle(vTab, savepoint).code

    inline fun release(savepoint: Int) =
        checkNotNull(callbacks.release).handle(vTab, savepoint).code

    inline fun rollbackTo(savepoint: Int) =
        checkNotNull(callbacks.rollbackTo).handle(vTab, savepoint).code

    inline fun integrity(
        schema: String,
        tableName: String,
        flags: Int,
        setError: (String) -> Unit
    ) = checkNotNull(callbacks.integrity).run {
        when (val result = vTabIntegrityScope().handle(vTab, schema, tableName, flags)) {
            is VTabIntegritySuccessResult -> {
                result.error?.let(setError)
                SqliteResultCode.OK
            }

            is VTabIntegrityFailureResult -> {
                setError(result.error)
                result.result
            }
        }.code
    }
}

/**
 * Map linking [VTabState]s by the native address of their associated [sqlite3_vtab].
 * This is because there is no way to store an arbitrary object in the native [sqlite3_vtab] struct
 * in most interops.
 */
private val VTabStates = ConcurrentMap<Long, VTabState<*, *>>()

/**
 * Returns the [VTabState] for `this` address.
 * This property should be accessed between xCreate/xConnect and xDisconnect/xDestroy.
 */
private inline val Long.vTabState: VTabState<*, *>
    get() = checkNotNull(VTabStates[this]) {
        "Virtual table state for address $this was not found"
    }

/**
 * Invokes `this` create or connect callback.
 */
private inline fun <AppData, VTab : sqlite3_vtab> CreateOrConnect<in AppData, VTab>.createOrConnect(
    module: VTabModule<AppData, VTab, *>,
    db: sqlite3,
    argv: Array<String>,
    setVTab: (sqlite3_vtab) -> Unit,
    setError: (String) -> Unit
): Int = when (val result = vTabCreateOrConnectScope<VTab>().handle(db, module.appData, argv)) {
    is VTabCreateOrConnectSuccessResult -> {
        setVTab(result.vTab.apply {
            check(VTabStates.put(address, VTabState(this, module.callbacks)) == null) {
                "A virtual table instance is already registered with the address $address"
            }
        })

        SqliteResultCode.OK
    }

    is VTabCreateOrConnectFailureResult -> {
        setError(result.error)
        SqliteResultCode.ERROR
    }
}.code

/**
 * Invokes the [VTabModuleCallbacks.create].
 */
internal inline fun <AppData, VTab : sqlite3_vtab> vTabCreate(
    module: VTabModule<AppData, VTab, *>,
    db: sqlite3,
    argv: Array<String>,
    setVTab: (sqlite3_vtab) -> Unit,
    setError: (String) -> Unit
) = checkNotNull(module.callbacks.create).createOrConnect(module, db, argv, setVTab, setError)

/**
 * Invokes the [VTabModuleCallbacks.connect].
 */
internal inline fun <AppData, VTab : sqlite3_vtab> vTabConnect(
    module: VTabModule<AppData, VTab, *>,
    db: sqlite3,
    argv: Array<String>,
    setVTab: (sqlite3_vtab) -> Unit,
    setError: (String) -> Unit
) = module.callbacks.connect.createOrConnect(module, db, argv, setVTab, setError)

/**
 * Invokes the [VTabModuleCallbacks.bestIndex].
 */
internal inline fun vTabBestIndex(
    vTab: Long,
    info: sqlite3_index_info
) = vTab.vTabState.bestIndex(info)

/**
 * Invokes the [VTabModuleCallbacks.disconnect].
 */
internal inline fun vTabDisconnect(
    vTab: Long,
    cleanup: (sqlite3_vtab) -> Unit
): Int = vTab.vTabState.disconnect(cleanup)

/**
 * Invokes the [VTabModuleCallbacks.destroy].
 */
internal inline fun vTabDestroy(
    vTab: Long,
    cleanup: (sqlite3_vtab) -> Unit
) = vTab.vTabState.destroy(cleanup)

/**
 * Invokes the [VTabModuleCallbacks.open].
 */
internal inline fun vTabOpen(
    vTab: Long,
    setCursor: (sqlite3_vtab_cursor) -> Unit
) = vTab.vTabState.open(setCursor)

/**
 * Invokes the [VTabModuleCallbacks.close].
 */
internal inline fun vTabClose(
    vTab: Long,
    cursor: Long,
    cleanup: (sqlite3_vtab_cursor) -> Unit
): Int = vTab.vTabState.closeCursor(cursor, cleanup)

/**
 * Invokes the [VTabModuleCallbacks.filter].
 */
internal inline fun vTabFilter(
    vTab: Long,
    cursor: Long,
    idxNum: Int,
    idxStr: String?,
    arguments: Array<sqlite3_value>
) = vTab.vTabState.filter(cursor, idxNum, idxStr, arguments)

/**
 * Invokes the [VTabModuleCallbacks.next].
 */
internal inline fun vTabNext(
    vTab: Long,
    cursor: Long
) = vTab.vTabState.next(cursor)

/**
 * Invokes the [VTabModuleCallbacks.eof].
 */
internal inline fun vTabEof(
    vTab: Long,
    cursor: Long
) = vTab.vTabState.eof(cursor)

/**
 * Invokes the [VTabModuleCallbacks.column].
 */
internal inline fun vTabColumn(
    vTab: Long,
    cursor: Long,
    context: sqlite3_context,
    columnIndex: Int
) = vTab.vTabState.column(cursor, context, columnIndex)

/**
 * Invokes the [VTabModuleCallbacks.rowid].
 */
internal inline fun vTabRowid(
    vTab: Long,
    cursor: Long,
    setRowid: (Long) -> Unit
) = vTab.vTabState.rowid(cursor, setRowid)

/**
 * Invokes the [VTabModuleCallbacks.update].
 */
internal inline fun vTabUpdate(
    vTab: Long,
    arguments: Array<sqlite3_value>,
    setRowid: (Long) -> Unit
) = vTab.vTabState.update(arguments, setRowid)

/**
 * Invokes the [VTabModuleCallbacks.findFunction].
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
 * Invokes the [VTabModuleCallbacks.begin].
 */
internal inline fun vTabBegin(vTab: Long) = vTab.vTabState.begin()

/**
 * Invokes the [VTabModuleCallbacks.sync].
 */
internal inline fun vTabSync(vTab: Long) = vTab.vTabState.sync()

/**
 * Invokes the [VTabModuleCallbacks.commit].
 */
internal inline fun vTabCommit(vTab: Long) = vTab.vTabState.commit()

/**
 * Invokes the [VTabModuleCallbacks.rollback].
 */
internal inline fun vTabRollback(vTab: Long) = vTab.vTabState.rollback()

/**
 * Invokes the [VTabModuleCallbacks.rename].
 */
internal inline fun vTabRename(
    vTab: Long,
    newName: String
) = vTab.vTabState.rename(newName)

/**
 * Invokes the [VTabModuleCallbacks.savepoint].
 */
internal inline fun vTabSavepoint(
    vTab: Long,
    savepoint: Int
) = vTab.vTabState.savepoint(savepoint)

/**
 * Invokes the [VTabModuleCallbacks.release].
 */
internal inline fun vTabRelease(
    vTab: Long,
    savepoint: Int
) = vTab.vTabState.release(savepoint)

/**
 * Invokes the [VTabModuleCallbacks.rollbackTo].
 */
internal inline fun vTabRollbackTo(
    vTab: Long,
    savepoint: Int
) = vTab.vTabState.rollbackTo(savepoint)

/**
 * Invokes the [VTabModuleCallbacks.integrity].
 */
internal inline fun vTabIntegrity(
    vTab: Long,
    schema: String,
    tableName: String,
    flags: Int,
    setError: (String) -> Unit
) = vTab.vTabState.integrity(schema, tableName, flags, setError)